/*
 * 文件名称:          TokenManage.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:31:08
 */
package com.wxiwei.office.objectpool

import java.util.Vector

/**
 * token manage
 */
class TokenManage {

    @Synchronized
    fun allocToken(obj: IMemObj): ParaToken? {
        var token: ParaToken? = null
        if (paraTokens.size >= TOKEN_SIZE) {
            for (i in 0 until TOKEN_SIZE) {
                if (paraTokens[i]!!.isFree) {
                    token = paraTokens.removeAt(i)
                    break
                }
            }
            //token.free();
            paraTokens.add(token)
        } else {
            token = ParaToken(obj)
            paraTokens.add(token)
        }
        return token
    }

    //
    @JvmField
    var paraTokens = Vector<ParaToken?>(TOKEN_SIZE)

    companion object {
        // token size
        private const val TOKEN_SIZE = 10

        //
        @JvmField
        var kit = TokenManage()

        @JvmStatic
        fun instance(): TokenManage {
            return kit
        }
    }
}
