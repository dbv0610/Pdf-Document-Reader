/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wxiwei.office.fc.codec

/**
 * 
 * Provides the highest level of abstraction for Encoders.
 * This is the sister interface of [Decoder].  Every implementation of
 * Encoder provides this common generic interface whic allows a user to pass a
 * generic Object to any Encoder implementation in the codec package.
 * 
 * @author Apache Software Foundation
 * @version $Id: Encoder.java 1075406 2011-02-28 16:18:26Z ggregory $
 */
interface Encoder {
    /**
     * Encodes an "Object" and returns the encoded content
     * as an Object.  The Objects here may just be `byte[]`
     * or `String`s depending on the implementation used.
     * 
     * @param source An object ot encode
     * 
     * @return An "encoded" Object
     * 
     * @throws EncoderException an encoder exception is
     * thrown if the encoder experiences a failure
     * condition during the encoding process.
     */
    @Throws(EncoderException::class)
    fun encode(source: Any?): Any?
}

