/*
 * 文件名称:          SheetbarResManager.java
 *
 * 编译器:            android2.2
 */
package com.wxiwei.office.ss.sheetbar

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.wxiwei.office.R

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-8-27
 * 负责人:           jqin
 */
class SheetbarResManager(context: Context) {
    private var context: Context? = context
    private var sheetbarBG: Drawable? = null
    private var sheetbarLeftShadow: Drawable? = null
    private var sheetbarRightShadow: Drawable? = null
    private var hSeparator: Drawable? = null
    //left
    /*private Drawable normalLeft;
    private Drawable pushLeft;
    private Drawable focusLeft;*/
    //middle
    private var normalMiddle: Drawable? = null
    //private Drawable pushMiddle;
    private var focusMiddle: Drawable? = null
    private var textSelectedTabColor = 0
    private var textUnselectedTabColor = 0
    //private int backgroundTabLayoutColor;
    //right
    /*private Drawable normalRight;
    private Drawable pushRight;
    private Drawable focusRight;*/

    init {
        @Suppress("UNUSED_VARIABLE")
        val loader = context.classLoader
        //sheetbar background
        sheetbarBG = ColorDrawable(ContextCompat.getColor(context, R.color.color_background_sheet_tablayout))
        sheetbarLeftShadow = ContextCompat.getDrawable(context, R.drawable.img_office_ss_sheetbar_shadow_left)
        sheetbarRightShadow = ContextCompat.getDrawable(context, R.drawable.img_office_ss_sheetbar_shadow_right)
        hSeparator = ContextCompat.getDrawable(context, R.drawable.img_office_ss_sheetbar_separated_horizontal)
        //normalLeft = ContextCompat.getDrawable(context, R.drawable.ss_sheetbar_button_normal_left);
        //normalRight = ContextCompat.getDrawable(context, R.drawable.ss_sheetbar_button_normal_right);
        normalMiddle = ColorDrawable(ContextCompat.getColor(context, R.color.color_background_button_sheet))
        //pushLeft = ContextCompat.getDrawable(context, R.drawable.ss_sheetbar_button_push_left);
        //pushMiddle = ContextCompat.getDrawable(context, R.drawable.ss_sheetbar_button_push_middle);
        //pushRight = ContextCompat.getDrawable(context, R.drawable.ss_sheetbar_button_push_right);
        //focusLeft = ContextCompat.getDrawable(context, R.drawable.ss_sheetbar_button_focus_left);
        focusMiddle = ColorDrawable(ContextCompat.getColor(context, R.color.clr_excel))
        textSelectedTabColor = ContextCompat.getColor(context, R.color.white)
        textUnselectedTabColor = ContextCompat.getColor(context, R.color.color_unselected_text_button_sheet)
        //focusRight = ContextCompat.getDrawable(context, R.drawable.ss_sheetbar_button_focus_right);

        // (The original Java kept a long commented-out block here that loaded every drawable via
        //  Drawable.createFromResourceStream(context.getResources(), null,
        //      loader.getResourceAsStream(SheetbarResConstant.RESNAME_xxx), SheetbarResConstant.RESNAME_xxx)
        //  for sheetbar bg, shadows, separator and normal/push/focus left/middle/right states.)
    }

    fun getColor(resId: Int): Int? {
        when (resId) {
            SheetbarResConstant.RESID_SHEET_BUTTON_SELECTED_TEXT_COLOR -> return textSelectedTabColor
            SheetbarResConstant.RESID_SHEET_BUTTON_UNSELECTED_TEXT_COLOR -> return textUnselectedTabColor
        }
        return null
    }

    fun getDrawable(resID: Short): Drawable? {
        when (resID) {
            SheetbarResConstant.RESID_SHEETBAR_BG -> return sheetbarBG

            SheetbarResConstant.RESID_SHEETBAR_SHADOW_LEFT -> return sheetbarLeftShadow

            SheetbarResConstant.RESID_SHEETBAR_SHADOW_RIGHT -> return sheetbarRightShadow

            SheetbarResConstant.RESID_SHEETBAR_SEPARATOR_H -> return hSeparator

            /*SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_LEFT -> return normalLeft*/

            SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_MIDDLE ->
                /*return Drawable.createFromResourceStream(context.getResources(), null,
                    context.getClassLoader().getResourceAsStream(SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_MIDDLE),
                    SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_MIDDLE);*/
                return normalMiddle

            /*SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_RIGHT -> return normalRight
            SheetbarResConstant.RESID_SHEETBUTTON_PUSH_LEFT -> return pushLeft*/
            /*SheetbarResConstant.RESID_SHEETBUTTON_PUSH_MIDDLE -> return pushMiddle*/
            /*SheetbarResConstant.RESID_SHEETBUTTON_PUSH_RIGHT -> return pushRight
            SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_LEFT -> return focusLeft*/

            SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_MIDDLE -> return focusMiddle

            /*SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_RIGHT -> return focusRight*/
        }

        return null
    }

    fun dispose() {
        sheetbarBG = null
        sheetbarLeftShadow = null
        sheetbarRightShadow = null
        hSeparator = null

        //normalLeft = null;
        normalMiddle = null
        //normalRight = null;

        //pushLeft = null;
        //pushMiddle =  null;
        //pushRight = null;

        //focusLeft = null;
        focusMiddle = null
        //focusRight = null;
    }
}
