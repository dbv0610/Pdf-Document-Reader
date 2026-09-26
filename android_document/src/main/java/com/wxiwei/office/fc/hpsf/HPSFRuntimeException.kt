/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hpsf

import java.io.PrintStream
import java.io.PrintWriter

/**
 * 
 * This exception is the superclass of all other unchecked
 * exceptions thrown in this package. It supports a nested "reason"
 * throwable, i.e. an exception that caused this one to be thrown.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
open class HPSFRuntimeException : RuntimeException {
    /**
     * 
     * Returns the [Throwable] that caused this exception to
     * be thrown or `null` if there was no such [ ].
     * 
     * @return The reason
     */
    /** 
     *
     *The underlying reason for this exception - may be
     * `null`.  */
    var reason: Throwable? = null
        private set


    /**
     * 
     * Creates a new [HPSFRuntimeException].
     */
    constructor() : super()


    /**
     * 
     * Creates a new [HPSFRuntimeException] with a message
     * string.
     * 
     * @param msg The message string.
     */
    constructor(msg: String?) : super(msg)


    /**
     * 
     * Creates a new [HPSFRuntimeException] with a
     * reason.
     * 
     * @param reason The reason, i.e. a throwable that indirectly
     * caused this exception.
     */
    constructor(reason: Throwable?) : super() {
        this.reason = reason
    }


    /**
     * 
     * Creates a new [HPSFRuntimeException] with a message
     * string and a reason.
     * 
     * @param msg The message string.
     * @param reason The reason, i.e. a throwable that indirectly
     * caused this exception.
     */
    constructor(msg: String?, reason: Throwable?) : super(msg) {
        this.reason = reason
    }


    /**
     * @see Throwable.printStackTrace
     */
    override fun printStackTrace() {
        printStackTrace(System.err)
    }


    /**
     * @see Throwable.printStackTrace
     */
    override fun printStackTrace(p: PrintStream) {
        val reason = this.reason
        super.printStackTrace(p)
        if (reason != null) {
            p.println("Caused by:")
            reason.printStackTrace(p)
        }
    }


    /**
     * @see Throwable.printStackTrace
     */
    override fun printStackTrace(p: PrintWriter) {
        val reason = this.reason
        super.printStackTrace(p)
        if (reason != null) {
            p.println("Caused by:")
            reason.printStackTrace(p)
        }
    }
}
