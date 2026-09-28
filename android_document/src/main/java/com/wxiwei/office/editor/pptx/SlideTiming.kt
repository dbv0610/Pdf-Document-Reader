package com.wxiwei.office.editor.pptx

import com.wxiwei.office.editor.ooxml.MC
import com.wxiwei.office.editor.ooxml.P
import com.wxiwei.office.editor.ooxml.childrenNamed
import com.wxiwei.office.editor.ooxml.firstChild
import com.wxiwei.office.editor.ooxml.newElement
import com.wxiwei.office.fc.dom4j.Element

/**
 * One effect of a slide's animation sequence (PowerPoint's Animation pane), read from and written
 * to `<p:timing>`. Effects this code does not know (motion paths, text built by paragraph...) keep
 * their XML in [raw] and are written back as they were.
 */
data class SlideEffect(
    val shapeId: Int,
    val kind: Kind,
    val effect: Effect,
    val direction: Direction = Direction.BOTTOM,
    val start: Start = Start.CLICK,
    val durationMs: Int = 500,
    val delayMs: Int = 0,
    val raw: Element? = null,
) {
    enum class Kind { ENTRANCE, EMPHASIS, EXIT }
    enum class Effect { APPEAR, FADE, FLY, ZOOM, WIPE, PULSE, OTHER }
    enum class Direction(val subtype: Int) { TOP(1), RIGHT(2), BOTTOM(4), LEFT(8) }
    /** On a tap, together with the effect before, or after it. */
    enum class Start { CLICK, WITH, AFTER }
}

/**
 * How the slide comes in: [type] fade, push, wipe, cover, pull, split, zoom, cut, other or none;
 * [direction] l, r, u, d (push, wipe, cover, pull); it moves on by itself after [advanceAfterMs].
 */
data class SlideTransition(
    val type: String,
    val direction: String? = null,
    val durationMs: Int = 700,
    val advanceAfterMs: Int? = null,
    val advanceOnClick: Boolean = true,
)

object SlideTiming {
    private val P14 = "http://schemas.microsoft.com/office/powerpoint/2010/main"

    // ---- reading ---------------------------------------------------------------------------

    private fun els(e: Element?): List<Element> = e?.elements()?.filterIsInstance<Element>().orEmpty()
    private fun all(e: Element): Sequence<Element> = sequenceOf(e) + els(e).asSequence().flatMap { all(it) }
    private fun Element.p(local: String) = firstChild(P, local)
    private fun Element.ps(local: String) = childrenNamed(P, local)

    private fun mainSeq(sld: Element): Element? {
        val root = sld.p("timing")?.p("tnLst")?.p("par")?.p("cTn")?.p("childTnLst") ?: return null
        return root.ps("seq").firstOrNull { it.p("cTn")?.attributeValue("nodeType") == "mainSeq" }
    }

    /** The effects of the slide (the root element of a slide part), in play order. */
    fun effects(sld: Element): List<SlideEffect> {
        val seq = mainSeq(sld) ?: return emptyList()
        val out = ArrayList<SlideEffect>()
        for (click in seq.p("cTn")?.p("childTnLst")?.ps("par").orEmpty()) {
            for (group in click.p("cTn")?.p("childTnLst")?.ps("par").orEmpty()) {
                for (par in group.p("cTn")?.p("childTnLst")?.ps("par").orEmpty()) read(par)?.let { out.add(it) }
            }
        }
        return out
    }

