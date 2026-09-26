/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.io

import com.wxiwei.office.fc.dom4j.ElementHandler
import com.wxiwei.office.fc.dom4j.ElementPath

/**
 * 
 * 
 * `DispatchHandler` implements the `ElementHandler`
 * interface and provides a means to register multiple
 * `ElementHandler` instances to be used by an event based
 * processor. This is a special `ElementHandler` in that it's
 * **onStart ** and **onEnd ** implementation methods are called for every
 * element encountered during the parse. It then delegates to other
 * `ElementHandler` instances registered with it to process the
 * elements encountered.
 * 
 * 
 * @author [Dave White ](mailto:dwhite@equipecom.com)
 * @version $Revision: 1.11 $
 */
open class DispatchHandler : ElementHandler {
    /** Whether the parser is at the root element or not  */
    private var atRoot = true

    /**
     * DOCUMENT ME!
     * 
     * @return the current path for the parse
     */
    /** The current path in the XML tree (i.e. /a/b/c)  */
    var path: String? = "/"
        private set

    /** maintains a stack of previously encountered paths  */
    private val pathStack: ArrayList<String?>

    /** maintains a stack of previously encountered handlers  */
    private val handlerStack: ArrayList<ElementHandler?>

    /**
     * `HashMap` maintains the mapping between element paths and
     * handlers
     */
    private val handlers: HashMap<String?, ElementHandler?>?

    /**
     * `ElementHandler` to use by default for element paths with no
     * handlers registered
     */
    private var defaultHandler: ElementHandler? = null

    init {
        pathStack = ArrayList<String?>()
        handlerStack = ArrayList<ElementHandler?>()
        handlers = HashMap<String?, ElementHandler?>()
    }

    /**
     * Adds the `ElementHandler` to be called when the specified
     * path is encounted.
     * 
     * @param handlerPath
     * is the path to be handled
     * @param handler
     * is the `ElementHandler` to be called by the event
     * based processor.
     */
    fun addHandler(handlerPath: String?, handler: ElementHandler?) {
        handlers!!.put(handlerPath, handler)
    }

    /**
     * Removes the `ElementHandler` from the event based processor,
     * for the specified path.
     * 
     * @param handlerPath
     * is the path to remove the `ElementHandler` for.
     * 
     * @return DOCUMENT ME!
     */
    fun removeHandler(handlerPath: String?): ElementHandler? {
        return handlers!!.remove(handlerPath) as ElementHandler?
    }

    /**
     * DOCUMENT ME!
     * 
     * @param handlerPath
     * DOCUMENT ME!
     * 
     * @return true when an `ElementHandler` is registered for the
     * specified path.
     */
    fun containsHandler(handlerPath: String?): Boolean {
        return handlers!!.containsKey(handlerPath)
    }

    /**
     * Get the registered [ElementHandler]for the specified path.
     * 
     * @param handlerPath
     * XML path to get the handler for
     * 
     * @return the registered handler
     */
    fun getHandler(handlerPath: String?): ElementHandler? {
        return handlers!!.get(handlerPath) as ElementHandler?
    }

    val activeHandlerCount: Int
        /**
         * Returns the number of [ElementHandler]objects that are waiting for
         * their elements closing tag.
         * 
         * @return number of active handlers
         */
        get() = handlerStack.size

    /**
     * When multiple `ElementHandler` instances have been
     * registered, this will set a default `ElementHandler` to be
     * called for any path which does **NOT ** have a handler registered.
     * 
     * @param handler
     * is the `ElementHandler` to be called by the event
     * based processor.
     */
    fun setDefaultHandler(handler: ElementHandler?) {
        defaultHandler = handler
    }

    /**
     * Used to remove all the Element Handlers and return things back to the way
     * they were when object was created.
     */
    fun resetHandlers() {
        atRoot = true
        path = "/"
        pathStack.clear()
        handlerStack.clear()
        handlers!!.clear()
        defaultHandler = null
    }

    // The following methods implement the ElementHandler interface
    override fun onStart(elementPath: ElementPath?) {
        val element = elementPath!!.current!!

        // Save the location of the last (i.e. parent) path
        pathStack.add(path)

        // Calculate the new path
        if (atRoot) {
            path = path + element.name
            atRoot = false
        } else {
            path = path + "/" + element.name
        }

        if ((handlers != null) && (handlers.containsKey(path))) {
            // The current node has a handler associated with it.
            // Find the handler and save it on the handler stack.
            val handler = handlers.get(path) as ElementHandler?
            handlerStack.add(handler)

            // Call the handlers onStart method.
            handler!!.onStart(elementPath)
        } else {
            // No handler is associated with this node, so use the
            // defaultHandler it it exists.
            if (handlerStack.isEmpty() && (defaultHandler != null)) {
                defaultHandler!!.onStart(elementPath)
            }
        }
    }

    override fun onEnd(elementPath: ElementPath?) {
        if ((handlers != null) && (handlers.containsKey(path))) {
            // This node has a handler associated with it.
            // Find the handler and pop it from the handler stack.
            val handler = handlers.get(path) as ElementHandler?
            handlerStack.removeAt(handlerStack.size - 1)

            // Call the handlers onEnd method
            handler!!.onEnd(elementPath)
        } else {
            // No handler is associated with this node, so use the
            // defaultHandler it it exists.
            if (handlerStack.isEmpty() && (defaultHandler != null)) {
                defaultHandler!!.onEnd(elementPath)
            }
        }

        // Set path back to its parent
        path = pathStack.removeAt(pathStack.size - 1) as String?

        if (pathStack.size == 0) {
            atRoot = true
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

