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
 * `Branch` interface defines the common behaviour for Nodes which
 * can contain child nodes (content) such as XML elements and documents. This
 * interface allows both elements and documents to be treated in a polymorphic
 * manner when changing or navigating child nodes (content).
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.32 $
 */
interface Branch : Node {
    /**
     * Returns the `Node` at the specified index position.
     * 
     * @param index
     * the index of the node to return.
     * 
     * @return the `Node` at the specified position.
     * 
     * @throws IndexOutOfBoundsException
     * if the index is out of range (index &lt; 0 || index &gt;=
     * [nodeCount]).
     */
    @Throws(IndexOutOfBoundsException::class)
    fun node(index: Int): Node?

    /**
     * Returns the index of the given node if it is a child node of this branch
     * or -1 if the given node is not a child node.
     * 
     * @param node
     * the content child node to find.
     * 
     * @return the index of the given node starting at 0 or -1 if the node is
     * not a child node of this branch
     */
    fun indexOf(node: Node?): Int

    /**
     * Returns the number of `Node` instances that this branch
     * contains.
     * 
     * @return the number of nodes this branch contains
     */
    fun nodeCount(): Int

    /**
     * Returns the element of the given ID attribute value. If this tree is
     * capable of understanding which attribute value should be used for the ID
     * then it should be used, otherwise this method should return null.
     * 
     * @param elementID
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    fun elementByID(elementID: String?): Element?

    /**
     * 
     * 
     * Returns the content nodes of this branch as a backed [List]so that
     * the content of this branch may be modified directly using the
     * [List]interface. The `List` is backed by the
     * `Branch` so that changes to the list are reflected in the
     * branch and vice versa.
     * 
     * 
     * @return the nodes that this branch contains as a `List`
     */
    fun content(): MutableList<*>?

    /**
     * Returns an iterator through the content nodes of this branch
     * 
     * @return an iterator through the content nodes of this branch
     */
    fun nodeIterator(): MutableIterator<*>?

    /**
     * Sets the contents of this branch as a `List` of
     * `Node` instances.
     * 
     * @param content
     * is the list of nodes to use as the content for this branch.
     */
    fun setContent(content: MutableList<*>?)

    /**
     * Appends the content of the given branch to this branch instance. This
     * method behaves like the [ ][java.util.Collection.addAll] method.
     * 
     * @param branch
     * is the branch whose content will be added to me.
     */
    fun appendContent(branch: Branch?)

    /**
     * Clears the content for this branch, removing any `Node`
     * instances this branch may contain.
     */
    fun clearContent()

    /**
     * 
     * 
     * Returns a list of all the processing instructions in this branch. The
     * list is backed by this branch so that changes to the list will be
     * reflected in the branch but the reverse is not the case.
     * 
     * 
     * @return a backed list of the processing instructions
     */
    fun processingInstructions(): MutableList<*>?

    /**
     * 
     * 
     * Returns a list of the processing instructions for the given target. The
     * list is backed by this branch so that changes to the list will be
     * reflected in the branch but the reverse is not the case.
     * 
     * 
     * @param target
     * DOCUMENT ME!
     * 
     * @return a backed list of the processing instructions
     */
    fun processingInstructions(target: String?): MutableList<*>?

    /**
     * DOCUMENT ME!
     * 
     * @param target
     * DOCUMENT ME!
     * 
     * @return the processing instruction for the given target
     */
    fun processingInstruction(target: String?): ProcessingInstruction?

    /**
     * Sets all the processing instructions for this branch
     * 
     * @param listOfPIs
     * DOCUMENT ME!
     */
    fun setProcessingInstructions(listOfPIs: MutableList<*>?)

    /**
     * Adds a new `Element` node with the given name to this branch
     * and returns a reference to the new node.
     * 
     * @param name
     * is the name for the `Element` node.
     * 
     * @return the newly added `Element` node.
     */
    fun addElement(name: String?): Element?

