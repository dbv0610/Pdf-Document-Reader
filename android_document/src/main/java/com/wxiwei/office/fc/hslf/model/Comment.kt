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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.fc.hslf.record.Comment2000

/**
 * 
 * @author Nick Burch
 */
class Comment
    (protected val comment2000: Comment2000) {
    var author: String?
        /**
         * Get the Author of this comment
         */
        get() = comment2000.author
        /**
         * Set the Author of this comment
         */
        set(author) {
            comment2000.author = author
        }

    var authorInitials: String?
        /**
         * Get the Author's Initials of this comment
         */
        get() = comment2000.authorInitials
        /**
         * Set the Author's Initials of this comment
         */
        set(initials) {
            comment2000.authorInitials = initials
        }

    var text: String?
        /**
         * Get the text of this comment
         */
        get() = comment2000.text
        /**
         * Set the text of this comment
         */
        set(text) {
            comment2000.text = text
        }
}
