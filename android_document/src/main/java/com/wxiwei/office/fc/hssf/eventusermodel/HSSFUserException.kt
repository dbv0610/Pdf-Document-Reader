/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
package com.wxiwei.office.fc.hssf.eventusermodel

/**
 * 
 * This exception is provided as a way for API users to throw
 * exceptions from their event handling code. By doing so they
 * abort file processing by the HSSFEventFactory and by
 * catching it from outside the HSSFEventFactory.processEvents
 * method they can diagnose the cause for the abort.
 * 
 * 
 * The HSSFUserException supports a nested "reason"
 * throwable, i.e. an exception that caused this one to be thrown.
 * 
 * 
 * The HSSF package does not itself throw any of these
 * exceptions.
 * 
 * @author Rainer Klute (klute@rainer-klute.de)
 * @author Carey Sublette (careysub@earthling.net)
 * @version HSSFUserException.java,v 1.0
 * @since 2002-04-19
 */
class HSSFUserException : Exception {
    /**
     * 
     * Returns the [Throwable] that caused this exception to
     * be thrown or `null` if there was no such [ ].
     */
    var reason: Throwable? = null
        private set


    /**
     * 
     * Creates a new [HSSFUserException].
     */
    constructor() : super()


    /**
     * 
     * Creates a new [HSSFUserException] with a message
     * string.
     */
    constructor(msg: String?) : super(msg)


    /**
     * 
     * Creates a new [HSSFUserException] with a reason.
     */
    constructor(reason: Throwable?) : super() {
        this.reason = reason
    }


    /**
     * 
     * Creates a new [HSSFUserException] with a message string
     * and a reason.
     */
    constructor(msg: String?, reason: Throwable?) : super(msg) {
        this.reason = reason
    }
}
