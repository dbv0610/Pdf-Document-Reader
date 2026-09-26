/*
 * 文件名称:          SlideshowListener.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:19:37
 */
package com.wxiwei.office.macro

import com.wxiwei.office.common.ISlideShow

/**
 *
 */
interface SlideShowListener {

    /**
     * exit slideshow
     */
    fun exit()

    companion object {
        //begin slideshow
        const val SlideShow_Begin = ISlideShow.SlideShow_Begin                    //0
        //exit slideshow
        const val SlideShow_Exit = ISlideShow.SlideShow_Exit                      //1
        //previous step of animation
        const val SlideShow_PreviousStep = ISlideShow.SlideShow_PreviousStep      //2
        //next step of animation
        const val SlideShow_NextStep = ISlideShow.SlideShow_NextStep              //3
        //previous slide
        const val SlideShow_PreviousSlide = ISlideShow.SlideShow_PreviousSlide    //4
        //next slide
        const val SlideShow_NextSlide = ISlideShow.SlideShow_NextSlide            //5
    }
}
