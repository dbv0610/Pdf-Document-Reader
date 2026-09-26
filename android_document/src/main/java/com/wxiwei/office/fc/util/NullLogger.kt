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

package com.wxiwei.office.fc.util

/**
 * A logger class that strives to make it as easy as possible for
 * developers to write log calls, while simultaneously making those
 * calls as cheap as possible by performing lazy evaluation of the log
 * message.<p>
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */
open class NullLogger : POILogger() {
    override fun initialize(cat: String?) {
        //do nothing
    }

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.
     */
    override fun log(level: Int, obj1: Any?) {
        //do nothing
    }

    /**
     * Check if a logger is enabled to log at the specified level
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     */
    override fun check(level: Int): Boolean {
        return false
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, obj7: Any?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, obj7: Any?, obj8: Any?) {
        //do nothing
    }

    /**
     * Log a message
     */
    override fun log(level: Int, obj1: Any?, exception: Throwable?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, exception: Throwable?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, exception: Throwable?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, exception: Throwable?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, exception: Throwable?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, exception: Throwable?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, obj7: Any?, exception: Throwable?) {
        //do nothing
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     */
    override fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, obj7: Any?, obj8: Any?, exception: Throwable?) {
        //do nothing
    }

    /**
     * Logs a formated message.
     */
    override fun logFormatted(level: Int, message: String?, obj1: Any?) {
        //do nothing
    }

    /**
     * Logs a formated message.
     */
    override fun logFormatted(level: Int, message: String?, obj1: Any?, obj2: Any?) {
        //do nothing
    }

    /**
     * Logs a formated message.
     */
    override fun logFormatted(level: Int, message: String?, obj1: Any?, obj2: Any?, obj3: Any?) {
        //do nothing
    }

    /**
     * Logs a formated message.
     */
    override fun logFormatted(level: Int, message: String?, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?) {
        //do nothing
    }
}
