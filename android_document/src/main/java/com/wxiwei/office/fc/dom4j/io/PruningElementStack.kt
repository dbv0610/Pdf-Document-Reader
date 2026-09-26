/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.ElementHandler

/**
 * 
 * 
 * `PruningElementStack` is a stack of [Element]instances
 * which will prune the tree when a path expression is reached. This is useful
 * for parsing very large documents where children of the root element can be
 * processed individually rather than keeping them all in memory at the same
 * time.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.11 $
 */
internal class PruningElementStack : ElementStack {
    /** ElementHandler to call when pruning occurs  */
    private val elementHandler: ElementHandler

    /**
     * the element name path which denotes the node to remove from its parent
     * when it is complete (i.e. when it is popped from the stack). The first
     * entry in the path will be a child of the root node
     */
    private val pathNames: Array<String?>

    /**
     * The level at which a path match can occur. We match when we have popped
     * the selected node so the and the lastElementIndex points to its parent so
     * this value should be path.length - 2
     */
    private var matchingElementIndex = 0

    constructor(path: Array<String?>, elementHandler: ElementHandler) {
        this.pathNames = path
        this.elementHandler = elementHandler
        checkPath()
    }

    constructor(path: Array<String?>, elementHandler: ElementHandler, defaultCapacity: Int) : super(
        defaultCapacity
    ) {
        this.pathNames = path
        this.elementHandler = elementHandler
        checkPath()
    }

    override fun popElement(): Element? {
        val answer = super.popElement()

        if ((lastElementIndex == matchingElementIndex) && (lastElementIndex >= 0)) {
            // we are popping the correct level in the tree
            // lets check if the path fits
            //
            // NOTE: this is an inefficient way of doing it - we could
            // maintain a history of which parts matched?
            if (validElement(answer, lastElementIndex + 1)) {
                var parent: Element? = null

                for (i in 0..lastElementIndex) {
                    parent = stack[i]

                    if (!validElement(parent, i)) {
                        parent = null

                        break
                    }
                }

                if (parent != null) {
                    pathMatches(parent, answer)
                }
            }
        }

        return answer
    }

    protected fun pathMatches(parent: Element, selectedNode: Element?) {
        elementHandler.onEnd(this)
        parent.remove(selectedNode)
    }

    protected fun validElement(element: Element?, index: Int): Boolean {
        val requiredName = pathNames[index]
        val name = element!!.name

        if (requiredName === name) {
            return true
        }

        if ((requiredName != null) && (name != null)) {
            return requiredName == name
        }

        return false
    }

    private fun checkPath() {
        if (pathNames.size < 2) {
            throw RuntimeException(
                ("Invalid path of length: " + pathNames.size
                        + " it must be greater than 2")
            )
        }

        matchingElementIndex = pathNames.size - 2
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