    private fun read(par: Element): SlideEffect? {
        val cTn = par.p("cTn") ?: return null
        val spid = all(par).firstOrNull { it.name == "spTgt" }?.attributeValue("spid")?.toIntOrNull() ?: return null
        val kind = when (cTn.attributeValue("presetClass")) {
            "entr" -> SlideEffect.Kind.ENTRANCE
            "exit" -> SlideEffect.Kind.EXIT
            else -> SlideEffect.Kind.EMPHASIS
        }
        val start = when (cTn.attributeValue("nodeType")) {
            "withEffect" -> SlideEffect.Start.WITH
            "afterEffect" -> SlideEffect.Start.AFTER
            else -> SlideEffect.Start.CLICK
        }
        val preset = cTn.attributeValue("presetID")?.toIntOrNull() ?: 0
        val subtype = cTn.attributeValue("presetSubtype")?.toIntOrNull() ?: 0
        val byParagraph = all(par).any { it.name == "txEl" }
        val effect = when {
            byParagraph -> SlideEffect.Effect.OTHER
            cTn.attributeValue("presetClass") == "path" -> SlideEffect.Effect.OTHER
            preset == 1 -> SlideEffect.Effect.APPEAR
            preset == 10 -> SlideEffect.Effect.FADE
            preset == 2 -> SlideEffect.Effect.FLY
            preset == 53 -> SlideEffect.Effect.ZOOM
            preset == 22 -> SlideEffect.Effect.WIPE
            preset == 6 || preset == 26 -> if (kind == SlideEffect.Kind.EMPHASIS) SlideEffect.Effect.PULSE else SlideEffect.Effect.OTHER
            else -> SlideEffect.Effect.OTHER
        }
        val direction = SlideEffect.Direction.values().firstOrNull { it.subtype == subtype } ?: SlideEffect.Direction.BOTTOM
        // the longest behaviour, not the 1 ms visibility switch
        val durations = all(par).filter { it.name == "cTn" && it !== cTn }.mapNotNull { c -> c.attributeValue("dur")?.toIntOrNull()?.let { if (c.attributeValue("autoRev") == "1") it * 2 else it } }.filter { it > 1 }.toList()
        val duration = durations.maxOrNull() ?: if (effect == SlideEffect.Effect.APPEAR) 0 else 500
        val delay = cTn.p("stCondLst")?.p("cond")?.attributeValue("delay")?.toIntOrNull() ?: 0
        return SlideEffect(spid, kind, effect, direction, start, duration, delay,
            raw = if (effect == SlideEffect.Effect.OTHER) par.createCopy() else null)
    }

    /** The transition of the slide, or null when it has none. */
    fun transition(sld: Element): SlideTransition? {
        val t = sld.p("transition")
            ?: sld.childrenNamed(MC, "AlternateContent").firstNotNullOfOrNull { ac ->
                ac.firstChild(MC, "Choice")?.p("transition") ?: ac.firstChild(MC, "Fallback")?.p("transition")
            } ?: return null
        val kid = els(t).firstOrNull { it.name != "sndAc" && it.name != "extLst" }
        val type = when (kid?.name) {
            null -> "none"
            "fade", "push", "wipe", "cover", "pull", "split", "zoom", "cut" -> kid.name!!
            else -> "other"
        }
        val speed = when (t.attributeValue("spd")) { "fast" -> 500; "slow" -> 1000; else -> 750 }
        val dur = t.attributeValue(com.wxiwei.office.fc.dom4j.QName("dur", com.wxiwei.office.fc.dom4j.Namespace.get("p14", P14)))?.toIntOrNull() ?: speed
        return SlideTransition(type, kid?.attributeValue("dir"), if (type == "cut") 0 else dur,
            t.attributeValue("advTm")?.toIntOrNull(), t.attributeValue("advClick") != "0")
    }

    // ---- writing ---------------------------------------------------------------------------

    private fun el(local: String, vararg attrs: Pair<String, String>): Element =
        newElement(P, local).also { e -> attrs.forEach { (k, v) -> e.addAttribute(k, v) } }

    private fun add(parent: Element, child: Element): Element { parent.add(child); return child }

    private fun cond(delay: String) = el("stCondLst").also { add(it, el("cond", "delay" to delay)) }

    private fun target(spid: Int) = el("tgtEl").also { add(it, el("spTgt", "spid" to spid.toString())) }

    private fun cBhvr(spid: Int, dur: Int, delay: Int = 0, attr: String? = null, extra: Map<String, String> = emptyMap()): Element {
        val b = el("cBhvr")
        extra.forEach { (k, v) -> b.addAttribute(k, v) }
        val cTn = add(b, el("cTn", "id" to "0", "dur" to dur.toString(), "fill" to "hold"))
        if (delay > 0) add(cTn, cond(delay.toString()))
        add(b, target(spid))
        if (attr != null) add(b, el("attrNameLst")).also { add(it, el("attrName")).setText(attr) }
        return b
    }

    private fun visibility(spid: Int, visible: Boolean, delay: Int = 0): Element {
        val set = el("set")
        add(set, cBhvr(spid, 1, delay, "style.visibility"))
        add(set, el("to")).also { add(it, el("strVal", "val" to if (visible) "visible" else "hidden")) }
        return set
    }

