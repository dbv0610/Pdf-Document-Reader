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
 * This exception is thrown if a certain type of property set is
 * expected (e.g. a Document Summary Information) but the provided
 * property set is not of that type.
 * 
 * 
 * The constructors of this class are analogous to those of its
 * superclass and documented there.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
class UnexpectedPropertySetTypeException : HPSFException {
    /**
     * 
     * Creates an [UnexpectedPropertySetTypeException].
     */
    constructor() : super()


    /**
     * 
     * Creates an [UnexpectedPropertySetTypeException] with a message
     * string.
     * 
     * @param msg The message string.
     */
    constructor(msg: String?) : super(msg)


    /**
     * 
     * Creates a new [UnexpectedPropertySetTypeException] with a
     * reason.
     * 
     * @param reason The reason, i.e. a throwable that indirectly
     * caused this exception.
     */
    constructor(reason: Throwable?) : super(reason)


    /**
     * 
     * Creates an [UnexpectedPropertySetTypeException] with a message
     * string and a reason.
     * 
     * @param msg The message string.
     * @param reason The reason, i.e. a throwable that indirectly
     * caused this exception.
     */
    constructor(
        msg: String?,
        reason: Throwable?
    ) : super(msg, reason)
}
