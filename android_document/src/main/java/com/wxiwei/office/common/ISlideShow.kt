/*
 * 文件名称:          ISlideShow.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:58:39
 */
package com.wxiwei.office.common

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2012-12-28
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
interface ISlideShow {
    //    /**
    //     * 
    //     * @param actionType
    //     */
    //    public void slideshow(byte actionType);
    /**
     * exit slideshow
     */
    fun exit()

    companion object {
        //slideshow type
        //begin slideshow
        const val SlideShow_Begin: Byte = 0 //0

        //exit slideshow
        const val SlideShow_Exit: Byte = 1

        //previous step of animation
        const val SlideShow_PreviousStep: Byte = 2

        //next step of animation
        const val SlideShow_NextStep: Byte = 3

        //previous slide
        const val SlideShow_PreviousSlide: Byte = 4

        //next slide
        const val SlideShow_NextSlide: Byte = 5
    }
}