    private fun animEffect(spid: Int, dur: Int, into: Boolean, filter: String) =
        el("animEffect", "transition" to if (into) "in" else "out", "filter" to filter).also { add(it, cBhvr(spid, dur)) }

    private fun anim(spid: Int, dur: Int, attr: String, from: String, to: String): Element {
        val a = el("anim", "calcmode" to "lin", "valueType" to "num")
        add(a, cBhvr(spid, dur, attr = attr, extra = mapOf("additive" to "base")))
        val list = add(a, el("tavLst"))
        for ((tm, v) in listOf("0" to from, "100000" to to)) {
            add(list, el("tav", "tm" to tm)).also { tav -> add(tav, el("val")).also { add(it, el("strVal", "val" to v)) } }
        }
        return a
    }

    private fun off(d: SlideEffect.Direction): Pair<String, String> = when (d) {
        SlideEffect.Direction.LEFT -> "ppt_x" to "0-#ppt_w/2"
        SlideEffect.Direction.RIGHT -> "ppt_x" to "1+#ppt_w/2"
        SlideEffect.Direction.TOP -> "ppt_y" to "0-#ppt_h/2"
        SlideEffect.Direction.BOTTOM -> "ppt_y" to "1+#ppt_h/2"
    }

    private fun wipe(d: SlideEffect.Direction) = when (d) {
        SlideEffect.Direction.BOTTOM -> "wipe(up)"; SlideEffect.Direction.TOP -> "wipe(down)"
        SlideEffect.Direction.LEFT -> "wipe(right)"; SlideEffect.Direction.RIGHT -> "wipe(left)"
    }

    private fun presetId(e: SlideEffect) = when (e.effect) {
        SlideEffect.Effect.APPEAR -> 1; SlideEffect.Effect.FADE -> 10; SlideEffect.Effect.FLY -> 2
        SlideEffect.Effect.ZOOM -> 53; SlideEffect.Effect.WIPE -> 22; SlideEffect.Effect.PULSE -> 6
        SlideEffect.Effect.OTHER -> 0
    }

    /** The `<p:par>` of one effect, as PowerPoint writes it. */
    private fun effectPar(e: SlideEffect, nodeType: String): Element {
        e.raw?.let { raw ->
            val copy = raw.createCopy()!!
            copy.p("cTn")?.addAttribute("nodeType", nodeType)
            return copy
        }
        val s = e.shapeId
        val d = maxOf(1, e.durationMs)
        val par = el("par")
        val cTn = add(par, el("cTn", "id" to "0", "presetID" to presetId(e).toString(),
            "presetClass" to when (e.kind) { SlideEffect.Kind.ENTRANCE -> "entr"; SlideEffect.Kind.EXIT -> "exit"; else -> "emph" },
            "presetSubtype" to (if (e.effect == SlideEffect.Effect.FLY || e.effect == SlideEffect.Effect.WIPE) e.direction.subtype else 0).toString(),
            "fill" to "hold", "nodeType" to nodeType))
        add(cTn, cond(e.delayMs.toString()))
        val kids = add(cTn, el("childTnLst"))
        when (e.kind) {
            SlideEffect.Kind.ENTRANCE -> {
                add(kids, visibility(s, true))
                when (e.effect) {
                    SlideEffect.Effect.FADE -> add(kids, animEffect(s, d, true, "fade"))
                    SlideEffect.Effect.FLY -> {
                        val (attr, from) = off(e.direction)
                        add(kids, anim(s, d, "ppt_x", if (attr == "ppt_x") from else "#ppt_x", "#ppt_x"))
                        add(kids, anim(s, d, "ppt_y", if (attr == "ppt_y") from else "#ppt_y", "#ppt_y"))
                    }
                    SlideEffect.Effect.ZOOM -> {
                        add(kids, anim(s, d, "ppt_w", "0", "#ppt_w"))
                        add(kids, anim(s, d, "ppt_h", "0", "#ppt_h"))
                        add(kids, animEffect(s, d, true, "fade"))
                    }
                    SlideEffect.Effect.WIPE -> add(kids, animEffect(s, d, true, wipe(e.direction)))
                    else -> Unit
                }
            }
            SlideEffect.Kind.EXIT -> {
                when (e.effect) {
                    SlideEffect.Effect.FADE -> add(kids, animEffect(s, d, false, "fade"))
                    SlideEffect.Effect.FLY -> {
                        val (attr, to) = off(e.direction)
                        add(kids, anim(s, d, "ppt_x", "#ppt_x", if (attr == "ppt_x") to else "#ppt_x"))
                        add(kids, anim(s, d, "ppt_y", "#ppt_y", if (attr == "ppt_y") to else "#ppt_y"))
                    }
                    SlideEffect.Effect.ZOOM -> {
                        add(kids, anim(s, d, "ppt_w", "#ppt_w", "0"))
                        add(kids, anim(s, d, "ppt_h", "#ppt_h", "0"))
                        add(kids, animEffect(s, d, false, "fade"))
                    }
                    SlideEffect.Effect.WIPE -> add(kids, animEffect(s, d, false, wipe(e.direction)))
                    else -> Unit
                }
                add(kids, visibility(s, false, if (e.effect == SlideEffect.Effect.APPEAR) 0 else d - 1))
            }
            SlideEffect.Kind.EMPHASIS -> {
                val scale = el("animScale")
                add(scale, cBhvr(s, maxOf(1, d / 2)).also { it.p("cTn")!!.addAttribute("autoRev", "1") })
                add(scale, el("by", "x" to "150000", "y" to "150000"))
                add(kids, scale)
            }
        }
        return par
    }

