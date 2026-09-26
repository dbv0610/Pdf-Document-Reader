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
 * Provides logging without clients having to mess with
 * configuration/initialization.
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */
class POILogFactory
/**
 * Construct a POILogFactory.
 */
private constructor() {

    companion object {
        /**
         * Map of POILogger instances, with classes as keys
         */
        private val _loggers: MutableMap<String?, POILogger> = HashMap()

        /**
         * A common instance of NullLogger, as it does nothing
         *  we only need the one
         */
        private val _nullLogger: POILogger = NullLogger()

        /**
         * The name of the class to use. Initialised the
         *  first time we need it
         */
        private var _loggerClassName: String? = null

        /**
         * Get a logger, based on a class name
         *
         * @param theclass the class whose name defines the log
         *
         * @return a POILogger for the specified class
         */
        @JvmStatic
        fun getLogger(theclass: Class<*>): POILogger {
            return getLogger(theclass.getName())
        }

        /**
         * Get a logger, based on a String
         *
         * @param cat the String that defines the log
         *
         * @return a POILogger for the specified class
         */
        @JvmStatic
        fun getLogger(cat: String?): POILogger {
            var logger: POILogger? = null

            // If we haven't found out what logger to use yet,
            //  then do so now
            // Don't look it up until we're first asked, so
            //  that our users can set the system property
            //  between class loading and first use
            if (_loggerClassName == null) {
                try {
                    _loggerClassName = System.getProperty("org.apache.poi.util.POILogger")
                } catch (e: Exception) {
                }

                // Use the default logger if none specified,
                //  or none could be fetched
                if (_loggerClassName == null) {
                    _loggerClassName = _nullLogger.javaClass.getName()
                }
            }

            // Short circuit for the null logger, which
            //  ignores all categories
            if (_loggerClassName == _nullLogger.javaClass.getName()) {
                return _nullLogger
            }

            // Fetch the right logger for them, creating
            //  it if that's required
            if (_loggers.containsKey(cat)) {
                logger = _loggers[cat]
            } else {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val loggerClass = Class.forName(_loggerClassName!!) as Class<out POILogger>
                    logger = loggerClass.newInstance()
                    logger!!.initialize(cat)
                } catch (e: Exception) {
                    // Give up and use the null logger
                    logger = _nullLogger
                }

                // Save for next time
                _loggers[cat] = logger!!
            }
            return logger!!
        }
    }
} // end public class POILogFactory
