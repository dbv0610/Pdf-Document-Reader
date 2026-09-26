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
 * A logger interface that strives to make it as easy as possible for
 * developers to write log calls, while simultaneously making those
 * calls as cheap as possible by performing lazy evaluation of the log
 * message.<p>
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */
abstract class POILogger
/**
 * package scope so it cannot be instantiated outside of the util
 * package. You need a POILogger? Go to the POILogFactory for one
 */
internal constructor() {
    // no fields to initialise

    abstract fun initialize(cat: String?)

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     */
    abstract fun log(level: Int, obj1: Any?)

    /**
     * Log a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 The object to log.  This is converted to a string.
     * @param exception An exception to be logged
     */
    abstract fun log(level: Int, obj1: Any?, exception: Throwable?)

    /**
     * Check if a logger is enabled to log at the specified level
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     */
    abstract fun check(level: Int): Boolean

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?) {
        if (check(level)) {
            log(level, StringBuffer(32).append(obj1).append(obj2))
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?) {
        if (check(level)) {
            log(level, StringBuffer(48).append(obj1).append(obj2).append(obj3))
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?) {
        if (check(level)) {
            log(level, StringBuffer(64).append(obj1).append(obj2).append(obj3).append(obj4))
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?) {
        if (check(level)) {
            log(level, StringBuffer(80).append(obj1).append(obj2).append(obj3).append(obj4).append(obj5))
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?) {
        if (check(level)) {
            log(level, StringBuffer(96).append(obj1).append(obj2).append(obj3).append(obj4).append(obj5).append(obj6))
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     * @param obj7 seventh Object to place in the message
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, obj7: Any?) {
        if (check(level)) {
            log(level, StringBuffer(112).append(obj1).append(obj2).append(obj3).append(obj4).append(obj5).append(obj6).append(obj7))
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     * @param obj7 seventh Object to place in the message
     * @param obj8 eighth Object to place in the message
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, obj7: Any?, obj8: Any?) {
        if (check(level)) {
            log(level, StringBuffer(128).append(obj1).append(obj2).append(obj3).append(obj4).append(obj5).append(obj6).append(obj7).append(obj8))
        }
    }

    /**
     * Log an exception, without a message
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param exception An exception to be logged
     */
    open fun log(level: Int, exception: Throwable?) {
        log(level, null, exception)
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param exception An exception to be logged
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, exception: Throwable?) {
        if (check(level)) {
            log(level, StringBuffer(32).append(obj1).append(obj2), exception)
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param exception An exception to be logged
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, exception: Throwable?) {
        if (check(level)) {
            log(level, StringBuffer(48).append(obj1).append(obj2).append(obj3), exception)
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param exception An exception to be logged
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, exception: Throwable?) {
        if (check(level)) {
            log(level, StringBuffer(64).append(obj1).append(obj2).append(obj3).append(obj4), exception)
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param exception An exception to be logged
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, exception: Throwable?) {
        if (check(level)) {
            log(level, StringBuffer(80).append(obj1).append(obj2).append(obj3).append(obj4).append(obj5), exception)
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     * @param exception An exception to be logged
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, exception: Throwable?) {
        if (check(level)) {
            log(level, StringBuffer(96).append(obj1).append(obj2).append(obj3).append(obj4).append(obj5).append(obj6), exception)
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     * @param obj7 seventh Object to place in the message
     * @param exception An exception to be logged
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, obj7: Any?, exception: Throwable?) {
        if (check(level)) {
            log(level, StringBuffer(112).append(obj1).append(obj2).append(obj3).append(obj4).append(obj5).append(obj6).append(obj7), exception)
        }
    }

    /**
     * Log a message. Lazily appends Object parameters together.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param obj1 first Object to place in the message
     * @param obj2 second Object to place in the message
     * @param obj3 third Object to place in the message
     * @param obj4 fourth Object to place in the message
     * @param obj5 fifth Object to place in the message
     * @param obj6 sixth Object to place in the message
     * @param obj7 seventh Object to place in the message
     * @param obj8 eighth Object to place in the message
     * @param exception An exception to be logged
     */
    open fun log(level: Int, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?, obj5: Any?, obj6: Any?, obj7: Any?, obj8: Any?, exception: Throwable?) {
        if (check(level)) {
            log(level, StringBuffer(128).append(obj1).append(obj2).append(obj3).append(obj4).append(obj5).append(obj6).append(obj7).append(obj8), exception)
        }
    }

    /**
     * Logs a formated message. The message itself may contain %
     * characters as place holders. This routine will attempt to match
     * the placeholder by looking at the type of parameter passed to
     * obj1.<p>
     *
     * If the parameter is an array, it traverses the array first and
     * matches parameters sequentially against the array items.
     * Otherwise the parameters after `message` are matched
     * in order.<p>
     *
     * If the place holder matches against a number it is printed as a
     * whole number. This can be overridden by specifying a precision
     * in the form %n.m where n is the padding for the whole part and
     * m is the number of decimal places to display. n can be excluded
     * if desired. n and m may not be more than 9.<p>
     *
     * If the last parameter (after flattening) is a Throwable it is
     * logged specially.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param message The message to log.
     * @param obj1 The first object to match against.
     */
    open fun logFormatted(level: Int, message: String?, obj1: Any?) {
        commonLogFormatted(level, message, arrayOf(obj1))
    }

    /**
     * Logs a formated message. The message itself may contain %
     * characters as place holders. This routine will attempt to match
     * the placeholder by looking at the type of parameter passed to
     * obj1.<p>
     *
     * If the parameter is an array, it traverses the array first and
     * matches parameters sequentially against the array items.
     * Otherwise the parameters after `message` are matched
     * in order.<p>
     *
     * If the place holder matches against a number it is printed as a
     * whole number. This can be overridden by specifying a precision
     * in the form %n.m where n is the padding for the whole part and
     * m is the number of decimal places to display. n can be excluded
     * if desired. n and m may not be more than 9.<p>
     *
     * If the last parameter (after flattening) is a Throwable it is
     * logged specially.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param message The message to log.
     * @param obj1 The first object to match against.
     * @param obj2 The second object to match against.
     */
    open fun logFormatted(level: Int, message: String?, obj1: Any?, obj2: Any?) {
        commonLogFormatted(level, message, arrayOf(obj1, obj2))
    }

    /**
     * Logs a formated message. The message itself may contain %
     * characters as place holders. This routine will attempt to match
     * the placeholder by looking at the type of parameter passed to
     * obj1.<p>
     *
     * If the parameter is an array, it traverses the array first and
     * matches parameters sequentially against the array items.
     * Otherwise the parameters after `message` are matched
     * in order.<p>
     *
     * If the place holder matches against a number it is printed as a
     * whole number. This can be overridden by specifying a precision
     * in the form %n.m where n is the padding for the whole part and
     * m is the number of decimal places to display. n can be excluded
     * if desired. n and m may not be more than 9.<p>
     *
     * If the last parameter (after flattening) is a Throwable it is
     * logged specially.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param message The message to log.
     * @param obj1 The first object to match against.
     * @param obj2 The second object to match against.
     * @param obj3 The third object to match against.
     */
    open fun logFormatted(level: Int, message: String?, obj1: Any?, obj2: Any?, obj3: Any?) {
        commonLogFormatted(level, message, arrayOf(obj1, obj2, obj3))
    }

    /**
     * Logs a formated message. The message itself may contain %
     * characters as place holders. This routine will attempt to match
     * the placeholder by looking at the type of parameter passed to
     * obj1.<p>
     *
     * If the parameter is an array, it traverses the array first and
     * matches parameters sequentially against the array items.
     * Otherwise the parameters after `message` are matched
     * in order.<p>
     *
     * If the place holder matches against a number it is printed as a
     * whole number. This can be overridden by specifying a precision
     * in the form %n.m where n is the padding for the whole part and
     * m is the number of decimal places to display. n can be excluded
     * if desired. n and m may not be more than 9.<p>
     *
     * If the last parameter (after flattening) is a Throwable it is
     * logged specially.
     *
     * @param level One of DEBUG, INFO, WARN, ERROR, FATAL
     * @param message The message to log.
     * @param obj1 The first object to match against.
     * @param obj2 The second object to match against.
     * @param obj3 The third object to match against.
     * @param obj4 The fourth object to match against.
     */
    open fun logFormatted(level: Int, message: String?, obj1: Any?, obj2: Any?, obj3: Any?, obj4: Any?) {
        commonLogFormatted(level, message, arrayOf(obj1, obj2, obj3, obj4))
    }

    private fun commonLogFormatted(level: Int, message: String?, unflatParams: Array<Any?>) {
        if (check(level)) {
            val params = flattenArrays(unflatParams)

            if (params[params.size - 1] is Throwable) {
                log(level, StringUtil.format(message, params), params[params.size - 1] as Throwable)
            } else {
                log(level, StringUtil.format(message, params))
            }
        }
    }

    /**
     * Flattens any contained objects. Only tranverses one level deep.
     */
    private fun flattenArrays(objects: Array<Any?>): Array<Any?> {
        val results: MutableList<Any?> = ArrayList()

        for (i in objects.indices) {
            results.addAll(objectToObjectArray(objects[i]))
        }
        return results.toTypedArray()
    }

    private fun objectToObjectArray(`object`: Any?): List<Any?> {
        val results: MutableList<Any?> = ArrayList()

        if (`object` is ByteArray) {
            val array = `object`

            for (j in array.indices) {
                results.add(java.lang.Byte.valueOf(array[j]))
            }
        }
        if (`object` is CharArray) {
            val array = `object`

            for (j in array.indices) {
                results.add(Character.valueOf(array[j]))
            }
        } else if (`object` is ShortArray) {
            val array = `object`

            for (j in array.indices) {
                results.add(java.lang.Short.valueOf(array[j]))
            }
        } else if (`object` is IntArray) {
            val array = `object`

            for (j in array.indices) {
                results.add(Integer.valueOf(array[j]))
            }
        } else if (`object` is LongArray) {
            val array = `object`

            for (j in array.indices) {
                results.add(java.lang.Long.valueOf(array[j]))
            }
        } else if (`object` is FloatArray) {
            val array = `object`

            for (j in array.indices) {
                results.add(java.lang.Float.valueOf(array[j]))
            }
        } else if (`object` is DoubleArray) {
            val array = `object`

            for (j in array.indices) {
                results.add(java.lang.Double.valueOf(array[j]))
            }
        } else if (`object` is Array<*>) {
            val array = `object`

            for (j in array.indices) {
                results.add(array[j])
            }
        } else {
            results.add(`object`)
        }
        return results
    }

    companion object {
        @JvmField
        var DEBUG = 1

        @JvmField
        var INFO = 3

        @JvmField
        var WARN = 5

        @JvmField
        var ERROR = 7

        @JvmField
        var FATAL = 9
    }
}