    /** Length of an effect from the start of its group. */
    private fun length(e: SlideEffect) = e.delayMs + e.durationMs

    /**
     * Replaces the main sequence of the slide with [effects] (an empty list removes the animations);
     * other sequences (triggered effects) stay.
     */
    fun setEffects(sld: Element, effects: List<SlideEffect>) {
        var timing = sld.p("timing")
        val oldSeq = timing?.let { mainSeq(sld) }
        if (effects.isEmpty()) {
            if (oldSeq != null) oldSeq.parent!!.remove(oldSeq)
            if (timing != null && all(timing).none { it.name == "seq" }) sld.remove(timing)
            else if (timing != null) prune(timing, emptySet())
            if (timing != null) renumber(timing)
            return
        }
        if (timing == null) {
            timing = el("timing")
            insertInSld(sld, timing)
        }
        val tnLst = timing.p("tnLst") ?: add(timing, el("tnLst")).also { moveFirst(timing, it) }
        val rootPar = tnLst.p("par") ?: add(tnLst, el("par"))
        val rootCTn = rootPar.p("cTn") ?: add(rootPar, el("cTn", "id" to "0", "dur" to "indefinite", "restart" to "never", "nodeType" to "tmRoot"))
        val rootKids = rootCTn.p("childTnLst") ?: add(rootCTn, el("childTnLst"))
        val seq = el("seq", "concurrent" to "1", "nextAc" to "seek")
        val seqCTn = add(seq, el("cTn", "id" to "0", "dur" to "indefinite", "nodeType" to "mainSeq"))
        val clicks = add(seqCTn, el("childTnLst"))
        add(seq, el("prevCondLst")).also { add(it, el("cond", "evt" to "onPrev", "delay" to "0")).also { c -> add(c, el("tgtEl")).also { t -> add(t, el("sldTgt")) } } }
        add(seq, el("nextCondLst")).also { add(it, el("cond", "evt" to "onNext", "delay" to "0")).also { c -> add(c, el("tgtEl")).also { t -> add(t, el("sldTgt")) } } }
        // click groups: a new one at every "on click"; inside, a new timed group at every "after"
        var clickKids: Element? = null
        var groupKids: Element? = null
        var groupStart = 0
        var groupEnd = 0
        effects.forEachIndexed { i, e ->
            if (clickKids == null || e.start == SlideEffect.Start.CLICK) {
                val clickPar = add(clicks, el("par"))
                val clickCTn = add(clickPar, el("cTn", "id" to "0", "fill" to "hold"))
                val st = add(clickCTn, el("stCondLst"))
                add(st, el("cond", "delay" to "indefinite"))
                // effects at the start of the slide (the first ones, not on a tap) begin with it
                if (i == 0 && e.start != SlideEffect.Start.CLICK) add(st, el("cond", "evt" to "onBegin", "delay" to "0")).also { add(it, el("tn", "val" to "2")) }
                clickKids = add(clickCTn, el("childTnLst"))
                groupKids = null; groupStart = 0; groupEnd = 0
            }
            if (groupKids == null || e.start == SlideEffect.Start.AFTER) {
                groupStart = if (groupKids == null) 0 else groupEnd
                val groupPar = add(clickKids!!, el("par"))
                val groupCTn = add(groupPar, el("cTn", "id" to "0", "fill" to "hold"))
                add(groupCTn, cond(groupStart.toString()))
                groupKids = add(groupCTn, el("childTnLst"))
                groupEnd = groupStart
            }
            val nodeType = when (e.start) { SlideEffect.Start.CLICK -> "clickEffect"; SlideEffect.Start.WITH -> "withEffect"; SlideEffect.Start.AFTER -> "afterEffect" }
            add(groupKids!!, effectPar(e, nodeType))
            groupEnd = maxOf(groupEnd, groupStart + length(e))
        }
        if (oldSeq != null) {
            val parent = oldSeq.parent!!
            @Suppress("UNCHECKED_CAST")
            val content = parent.content() as MutableList<Any?>
            val at = content.indexOf(oldSeq)
            parent.remove(oldSeq)
            content.add(at, seq)
        } else {
            @Suppress("UNCHECKED_CAST")
            (rootKids.content() as MutableList<Any?>).add(0, seq)
        }
        prune(timing, effects.map { it.shapeId }.toSet())
        renumber(timing)
    }