    /**
     * Adds a new `Element` node with the given [QName]to
     * this branch and returns a reference to the new node.
     * 
     * @param qname
     * is the qualified name for the `Element` node.
     * 
     * @return the newly added `Element` node.
     */
    fun addElement(qname: QName?): Element?

    /**
     * Adds a new `Element` node with the given qualified name and
     * namespace URI to this branch and returns a reference to the new node.
     * 
     * @param qualifiedName
     * is the fully qualified name of the Element
     * @param namespaceURI
     * is the URI of the namespace to use
     * 
     * @return the newly added `Element` node.
     */
    fun addElement(qualifiedName: String?, namespaceURI: String?): Element?

    /**
     * Removes the processing instruction for the given target if it exists
     * 
     * @param target
     * DOCUMENT ME!
     * 
     * @return true if a processing instruction was removed else false
     */
    fun removeProcessingInstruction(target: String?): Boolean

    /**
     * Adds the given `Node` or throws [IllegalAddException]
     * if the given node is not of a valid type. This is a polymorphic method
     * which will call the typesafe method for the node type such as
     * add(Element) or add(Comment).
     * 
     * @param node
     * is the given node to add
     */
    fun add(node: Node?)

    /**
     * Adds the given `Comment` to this branch. If the given node
     * already has a parent defined then an `IllegalAddException`
     * will be thrown.
     * 
     * @param comment
     * is the comment to be added
     */
    fun add(comment: Comment?)

    /**
     * Adds the given `Element` to this branch. If the given node
     * already has a parent defined then an `IllegalAddException`
     * will be thrown.
     * 
     * @param element
     * is the element to be added
     */
    fun add(element: Element?)

    /**
     * Adds the given `ProcessingInstruction` to this branch. If
     * the given node already has a parent defined then an
     * `IllegalAddException` will be thrown.
     * 
     * @param pi
     * is the processing instruction to be added
     */
    fun add(pi: ProcessingInstruction?)

    /**
     * Removes the given `Node` if the node is an immediate child
     * of this branch. If the given node is not an immediate child of this
     * branch then the [detach]method should be used instead. This
     * is a polymorphic method which will call the typesafe method for the node
     * type such as remove(Element) or remove(Comment).
     * 
     * @param node
     * is the given node to be removed
     * 
     * @return true if the node was removed
     */
    fun remove(node: Node?): Boolean

    /**
     * Removes the given `Comment` if the node is an immediate
     * child of this branch. If the given node is not an immediate child of this
     * branch then the [detach]method should be used instead.
     * 
     * @param comment
     * is the comment to be removed
     * 
     * @return true if the comment was removed
     */
    fun remove(comment: Comment?): Boolean

    /**
     * Removes the given `Element` if the node is an immediate
     * child of this branch. If the given node is not an immediate child of this
     * branch then the [detach]method should be used instead.
     * 
     * @param element
     * is the element to be removed
     * 
     * @return true if the element was removed
     */
    fun remove(element: Element?): Boolean

    /**
     * Removes the given `ProcessingInstruction` if the node is an
     * immediate child of this branch. If the given node is not an immediate
     * child of this branch then the [detach]method should be used
     * instead.
     * 
     * @param pi
     * is the processing instruction to be removed
     * 
     * @return true if the processing instruction was removed
     */
    fun remove(pi: ProcessingInstruction?): Boolean

    /**
     * Puts all `Text` nodes in the full depth of the sub-tree
     * underneath this `Node`, including attribute nodes, into a
     * "normal" form where only structure (e.g., elements, comments, processing
     * instructions, CDATA sections, and entity references) separates
     * `Text` nodes, i.e., there are neither adjacent
     * `Text` nodes nor empty `Text` nodes. This can
     * be used to ensure that the DOM view of a document is the same as if it
     * were saved and re-loaded, and is useful when operations (such as XPointer
     * lookups) that depend on a particular document tree structure are to be
     * used.In cases where the document contains `CDATASections`,
     * the normalize operation alone may not be sufficient, since XPointers do
     * not differentiate between `Text` nodes and
     * `CDATASection` nodes.
     * 
     * @since DOM Level 2
     */
    fun normalize()
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

