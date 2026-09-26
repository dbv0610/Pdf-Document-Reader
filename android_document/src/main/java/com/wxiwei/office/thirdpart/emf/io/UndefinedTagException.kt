// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException

class UndefinedTagException : IOException {
    constructor() : super()

    constructor(msg: String) : super(msg)

    constructor(code: Int) : super("Code: ($code)")
}
