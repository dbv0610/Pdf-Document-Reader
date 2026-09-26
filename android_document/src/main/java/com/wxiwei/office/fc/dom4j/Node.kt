/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j

import java.io.IOException
import java.io.Writer

/**
 * 
 * 
 * `Node` defines the polymorphic behavior for all XML nodes in a
 * dom4j tree.
 * 
 * 
 * 
 * 
 * A node can be output as its XML format, can be detached from its position in
 * a document and can have XPath expressions evaluated on itself.
 * 
 * 
 * 
 * 
 * A node may optionally support the parent relationship and may be read only.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.31 $
 * 
 * @see .supportsParent
 * 
 * @see .isReadOnly
 */
interface Node : Cloneable {
    /**
     * 
     * 
     * `supportsParent` returns true if this node supports the
     * parent relationship.
     * 
     * 
     * 
     * 
     * Some XML tree implementations are singly linked and only support downward
     * navigation through children relationships. The default case is that both
     * parent and children relationships are supported though for memory and
     * performance reasons the parent relationship may not be supported.
     * 
     * 
     * @return true if this node supports the parent relationship or false it is
     * not supported
     */
    fun supportsParent(): Boolean

    /**
     * 
     * 
     * `getParent` returns the parent `Element` if
     * this node supports the parent relationship or null if it is the root
     * element or does not support the parent relationship.
     * 
     * 
     * 
     * 
     * This method is an optional feature and may not be supported for all
     * `Node` implementations.
     * 
     * 
     * @return the parent of this node or null if it is the root of the tree or
     * the parent relationship is not supported.
     */
    /**
     * 
     * 
     * `setParent` sets the parent relationship of this node if the
     * parent relationship is supported or does nothing if the parent
     * relationship is not supported.
     * 
     * 
     * 
     * 
     * This method should only be called from inside an `Element`
     * implementation method and is not intended for general use.
     * 
     * 
     * @param parent
     * is the new parent of this node.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getParentProperty")
    @set:JvmName("setParentProperty")
    var parent: Element?
    fun getParent(): Element? = parent
    fun setParent(parent: Element?) { this.parent = parent }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDocumentProperty")
    @set:JvmName("setDocumentProperty")
    var document: Document?
    fun getDocument(): Document? = document
    fun setDocument(document: Document?) { this.document = document }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("isReadOnlyProperty")
    val isReadOnly: Boolean
    fun isReadOnly(): Boolean = isReadOnly

    /**
     * 
     * 
     * `hasContent` returns true if this node is a Branch (either
     * an Element or a Document) and it contains at least one content node such
     * as a child Element or Text node.
     * 
     * 
     * @return true if this `Node` is a Branch with a nodeCount()
     * of one or more.
     */
    fun hasContent(): Boolean

    /**
     * 
     * 
     * `getName` returns the name of this node. This is the XML
     * local name of the element, attribute, entity or processing instruction.
     * For CDATA and Text nodes this method will return null.
     * 
     * 
     * @return the XML name of this node
     */
    /**
     * 
     * 
     * Sets the text data of this node or this method will throw an
     * `UnsupportedOperationException` if it is read-only.
     * 
     * 
     * @param name
     * is the new name of this node
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNameProperty")
    @set:JvmName("setNameProperty")
    var name: String?
    fun getName(): String? = name
    fun setName(name: String?) { this.name = name }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextProperty")
    @set:JvmName("setTextProperty")
    var text: String?
    fun getText(): String? = text
    fun setText(text: String?) { this.text = text }

    /**
     * Returns the XPath string-value of this node. The behaviour of this method
     * is defined in the [XPath
     * specification ](http://www.w3.org/TR/xpath).
     * 
     * @return the text from all the child Text and Element nodes appended
     * together.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    val stringValue: String?
    fun getStringValue(): String? = stringValue

    /**
     * 
     * 
     * Returns the XPath expression which will return a node set containing the
     * given node such as /a/b/&#64;c. No indexing will be used to restrict the
     * path if multiple elements with the same name occur on the path.
     * 
     * 
     * @return the XPath expression which will return a nodeset containing at
     * least this node.
     */
    val path: String?

    /**
     * Returns the relative XPath expression which will return a node set
     * containing the given node such as a/b/&#64;c. No indexing will be used to
     * restrict the path if multiple elements with the same name occur on the
     * path.
     * 
     * @param context
     * is the parent context from which the relative path should
     * start. If the context is null or the context is not an
     * ancestor of this node then the path will be absolute and start
     * from the document and so begin with the '/' character.
     * 
     * @return the XPath expression relative to the given context which will
     * return a nodeset containing at least this node.
     */
    fun getPath(context: Element?): String?

