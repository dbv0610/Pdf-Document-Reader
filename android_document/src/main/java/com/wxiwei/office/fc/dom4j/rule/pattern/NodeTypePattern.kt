/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.rule.pattern

import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.rule.Pattern

/**
 * 
 * 
 * `NodeTypePattern` implements a Pattern which matches any node of
 * the given node type.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.7 $
 */
class NodeTypePattern(private val nodeType: Short) : Pattern {
    override fun matches(node: Node?): Boolean {
        return node!!.nodeType == nodeType
    }

    override val priority: Double
        get() {
            return Pattern.Companion.DEFAULT_PRIORITY
        }

    override val unionPatterns: Array<Pattern?>?
        get() {
            return null
        }

    override val matchType: Short
        get() {
            return nodeType
        }

    override val matchesNodeName: String?
        get() {
            return null
        }

    companion object {
        /** A pattern which matches any Attribute node  */
        val ANY_ATTRIBUTE: NodeTypePattern = NodeTypePattern(Node.Companion.ATTRIBUTE_NODE)

        /** A pattern which matches any Comment node  */
        val ANY_COMMENT: NodeTypePattern = NodeTypePattern(Node.Companion.COMMENT_NODE)

        /** A pattern which matches any Document node  */
        val ANY_DOCUMENT: NodeTypePattern = NodeTypePattern(Node.Companion.DOCUMENT_NODE)

        /** A pattern which matches any Element node  */
        val ANY_ELEMENT: NodeTypePattern = NodeTypePattern(Node.Companion.ELEMENT_NODE)

        /** A pattern which matches any ProcessingInstruction node  */
        val ANY_PROCESSING_INSTRUCTION: NodeTypePattern = NodeTypePattern(
            Node.Companion.PROCESSING_INSTRUCTION_NODE
        )

        /** A pattern which matches any Text node  */
        val ANY_TEXT: NodeTypePattern = NodeTypePattern(Node.Companion.TEXT_NODE)
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

