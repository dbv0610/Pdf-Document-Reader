/*
 * 文件名称:          Style.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:14:40
 */
package com.wxiwei.office.simpletext.model

/**
 * 样式
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-2-27
 *
 * 负责人:          ljj8494
 */
class Style {
    // style id
    private var id = -1

    // style base id;
    private var baseID = -1

    // style name
    private var name: String? = null

    // = 0 paragraph; = 1 character
    private var type: Byte = 0

    // attribute set
    private var attr: IAttributeSet? = AttributeSetImpl()

    /**
     * @return Returns the id.
     */
    fun getId(): Int {
        return id
    }

    /**
     * @param id The id to set.
     */
    fun setId(id: Int) {
        this.id = id
    }

    /**
     * @return Returns the baseID.
     */
    fun getBaseID(): Int {
        return baseID
    }

    /**
     * @param baseID The baseID to set.
     */
    fun setBaseID(baseID: Int) {
        this.baseID = baseID
    }

    /**
     * @return Returns the name.
     */
    fun getName(): String? {
        return name
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @return Returns the type.
     */
    fun getType(): Byte {
        return type
    }

    /**
     * @param type The type to set.
     */
    fun setType(type: Byte) {
        this.type = type
    }

    /**
     * @return Returns the attr.
     */
    fun getAttrbuteSet(): IAttributeSet? {
        return attr
    }

    /**
     * @param attr The attr to set.
     */
    fun setAttrbuteSet(attr: IAttributeSet?) {
        this.attr = attr
    }

    fun dispose() {
        name = null
        if (attr != null) {
            attr!!.dispose()
            attr = null
        }
    }
}
