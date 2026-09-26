package com.wxiwei.office.fc.ppt.reader

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PptXmlBooleanTest {
    @Test fun readsBothXmlRepresentations() {
        for (value in listOf("true", "1", " true ")) assertTrue(pptXmlBoolean(value))
        for (value in listOf("false", "0", " false ")) assertFalse(pptXmlBoolean(value, true))
    }

    @Test fun preservesAbsentAttributeDefault() {
        assertFalse(pptXmlBoolean(null))
        assertTrue(pptXmlBoolean(null, true))
    }
}
