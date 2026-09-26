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

/**
 * 
 * This exception is thrown when there is an illegal value set in a
 * [PropertySet]. For example, a [Variant.VT_BOOL] must
 * have a value of `-1 (true)` or `0 (false)`.
 * Any other value would trigger this exception. It supports a nested
 * "reason" throwable, i.e. an exception that caused this one to be
 * thrown.
 * 
 * @author Drew Varner(Drew.Varner atDomain sc.edu)
 */
class IllegalPropertySetDataException : HPSFRuntimeException {
    /**
     * 
     * Constructor
     */
    constructor() : super()


    /**
     * 
     * Constructor
     * 
     * @param msg The exception's message string
     */
    constructor(msg: String?) : super(msg)


    /**
     * 
     * Constructor
     * 
     * @param reason This exception's underlying reason
     */
    constructor(reason: Throwable?) : super(reason)


    /**
     * 
     * Constructor
     * 
     * @param msg The exception's message string
     * @param reason This exception's underlying reason
     */
    constructor(
        msg: String?,
        reason: Throwable?
    ) : super(msg, reason)
}
