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
package com.wxiwei.office.fc.hslf.model.textproperties

/**
 * Definition for the common character text property bitset, which
 * handles bold/italic/underline etc.
 */
class CharFlagsTextProp : BitMaskTextProp(
    2, 0xffff, "char_flags",
    arrayOf("bold", "italic", "underline", "unused1", "shadow", "fehint", "unused2", "komi", "strikethrough", "emboss", "pp9rt_1", "pp9rt_2", "pp9rt_3")
) {
    companion object {
        const val BOLD_IDX: Int = 0
        const val ITALIC_IDX: Int = 1
        const val UNDERLINE_IDX: Int = 2
        const val SHADOW_IDX: Int = 4
        const val STRIKETHROUGH_IDX: Int = 8
        const val RELIEF_IDX: Int = 9
        const val RESET_NUMBERING_IDX: Int = 10
        const val ENABLE_NUMBERING_1_IDX: Int = 11
        const val ENABLE_NUMBERING_2_IDX: Int = 12

        const val NAME: String = "char_flags"
    }
}