    /** Build entries of shapes no longer animated go (PowerPoint rejects a build without its effect). */
    private fun prune(timing: Element, animated: Set<Int>) {
        val bldLst = timing.p("bldLst") ?: return
        val stillUsed = animated + all(timing).filter { it.name == "spTgt" }.mapNotNull { it.attributeValue("spid")?.toIntOrNull() }.toSet()
        for (b in els(bldLst)) {
            val spid = b.attributeValue("spid")?.toIntOrNull() ?: continue
            if (spid !in stillUsed) bldLst.remove(b)
        }
        if (els(bldLst).isEmpty()) timing.remove(bldLst)
    }

    private fun moveFirst(parent: Element, child: Element) {
        @Suppress("UNCHECKED_CAST")
        val content = parent.content() as MutableList<Any?>
        content.remove(child); content.add(0, child)
    }

    /** cTn ids must be unique, from 1 in document order. */
    private fun renumber(timing: Element) {
        var n = 1
        for (c in all(timing).filter { it.name == "cTn" && it.namespaceURI == P.uRI }) c.addAttribute("id", (n++).toString())
        // "onBegin" of the first effects points at the main sequence
        val seqId = all(timing).firstOrNull { it.name == "cTn" && it.attributeValue("nodeType") == "mainSeq" }?.attributeValue("id")
        if (seqId != null) all(timing).filter { it.name == "tn" }.forEach { it.addAttribute("val", seqId) }
    }

    /** CT_Slide: cSld, clrMapOvr, transition, timing, extLst. */
    private fun insertInSld(sld: Element, child: Element) {
        @Suppress("UNCHECKED_CAST")
        val content = sld.content() as MutableList<Any?>
        val order = listOf("cSld", "clrMapOvr", "transition", "AlternateContent", "timing", "extLst")
        val mine = order.indexOf(child.name)
        val next = content.indexOfFirst { it is Element && it !== child && order.indexOf(it.name) > mine }
        if (next < 0) content.add(child) else content.add(next, child)
    }

    /** Replaces the slide's transition ([t] null or type "none" removes it). */
    fun setTransition(sld: Element, t: SlideTransition?) {
        sld.p("transition")?.let { sld.remove(it) }
        sld.childrenNamed(MC, "AlternateContent").filter { ac -> all(ac).any { it.name == "transition" } }.forEach { sld.remove(it) }
        if (t == null || (t.type == "none" && t.advanceAfterMs == null && t.advanceOnClick)) return
        val speed = when { t.durationMs <= 600 -> "fast"; t.durationMs >= 900 -> "slow"; else -> "med" }
        val tr = el("transition", "spd" to speed)
        if (!t.advanceOnClick) tr.addAttribute("advClick", "0")
        t.advanceAfterMs?.let { tr.addAttribute("advTm", it.toString()) }
        when (t.type) {
            "push", "wipe", "cover", "pull" -> add(tr, el(t.type, "dir" to (t.direction ?: if (t.type == "push" || t.type == "pull" || t.type == "cover") "l" else "l")))
            "split" -> add(tr, el("split", "orient" to "horz", "dir" to "out"))
            "fade", "zoom", "cut" -> add(tr, el(t.type))
            "none" -> Unit
            else -> add(tr, el("fade"))
        }
        insertInSld(sld, tr)
    }
}
