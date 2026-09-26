/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.ElementHandler
import com.wxiwei.office.fc.dom4j.ElementPath

/**
 * This [org.dom4j.ElementHandler]is used to trigger [ ] objects in order to modify (parts of) the Document on the
 * fly.
 * 
 * 
 * 
 * When an element is completely parsed, a copy is handed to the associated (if
 * any) [ElementModifier]that on his turn returns the modified element
 * that has to come in the tree.
 * 
 * 
 * @author Wonne Keysers (Realsoftware.be)
 */
internal class SAXModifyElementHandler(private val elemModifier: ElementModifier) : ElementHandler {
    /**
     * DOCUMENT ME!
     * 
     * @return Returns the modified Element.
     */
    var modifiedElement: Element? = null
        private set

    override fun onStart(elementPath: ElementPath?) {
        this.modifiedElement = elementPath!!.current
    }

    override fun onEnd(elementPath: ElementPath?) {
        try {
            val origElement = elementPath!!.current!!
            val currentParent = origElement.parent

            if (currentParent != null) {
                // Clone sets parent + document to null
                val clonedElem = origElement.clone() as Element

                // Ask for modified element
                modifiedElement = elemModifier.modifyElement(clonedElem)

                if (modifiedElement != null) {
                    // Restore parent + document
                    modifiedElement!!.parent = origElement.parent
                    modifiedElement!!.document = origElement.document

                    // Replace old with new element in parent
                    val contentIndex = currentParent.indexOf(origElement)
                    (currentParent.content() as MutableList<Any?>).set(contentIndex, modifiedElement)
                }

                // Remove the old element
                origElement.detach()
            } else {
                if (origElement.isRootElement) {
                    // Clone sets parent + document to null
                    val clonedElem = origElement.clone() as Element

                    // Ask for modified element
                    modifiedElement = elemModifier.modifyElement(clonedElem)

                    if (modifiedElement != null) {
                        // Restore parent + document
                        modifiedElement!!.document = origElement.document

                        // Replace old with new element in parent
                        val doc = origElement.document
                        doc!!.rootElement = modifiedElement
                    }

                    // Remove the old element
                    origElement.detach()
                }
            }

            // Put the new element on the ElementStack, it might get pruned by
            // the PruningDispatchHandler
            if (elementPath is ElementStack) {
                val elementStack = elementPath
                elementStack.popElement()
                elementStack.pushElement(modifiedElement)
            }
        } catch (ex: Exception) {
            throw SAXModifyException(ex)
        }
    }
} /*
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name "DOM4J" must not be used to endorse or promote products derived
 * from this Software without prior written permission of MetaStuff, Ltd. For
 * written permission, please contact dom4j-info@metastuff.com.
 * 
 * 4. Products derived from this Software may not be called "DOM4J" nor may
 * "DOM4J" appear in their names without prior written permission of MetaStuff,
 * Ltd. DOM4J is a registered trademark of MetaStuff, Ltd.
 * 
 * 5. Due credit should be given to the DOM4J Project - http://www.dom4j.org
 * 
 * THIS SOFTWARE IS PROVIDED BY METASTUFF, LTD. AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL METASTUFF, LTD. OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 */