    /**
     * 
     * 
     * Returns the XPath expression which will return a nodeset of one node
     * which is the current node. This method will use the XPath index operator
     * to restrict the path if multiple elements with the same name occur on the
     * path.
     * 
     * 
     * @return the XPath expression which will return a nodeset containing just
     * this node.
     */
    val uniquePath: String?

    /**
     * 
     * 
     * Returns the relative unique XPath expression from the given context which
     * will return a nodeset of one node which is the current node. This method
     * will use the XPath index operator to restrict the path if multiple
     * elements with the same name occur on the path.
     * 
     * 
     * @param context
     * is the parent context from which the path should start. If the
     * context is null or the context is not an ancestor of this node
     * then the path will start from the document and so begin with
     * the '/' character.
     * 
     * @return the XPath expression relative to the given context which will
     * return a nodeset containing just this node.
     */
    fun getUniquePath(context: Element?): String?

    /**
     * 
     * 
     * `asXML` returns the textual XML representation of this node.
     * 
     * 
     * @return the XML representation of this node
     */
    fun asXML(): String?

    /**
     * 
     * 
     * `write` writes this node as the default XML notation for
     * this node. If you wish to control the XML output (such as for pretty
     * printing, changing the indentation policy etc.) then please use [ ] or its derivations.
     * 
     * 
     * @param writer
     * is the `Writer` to output the XML to
     * 
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(writer: Writer?)

    /**
     * Returns the code according to the type of node. This makes processing
     * nodes polymorphically much easier as the switch statement can be used
     * instead of multiple if (instanceof) statements.
     * 
     * @return a W3C DOM complient code for the node type such as ELEMENT_NODE
     * or ATTRIBUTE_NODE
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeProperty")
    val nodeType: Short
    fun getNodeType(): Short = nodeType

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNodeTypeNameProperty")
    val nodeTypeName: String?
    fun getNodeTypeName(): String? = nodeTypeName

    /**
     * 
     * 
     * Removes this node from its parent if there is one. If this node is the
     * root element of a document then it is removed from the document as well.
     * 
     * 
     * 
     * 
     * This method is useful if you want to remove a node from its source
     * document and add it to another document. For example
     * 
     * ` Node node = ...; Element someOtherElement = ...;
     * someOtherElement.add( node.detach() ); `
     * 
     * @return the node that has been removed from its parent node if any and
     * its document if any.
     */
    fun detach(): Node?

    /**
     * 
     * 
     * `selectNodes` evaluates an XPath expression and returns the
     * result as a `List` of `Node` instances or
     * `String` instances depending on the XPath expression.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to be evaluated
     * 
     * @return the list of `Node` or `String`
     * instances depending on the XPath expression
     */
    fun selectNodes(xpathExpression: String?): MutableList<*>?

    /**
     * 
     * 
     * `selectObject` evaluates an XPath expression and returns the
     * result as an [Object]. The object returned can either be a [ ] of one or more [Node]instances or a scalar object like a
     * [String]or a [Number]instance depending on the XPath
     * expression.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to be evaluated
     * 
     * @return the value of the XPath expression as a [List]of [         ] instances, a [String]or a [Number]instance
     * depending on the XPath expression.
     */
    fun selectObject(xpathExpression: String?): Any?

    /**
     * 
     * 
     * `selectNodes` evaluates an XPath expression then sorts the
     * results using a secondary XPath expression Returns a sorted
     * `List` of `Node` instances.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to be evaluated
     * @param comparisonXPathExpression
     * is the XPath expression used to compare the results by for
     * sorting
     * 
     * @return the list of `Node` instances sorted by the
     * comparisonXPathExpression
     */
    fun selectNodes(xpathExpression: String?, comparisonXPathExpression: String?): MutableList<*>?

    /**
     * 
     * 
     * `selectNodes` evaluates an XPath expression then sorts the
     * results using a secondary XPath expression Returns a sorted
     * `List` of `Node` instances.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to be evaluated
     * @param comparisonXPathExpression
     * is the XPath expression used to compare the results by for
     * sorting
     * @param removeDuplicates
     * if this parameter is true then duplicate values (using the
     * comparisonXPathExpression) are removed from the result List.
     * 
     * @return the list of `Node` instances sorted by the
     * comparisonXPathExpression
     */
    fun selectNodes(
        xpathExpression: String?, comparisonXPathExpression: String?,
        removeDuplicates: Boolean
    ): MutableList<*>?

