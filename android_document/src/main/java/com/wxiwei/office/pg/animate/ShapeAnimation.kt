package com.wxiwei.office.pg.animate

class ShapeAnimation @JvmOverloads constructor(
    private val shapeID: Int,
    private val animType: Byte,
    private val paraBegin: Int = Para_BG,
    private val paraEnd: Int = Para_BG
) {
    companion object {
        const val SA_ENTR: Byte = 0
        const val SA_EMPH: Byte = 1
        const val SA_EXIT: Byte = 2
        const val SA_PATH: Byte = 3
        const val SA_VERB: Byte = 4
        const val SA_MEDIACALL: Byte = 5
        const val Para_BG: Int = -1
        const val Para_All: Int = -2
        const val Slide: Int = -3
    }
    fun getShapeID(): Int = shapeID
    fun getAnimationType(): Byte = animType
    fun getParagraphBegin(): Int = paraBegin
    fun getParagraphEnd(): Int = paraEnd
}
