/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.rule

import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.NodeFilter

/**
 * 
 * 
 * `Pattern` defines the behaviour for pattern in the XSLT
 * processing model.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.6 $
 */
interface Pattern : NodeFilter {
    /**
     * DOCUMENT ME!
     * 
     * @param node
     * DOCUMENT ME!
     * 
     * @return true if the pattern matches the given DOM4J node.
     */
    override fun matches(node: Node?): Boolean

    /**
     * Returns the default resolution policy of the pattern according to the [ XSLT conflict resolution
     * spec ](http://www.w3.org/TR/xslt11/#conflict).
     * 
     * @return DOCUMENT ME!
     */
    val priority: Double

    /**
     * If this pattern is a union pattern then this method should return an
     * array of patterns which describe the union pattern, which should contain
     * more than one pattern. Otherwise this method should return null.
     * 
     * @return an array of the patterns which make up this union pattern or null
     * if this pattern is not a union pattern
     */
    val unionPatterns: Array<Pattern?>?

    /**
     * DOCUMENT ME!
     * 
     * @return the type of node the pattern matches which by default should
     * return ANY_NODE if it can match any kind of node.
     */
    val matchType: Short

    /**
     * For patterns which only match an ATTRIBUTE_NODE or an ELEMENT_NODE then
     * this pattern may return the name of the element or attribute it matches.
     * This allows a more efficient rule matching algorithm to be performed,
     * rather than a brute force approach of evaluating every pattern for a
     * given Node.
     * 
     * @return the name of the element or attribute this pattern matches or null
     * if this pattern matches any or more than one name.
     */
    val matchesNodeName: String?

    companion object {
        // These node numbers are compatable with DOM4J's Node types
        /** Matches any node  */
        const val ANY_NODE: Short = 0

        /** Matches no nodes  */
        const val NONE: Short = 9999

        /** Count of the number of node types  */
        val NUMBER_OF_TYPES: Short = Node.Companion.UNKNOWN_NODE

        /**
         * According to the [spec
        ](http://www.w3.org/TR/xslt11/#conflict) *  we should return 0.5 if we cannot determine the priority
         */
        const val DEFAULT_PRIORITY: Double = 0.5
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

