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
package com.wxiwei.office.fc.dom4j.tree

import com.wxiwei.office.fc.dom4j.IllegalAddException
import com.wxiwei.office.fc.dom4j.Node
import java.util.AbstractList

/**
 * `ContentListFacade` represents a facade of the content of a
 * [com.wxiwei.office.fc.dom4j.Branch] which is returned via calls to the
 * [com.wxiwei.office.fc.dom4j.Branch.content] method to allow users to modify the content of a
 * [com.wxiwei.office.fc.dom4j.Branch] directly using the [List] interface. This list is
 * backed by the branch such that changes to the list will be reflected in the
 * branch and changes to the branch will be reflected in this list.
 *
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.11 $
 */
class ContentListFacade(
    /** The branch which this lists is based on  */
    private val branch: AbstractBranch,
    /** The `List` used by the list  */
    val backingList: MutableList<Any?>
) : AbstractList<Any?>() {

    override fun add(element: Any?): Boolean {
        branch.childAdded(asNode(element))

        return backingList.add(element)
    }

    override fun add(index: Int, element: Any?) {
        branch.childAdded(asNode(element))
        backingList.add(index, element)
    }

    override fun set(index: Int, element: Any?): Any? {
        branch.childAdded(asNode(element))

        return backingList.set(index, element)
    }

    override fun remove(element: Any?): Boolean {
        branch.childRemoved(asNode(element))

        return backingList.remove(element)
    }

    override fun removeAt(index: Int): Any? {
        val `object` = backingList.removeAt(index)

        if (`object` != null) {
            branch.childRemoved(asNode(`object`))
        }

        return `object`
    }

    override fun addAll(elements: Collection<Any?>): Boolean {
        var count = backingList.size

        val iter = elements.iterator()
        while (iter.hasNext()) {
            add(iter.next())
            count++
        }

        return count == backingList.size
    }

    override fun addAll(index: Int, elements: Collection<Any?>): Boolean {
        var index = index
        var count = backingList.size

        val iter = elements.iterator()
        while (iter.hasNext()) {
            add(index++, iter.next())
            count--
        }

        return count == backingList.size
    }

    override fun clear() {
        val iter = iterator()
        while (iter.hasNext()) {
            val `object` = iter.next()
            branch.childRemoved(asNode(`object`))
        }

        backingList.clear()
    }

    override fun removeAll(elements: Collection<Any?>): Boolean {
        val iter = elements.iterator()
        while (iter.hasNext()) {
            val `object` = iter.next()
            branch.childRemoved(asNode(`object`))
        }

        return backingList.removeAll(elements)
    }

    override val size: Int
        get() = backingList.size

    override fun isEmpty(): Boolean {
        return backingList.isEmpty()
    }

    override fun contains(element: Any?): Boolean {
        return backingList.contains(element)
    }

    override fun containsAll(elements: Collection<Any?>): Boolean {
        return backingList.containsAll(elements)
    }

    override fun get(index: Int): Any? {
        return backingList[index]
    }

    override fun indexOf(element: Any?): Int {
        return backingList.indexOf(element)
    }

    override fun lastIndexOf(element: Any?): Int {
        return backingList.lastIndexOf(element)
    }

    protected fun asNode(`object`: Any?): Node {
        if (`object` is Node) {
            return `object`
        } else {
            throw IllegalAddException(
                ("This list must contain instances of "
                        + "Node. Invalid type: " + `object`)
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

