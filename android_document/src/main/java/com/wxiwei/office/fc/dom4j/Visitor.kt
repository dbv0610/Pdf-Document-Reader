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
package com.wxiwei.office.fc.dom4j

/**
 * 
 * 
 * `Visitor` is used to implement the `Visitor`
 * pattern in DOM4J. An object of this interface can be passed to a
 * `Node` which will then call its typesafe methods. Please refer
 * to the *Gang of Four * book of Design Patterns for more details on the
 * `Visitor` pattern.
 * 
 * 
 * 
 * 
 * This [site ](http://www.patterndepot.com/put/8/JavaPatterns.htm)
 * has further discussion on design patterns and links to the GOF book. This [link ](http://www.patterndepot.com/put/8/visitor.pdf) describes the
 * Visitor pattern in detail.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.8 $
 */
interface Visitor {
    /**
     * 
     * 
     * Visits the given `Document`
     * 
     * 
     * @param document
     * is the `Document` node to visit.
     */
    fun visit(document: Document?)

    /**
     * 
     * 
     * Visits the given `DocumentType`
     * 
     * 
     * @param documentType
     * is the `DocumentType` node to visit.
     */
    fun visit(documentType: DocumentType?)

    /**
     * 
     * 
     * Visits the given `Element`
     * 
     * 
     * @param node
     * is the `Element` node to visit.
     */
    fun visit(node: Element?)

    /**
     * 
     * 
     * Visits the given `Attribute`
     * 
     * 
     * @param node
     * is the `Attribute` node to visit.
     */
    fun visit(node: Attribute?)

    /**
     * 
     * 
     * Visits the given `CDATA`
     * 
     * 
     * @param node
     * is the `CDATA` node to visit.
     */
    fun visit(node: CDATA?)

    /**
     * 
     * 
     * Visits the given `Comment`
     * 
     * 
     * @param node
     * is the `Comment` node to visit.
     */
    fun visit(node: Comment?)

    /**
     * 
     * 
     * Visits the given `Entity`
     * 
     * 
     * @param node
     * is the `Entity` node to visit.
     */
    fun visit(node: Entity?)

    /**
     * 
     * 
     * Visits the given `Namespace`
     * 
     * 
     * @param namespace
     * is the `Namespace` node to visit.
     */
    fun visit(namespace: Namespace?)

    /**
     * 
     * 
     * Visits the given `ProcessingInstruction`
     * 
     * 
     * @param node
     * is the `ProcessingInstruction` node to visit.
     */
    fun visit(node: ProcessingInstruction?)

    /**
     * 
     * 
     * Visits the given `Text`
     * 
     * 
     * @param node
     * is the `Text` node to visit.
     */
    fun visit(node: Text?)
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

