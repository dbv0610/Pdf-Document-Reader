// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException

interface RouteListener {
    @Throws(IOException::class)
    fun routeFound(input: RoutedInputStream.Route)
}
