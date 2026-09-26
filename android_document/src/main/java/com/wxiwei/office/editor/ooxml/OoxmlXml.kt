package com.wxiwei.office.editor.ooxml

import com.wxiwei.office.fc.dom4j.DocumentHelper
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName
import kotlin.math.roundToLong

val W: Namespace = Namespace.get("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
val R: Namespace = Namespace.get("r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
val WP: Namespace = Namespace.get("wp", "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
val A: Namespace = Namespace.get("a", "http://schemas.openxmlformats.org/drawingml/2006/main")
val PIC: Namespace = Namespace.get("pic", "http://schemas.openxmlformats.org/drawingml/2006/picture")
val P: Namespace = Namespace.get("p", "http://schemas.openxmlformats.org/presentationml/2006/main")
val MC: Namespace = Namespace.get("mc", "http://schemas.openxmlformats.org/markup-compatibility/2006")
val SS: Namespace = Namespace.get("", "http://schemas.openxmlformats.org/spreadsheetml/2006/main")
const val REL_IMAGE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
const val REL_OFFICE_DOCUMENT = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
const val REL_SLIDE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide"

/** Matches expanded names; input documents may use different prefixes. */
fun Element.childrenNamed(ns: Namespace, local: String): List<Element> =
    elements().filterIsInstance<Element>().filter { it.namespaceURI == ns.uri && it.name == local }

fun Element.firstChild(ns: Namespace, local: String): Element? = childrenNamed(ns, local).firstOrNull()
fun newElement(ns: Namespace, local: String): Element = DocumentHelper.createElement(QName(local, ns))

/** 914400 EMU per inch; pixels are interpreted at 96 dpi. */
fun pxToEmu(px: Number): Long = (px.toDouble() * 9525.0).roundToLong()
fun ptToEmu(pt: Number): Long = (pt.toDouble() * 12700.0).roundToLong()
