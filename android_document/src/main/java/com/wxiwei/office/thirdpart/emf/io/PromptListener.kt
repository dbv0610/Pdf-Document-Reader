// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException

interface PromptListener {
    @Throws(IOException::class)
    fun promptFound(route: RoutedInputStream.Route)
}
