/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j

/**
 * This interface is used by [ElementHandler]instances to retrieve
 * information about the current path hierarchy they are to process. It's
 * primary use is to retrieve the current [Element]being processed.
 * 
 * @author [Dave White ](mailto:dwhite@equipecom.com)
 * @version $Revision: 1.6 $
 */
interface ElementPath {
    /**
     * DOCUMENT ME!
     * 
     * @return the number of elements in the path
     */
    fun size(): Int

    /**
     * DOCUMENT ME!
     * 
     * @param depth
     * DOCUMENT ME!
     * 
     * @return the element at the specified depth index, 0 = root element
     */
    fun getElement(depth: Int): Element?

    /**
     * DOCUMENT ME!
     * 
     * @return the path as a string
     */
    val path: String?

    /**
     * DOCUMENT ME!
     * 
     * @return the current element
     */
    val current: Element?

    /**
     * Adds the `ElementHandler` to be called when the specified
     * path is encounted. The path can be either an absolute path (i.e. prefixed
     * with "/") or a relative path (i.e. assummed to be a child of the current
     * path as retrieved by **getPath **.
     * 
     * @param path
     * is the path to be handled
     * @param handler
     * is the `ElementHandler` to be called by the event
     * based processor.
     */
    fun addHandler(path: String?, handler: ElementHandler?)

    /**
     * Removes the `ElementHandler` from the event based processor,
     * for the specified path. The path can be either an absolute path (i.e.
     * prefixed with "/") or a relative path (i.e. assummed to be a child of the
     * current path as retrieved by **getPath **.
     * 
     * @param path
     * is the path to remove the `ElementHandler` for.
     */
    fun removeHandler(path: String?)
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

