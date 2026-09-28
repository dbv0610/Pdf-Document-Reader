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

/**
 * 
 * 
 * `FilterIterator` is an abstract base class which is useful for
 * implementors of [Iterator]which filter an existing iterator.
 * 
 * 
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @version $Revision: 1.10 $
 * 
 */
@Deprecated("THIS CLASS WILL BE REMOVED IN dom4j-1.6 !!")
abstract class FilterIterator(protected var proxy: MutableIterator<*>?) : MutableIterator<Any?> {
    private var next: Any? = null

    private var first = true

    override fun hasNext(): Boolean {
        if (first) {
            next = findNext()
            first = false
        }

        return next != null
    }

    @Throws(NoSuchElementException::class)
    override fun next(): Any? {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        val answer = this.next
        this.next = findNext()

        return answer
    }

    /**
     * Always throws UnsupportedOperationException as this class does look-ahead
     * with its internal iterator.
     * 
     * @throws UnsupportedOperationException
     * always
     */
    override fun remove() {
        throw UnsupportedOperationException()
    }

    /**
     * Filter method to perform some matching on the given element.
     * 
     * @param element
     * DOCUMENT ME!
     * 
     * @return true if the given element matches the filter and should be appear
     * in the iteration
     */
    protected abstract fun matches(element: Any?): Boolean

    protected fun findNext(): Any? {
        if (proxy != null) {
            while (proxy!!.hasNext()) {
                val nextObject = proxy!!.next()

                if ((nextObject != null) && matches(nextObject)) {
                    return nextObject
                }
            }

            proxy = null
        }

        return null
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

