/*
 * 文件名称:          ResKit.java
 *
 * 编译器:            android2.2
 * 时间:              下午9:03:08
 */

package com.wxiwei.office.res

/**
 *
 */
class ResKit {

    private val res = HashMap<String, String>()

    init {
        try {
            // load "ResConstant"
            val cls = Class.forName("com.wxiwei.office.res.ResConstant")
            // get all fields (chỉ lấy field String, bỏ qua field INSTANCE của Kotlin object)
            for (field in cls.declaredFields) {
                if (field.type == String::class.java) {
                    res[field.name] = field.get(null) as String
                }
            }
        } catch (e: Exception) {
        }
    }

    fun hasResName(resName: String): Boolean {
        return res.containsKey(resName)
    }

    fun getLocalString(resName: String): String? {
        return res[resName]
    }

    companion object {
        //
        private val kit = ResKit()

        @JvmStatic
        fun instance(): ResKit {
            return kit
        }
    }
}
