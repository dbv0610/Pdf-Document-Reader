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
package com.wxiwei.office.fc.ddf

/**
 * Defines the constants for the various possible shape paths.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class EscherShapePathProperty(propertyNumber: Short, shapePath: Int) :
    EscherSimpleProperty(propertyNumber, false, false, shapePath) {
    companion object {
        const val LINE_OF_STRAIGHT_SEGMENTS: Int = 0
        const val CLOSED_POLYGON: Int = 1
        const val CURVES: Int = 2
        const val CLOSED_CURVES: Int = 3
        const val COMPLEX: Int = 4
    }
}
