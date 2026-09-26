package com.wxiwei.office.simpletext.model

import com.wxiwei.office.constant.wp.AttrIDConstant
import org.junit.Assert.assertEquals
import org.junit.Test

class AttributeSetImplTest {
    private val fontSize = AttrIDConstant.FONT_SIZE_ID

    private fun style(id: Int, base: Int = -1, size: Int? = null): Style = Style().apply {
        setId(id)
        setBaseID(base)
        size?.let { getAttrbuteSet()!!.setAttribute(fontSize, it) }
        StyleManage.instance().addStyle(this)
    }

    private fun attributes(character: Int, paragraph: Int? = null) = AttributeSetImpl().apply {
        setAttribute(AttrIDConstant.FONT_STYLE_ID, character)
        paragraph?.let { setAttribute(AttrIDConstant.PARA_STYLE_ID, it) }
    }

    @Test fun selfReferenceReturnsMissingAttribute() {
        style(100000, 100000)
        assertEquals(Int.MIN_VALUE, attributes(100000).getAttribute(fontSize))
    }

    @Test fun circularCharacterStylesFallBackToParagraphStyle() {
        style(100001, 100002)
        style(100002, 100001)
        style(100003, size = 24)
        assertEquals(24, attributes(100001, 100003).getAttribute(fontSize))
    }

    @Test fun findsValueBeforeRevisitingCycle() {
        style(100004, 100005)
        style(100005, 100004, 18)
        assertEquals(18, attributes(100004).getAttribute(fontSize))
    }

    @Test fun missingStyleAndMissingBaseReturnMissingAttribute() {
        assertEquals(Int.MIN_VALUE, attributes(199999).getAttribute(fontSize))
        style(100006, 199999)
        assertEquals(Int.MIN_VALUE, attributes(100006).getAttribute(fontSize))
    }

    @Test fun preservesLocalAndNearestStylePrecedence() {
        style(100007, size = 12)
        style(100008, 100007, 18)
        style(100009, size = 24)
        val attributes = attributes(100008, 100009)
        assertEquals(18, attributes.getAttribute(fontSize))
        attributes.setAttribute(fontSize, 36)
        assertEquals(36, attributes.getAttribute(fontSize))
    }

    @Test fun deepInheritanceDoesNotUseCallStack() {
        for (id in 200000 until 220000) style(id, id + 1)
        style(220000, size = 42)
        assertEquals(42, attributes(200000).getAttribute(fontSize))
    }
}
