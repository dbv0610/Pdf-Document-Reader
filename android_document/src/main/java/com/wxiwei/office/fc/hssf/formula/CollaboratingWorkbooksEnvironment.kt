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
package com.wxiwei.office.fc.hssf.formula

import java.util.IdentityHashMap

/**
 * Manages a collection of [WorkbookEvaluator]s, in order to support evaluation of formulas
 * across spreadsheets.
 *
 *
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
class CollaboratingWorkbooksEnvironment {
    class WorkbookNotFoundException internal constructor(msg: String?) : Exception(msg)

    private val _evaluatorsByName: MutableMap<String?, WorkbookEvaluator?>
    private val _evaluators: Array<WorkbookEvaluator?>

    private var _unhooked = false

    private constructor() {
        _evaluatorsByName = mutableMapOf<String?, WorkbookEvaluator?>()
        _evaluators = arrayOfNulls<WorkbookEvaluator>(0)
    }

    private constructor(
        workbookNames: Array<String?>,
        evaluators: Array<WorkbookEvaluator?>,
        nItems: Int
    ) {
        val m: MutableMap<String?, WorkbookEvaluator?> =
            HashMap<String?, WorkbookEvaluator?>(nItems * 3 / 2)
        val uniqueEvals = IdentityHashMap<WorkbookEvaluator?, String>(nItems * 3 / 2)
        for (i in 0..<nItems) {
            val wbName = workbookNames[i]
            val wbEval = evaluators[i]
            require(!m.containsKey(wbName)) { "Duplicate workbook name '" + wbName + "'" }
            if (uniqueEvals.containsKey(wbEval)) {
                val msg = ("Attempted to register same workbook under names '"
                        + uniqueEvals.get(wbEval) + "' and '" + wbName + "'")
                throw IllegalArgumentException(msg)
            }
            uniqueEvals.put(wbEval, wbName)
            m.put(wbName, wbEval)
        }
        unhookOldEnvironments(evaluators)
        hookNewEnvironment(evaluators, this)
        _unhooked = false
        _evaluators = evaluators
        _evaluatorsByName = m
    }

    /**
     * Completely dismantles all workbook environments that the supplied evaluators are part of
     */
    private fun unhookOldEnvironments(evaluators: Array<WorkbookEvaluator?>) {
        val oldEnvs: MutableSet<CollaboratingWorkbooksEnvironment?> =
            HashSet<CollaboratingWorkbooksEnvironment?>()
        for (i in evaluators.indices) {
            oldEnvs.add(evaluators[i]!!.environment)
        }
        val oldCWEs = oldEnvs.toTypedArray()
        for (i in oldCWEs.indices) {
            oldCWEs[i]!!.unhook()
        }
    }

    /**
     * Tell all contained evaluators that this environment should be discarded
     */
    private fun unhook() {
        if (_evaluators.size < 1) {
            // Never dismantle the EMPTY environment
            return
        }
        for (i in _evaluators.indices) {
            _evaluators[i]!!.detachFromEnvironment()
        }
        _unhooked = true
    }

    @Throws(WorkbookNotFoundException::class)
    fun getWorkbookEvaluator(workbookName: String?): WorkbookEvaluator {
        check(!_unhooked) { "This environment has been unhooked" }
        val result = _evaluatorsByName.get(workbookName)
        if (result == null) {
            val sb = StringBuffer(256)
            sb.append("Could not resolve external workbook name '").append(workbookName)
                .append("'.")
            if (_evaluators.size < 1) {
                sb.append(" Workbook environment has not been set up.")
            } else {
                sb.append(" The following workbook names are valid: (")
                val i = _evaluatorsByName.keys.iterator()
                var count = 0
                while (i.hasNext()) {
                    if (count++ > 0) {
                        sb.append(", ")
                    }
                    sb.append("'").append(i.next()).append("'")
                }
                sb.append(")")
            }
            throw WorkbookNotFoundException(sb.toString())
        }
        return result
    }

    companion object {
        val EMPTY: CollaboratingWorkbooksEnvironment = CollaboratingWorkbooksEnvironment()

        fun setup(workbookNames: Array<String?>, evaluators: Array<WorkbookEvaluator?>) {
            val nItems = workbookNames.size
            require(evaluators.size == nItems) {
                ("Number of workbook names is " + nItems
                        + " but number of evaluators is " + evaluators.size)
            }
            require(nItems >= 1) { "Must provide at least one collaborating worbook" }
            CollaboratingWorkbooksEnvironment(workbookNames, evaluators, nItems)
        }

        private fun hookNewEnvironment(
            evaluators: Array<WorkbookEvaluator?>,
            env: CollaboratingWorkbooksEnvironment
        ) {
            // All evaluators will need to share the same cache.
            // but the cache takes an optional evaluation listener.

            val nItems = evaluators.size
            val evalListener = evaluators[0]!!.evaluationListener
            // make sure that all evaluators have the same listener
            for (i in 0..<nItems) {
                if (evalListener !== evaluators[i]!!.evaluationListener) {
                    // This would be very complex to support
                    throw RuntimeException("Workbook evaluators must all have the same evaluation listener")
                }
            }
            val cache = EvaluationCache(evalListener)

            for (i in 0..<nItems) {
                evaluators[i]!!.attachToEnvironment(env, cache, i)
            }
        }
    }
}