    /**
     * 
     * 
     * `selectSingleNode` evaluates an XPath expression and returns
     * the result as a single `Node` instance.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to be evaluated
     * 
     * @return the `Node` matching the XPath expression
     */
    fun selectSingleNode(xpathExpression: String?): Node?

    /**
     * 
     * 
     * `valueOf` evaluates an XPath expression and returns the
     * textual representation of the results the XPath string-value of this
     * node. The string-value for a given node type is defined in the [XPath specification ](http://www.w3.org/TR/xpath).
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to be evaluated
     * 
     * @return the string-value representation of the results of the XPath
     * expression
     */
    fun valueOf(xpathExpression: String?): String?

    /**
     * 
     * 
     * `numberValueOf` evaluates an XPath expression and returns
     * the numeric value of the XPath expression if the XPath expression results
     * in a number, or null if the result is not a number.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to be evaluated
     * 
     * @return the numeric result of the XPath expression or null if the result
     * is not a number.
     */
    fun numberValueOf(xpathExpression: String?): Number?

    /**
     * 
     * 
     * `matches` returns true if evaluating the given XPath
     * expression on this node returns a non-empty node set containing this
     * node.
     * 
     * 
     * 
     * 
     * This method does not behave like the &lt;xsl:if&gt; element - if you want
     * that behaviour, to evaluate if an XPath expression matches something,
     * then you can use the following code to be equivalent...
     * 
     * `if ( node.selectSingleNode( "/some/path" ) != nulll )`
     * 
     * @param xpathExpression
     * is an XPath expression
     * 
     * @return true if this node is returned by the given XPath expression
     */
    fun matches(xpathExpression: String?): Boolean

    /**
     * 
     * 
     * `createXPath` creates an XPath object for the given
     * xpathExpression. The XPath object allows the variable context to be
     * specified.
     * 
     * 
     * @param xpathExpression
     * is the XPath expression to be evaluated
     * 
     * @return an XPath object represeting the given expression
     * 
     * @throws InvalidXPathException
     * if the XPath expression is invalid
     */
    @Throws(InvalidXPathException::class)
    fun createXPath(xpathExpression: String?): XPath?

    /**
     * 
     * 
     * `asXPathResult` returns a version of this node which is
     * capable of being an XPath result. The result of an XPath expression
     * should always support the parent relationship, whether the original XML
     * tree was singly or doubly linked. If the node does not support the parent
     * relationship then a new node will be created which is linked to its
     * parent and returned.
     * 
     * 
     * @param parent
     * DOCUMENT ME!
     * 
     * @return a `Node` which supports the parent relationship
     */
    fun asXPathResult(parent: Element?): Node?

    /**
     * 
     * 
     * `accept` is the method used in the Visitor Pattern.
     * 
     * 
     * @param visitor
     * is the visitor in the Visitor Pattern
     */
    fun accept(visitor: Visitor?)

    /**
     * 
     * 
     * `clone` will return a deep clone or if this node is
     * read-only then clone will return the same instance.
     * 
     * 
     * @return a deep clone of myself or myself if I am read only.
     */
    public override fun clone(): Any

    companion object {
        // W3C DOM complient node type codes
        /** Matches Element nodes  */
        const val ANY_NODE: Short = 0

        /** Matches Element nodes  */
        const val ELEMENT_NODE: Short = 1

        /** Matches elements nodes  */
        const val ATTRIBUTE_NODE: Short = 2

        /** Matches elements nodes  */
        const val TEXT_NODE: Short = 3

        /** Matches elements nodes  */
        const val CDATA_SECTION_NODE: Short = 4

        /** Matches elements nodes  */
        const val ENTITY_REFERENCE_NODE: Short = 5

        /** Matches elements nodes  */ // public static final short ENTITY_NODE = 6;
        /** Matches ProcessingInstruction  */
        const val PROCESSING_INSTRUCTION_NODE: Short = 7

        /** Matches Comments nodes  */
        const val COMMENT_NODE: Short = 8

        /** Matches Document nodes  */
        const val DOCUMENT_NODE: Short = 9

        /** Matches DocumentType nodes  */
        const val DOCUMENT_TYPE_NODE: Short = 10

        // public static final short DOCUMENT_FRAGMENT_NODE = 11;
        // public static final short NOTATION_NODE = 12;
        /** Matchs a Namespace Node - NOTE this differs from DOM  */ // XXXX: ????
        const val NAMESPACE_NODE: Short = 13

        /** Does not match any valid node  */
        const val UNKNOWN_NODE: Short = 14

        /** The maximum number of node types for sizing purposes  */
        const val MAX_NODE_TYPE: Short = 14
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

