package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFRenderer

interface GDIObject {
    fun render(renderer: EMFRenderer)
}
