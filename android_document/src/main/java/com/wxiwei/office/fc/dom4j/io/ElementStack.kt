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
 * 
 * 
 * `ElementStack` is used internally inside the [ ] to maintain a stack of [Element]instances. It opens
 * an integration possibility allowing derivations to prune the tree when a node
 * is complete.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.14 $
 */
open class ElementStack @JvmOverloads constructor(defaultCapacity: Int = 50) :
    ElementPath {
    /** stack of `Element` objects  */
    protected var stack: Array<Element?>

    /** index of the item at the top of the stack or -1 if the stack is empty  */
    protected var lastElementIndex: Int = -1

    var dispatchHandler: DispatchHandler? = null

    init {
        stack = arrayOfNulls<Element>(defaultCapacity)
    }

    /**
     * Peeks at the top element on the stack without changing the contents of
     * the stack.
     */
    fun clear() {
        lastElementIndex = -1
    }

    /**
     * Peeks at the top element on the stack without changing the contents of
     * the stack.
     * 
     * @return the current element on the stack
     */
    fun peekElement(): Element? {
        if (lastElementIndex < 0) {
            return null
        }

        return stack[lastElementIndex]
    }

    /**
     * Pops the element off the stack
     * 
     * @return the element that has just been popped off the stack
     */
    open fun popElement(): Element? {
        if (lastElementIndex < 0) {
            return null
        }

        return stack[lastElementIndex--]
    }

    /**
     * Pushes a new element onto the stack
     * 
     * @param element
     * DOCUMENT ME!
     */
    fun pushElement(element: Element?) {
        val length = stack.size

        if (++lastElementIndex >= length) {
            reallocate(length * 2)
        }

        stack[lastElementIndex] = element
    }

    /**
     * Reallocates the stack to the given size
     * 
     * @param size
     * DOCUMENT ME!
     */
    protected fun reallocate(size: Int) {
        val oldStack = stack
        stack = arrayOfNulls<Element>(size)
        System.arraycopy(oldStack, 0, stack, 0, oldStack.size)
    }

    // The ElementPath Interface
    //
    override fun size(): Int {
        return lastElementIndex + 1
    }

    override fun getElement(depth: Int): Element? {
        var element: Element?

        try {
            element = stack[depth]
        } catch (e: ArrayIndexOutOfBoundsException) {
            element = null
        }

        return element
    }

    override val path: String?
        get() {
            if (this.dispatchHandler == null) {
                this.dispatchHandler = DispatchHandler()
            }

            return dispatchHandler!!.path
        }

    override val current: Element?
        get() = peekElement()

    override fun addHandler(path: String?, elementHandler: ElementHandler?) {
        this.dispatchHandler!!.addHandler(getHandlerPath(path!!), elementHandler)
    }

    override fun removeHandler(path: String?) {
        this.dispatchHandler!!.removeHandler(getHandlerPath(path!!))
    }

    /**
     * DOCUMENT ME!
     * 
     * @param path
     * DOCUMENT ME!
     * 
     * @return true when an `ElementHandler` is registered for the
     * specified path.
     */
    fun containsHandler(path: String?): Boolean {
        return this.dispatchHandler!!.containsHandler(path)
    }

    private fun getHandlerPath(path: String): String {
        val handlerPath: String

        if (this.dispatchHandler == null) {
            this.dispatchHandler = DispatchHandler()
        }

        if (path.startsWith("/")) {
            handlerPath = path
        } else if (this.path == "/") {
            handlerPath = this.path + path
        } else {
            handlerPath = this.path + "/" + path
        }

        return handlerPath
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

