/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.IllegalAddException
import com.wxiwei.office.fc.dom4j.Node

/**
 * `BackedList` represents a list of content of a
 * [com.wxiwei.office.fc.dom4j.Branch]. Changes to the list will be reflected in the branch,
 * though changes to the branch will not be reflected in this list.
 *
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.14 $
 */
class BackedList : ArrayList<Any?> {
    /** The content of the Branch which is modified if I am modified  */
    private val branchContent: MutableList<Any?>

    /** The `AbstractBranch` instance which owns the content  */
    private val branch: AbstractBranch

    @JvmOverloads
    constructor(
        branch: AbstractBranch,
        branchContent: MutableList<Any?>,
        capacity: Int = branchContent.size
    ) : super(capacity) {
        this.branch = branch
        this.branchContent = branchContent
    }

    constructor(
        branch: AbstractBranch,
        branchContent: MutableList<Any?>,
        initialContent: MutableList<*>
    ) : super(initialContent) {
        this.branch = branch
        this.branchContent = branchContent
    }

    override fun add(element: Any?): Boolean {
        branch.addNode(asNode(element))
        return super.add(element)
    }

    override fun add(index: Int, element: Any?) {
        val size = size
        if (index < 0) {
            throw IndexOutOfBoundsException("Index value: $index is less than zero")
        } else if (index > size) {
            throw IndexOutOfBoundsException(
                ("Index value: " + index
                        + " cannot be greater than " + "the size: " + size)
            )
        }

        val realIndex: Int
        if (size == 0) {
            realIndex = branchContent.size
        } else if (index < size) {
            realIndex = branchContent.indexOf(get(index))
        } else {
            realIndex = branchContent.indexOf(get(size - 1)) + 1
        }

        branch.addNode(realIndex, asNode(element))
        super.add(index, element)
    }

    override fun set(index: Int, element: Any?): Any? {
        var realIndex = branchContent.indexOf(get(index))

        if (realIndex < 0) {
            realIndex = if (index == 0) 0 else Int.MAX_VALUE
        }

        if (realIndex < branchContent.size) {
            branch.removeNode(asNode(get(index)))
            branch.addNode(realIndex, asNode(element))
        } else {
            branch.removeNode(asNode(get(index)))
            branch.addNode(asNode(element))
        }

        branch.childAdded(asNode(element))

        return super.set(index, element)
    }

    override fun remove(element: Any?): Boolean {
        branch.removeNode(asNode(element))

        return super.remove(element)
    }

    override fun removeAt(index: Int): Any? {
        val `object` = super.removeAt(index)

        if (`object` != null) {
            branch.removeNode(asNode(`object`))
        }

        return `object`
    }

    override fun addAll(elements: Collection<Any?>): Boolean {
        ensureCapacity(size + elements.size)

        var count = size

        val iter = elements.iterator()
        while (iter.hasNext()) {
            add(iter.next())
            count--
        }

        return count != 0
    }

    override fun addAll(index: Int, elements: Collection<Any?>): Boolean {
        var index = index
        ensureCapacity(size + elements.size)

        var count = size

        val iter = elements.iterator()
        while (iter.hasNext()) {
            add(index++, iter.next())
            count--
        }

        return count != 0
    }

    override fun clear() {
        val iter = iterator()
        while (iter.hasNext()) {
            val `object` = iter.next()
            branchContent.remove(`object`)
            branch.childRemoved(asNode(`object`))
        }

        super.clear()
    }

    /**
     * Performs a local addition which is not forward through to the Branch or
     * backing list
     */
    fun addLocal(`object`: Any?) {
        super.add(`object`)
    }

    protected fun asNode(`object`: Any?): Node {
        if (`object` is Node) {
            return `object`
        } else {
            throw IllegalAddException(
                ("This list must contain instances "
                        + "of Node. Invalid type: " + `object`)
            )
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

