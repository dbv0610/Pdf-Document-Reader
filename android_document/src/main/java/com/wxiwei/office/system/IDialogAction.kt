package com.wxiwei.office.system

import java.util.Vector

interface IDialogAction {
    fun doAction(id: Int, model: Vector<Any>?)
    fun getControl(): IControl?
    fun dispose()
}
