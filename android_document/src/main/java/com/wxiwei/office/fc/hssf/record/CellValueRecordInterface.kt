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
package com.wxiwei.office.fc.hssf.record


/**
 * The cell value record interface is implemented by all classes of type Record that
 * contain cell values.  It allows the containing sheet to move through them and compare
 * them.
 * 
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * 
 * @see Record
 * 
 * @see RecordFactory
 */
interface CellValueRecordInterface {
    /**
     * @return the row this cell occurs on
     */
    /**
     * @param row the row this cell occurs within
     */
    var row: Int

    /**
     * @return the column this cell defines within the row
     */
    /**
     * @param col the column this cell defines
     */
    var column: Short

    var xFIndex: Short
}
