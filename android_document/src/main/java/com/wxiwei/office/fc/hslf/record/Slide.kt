package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian

/**
 * Master container for Slides. There is one of these for every slide,
 * and they have certain specific children
 * 
 * @author Nick Burch
 */
class Slide : SheetContainer {
    private var _header: ByteArray?

    /**
     * Returns the SlideAtom of this Slide
     */
    var slideAtom: SlideAtom? = null
        private set
    private var ppDrawing: PPDrawing? = null
    private var _colorScheme: ColorSchemeAtom? = null

    var slideShowSlideInfoAtom: SlideShowSlideInfoAtom? = null
        private set

    var slideProgTagsContainer: SlideProgTagsContainer? = null
        private set
    var headersFootersContainer: HeadersFootersContainer? = null
        private set

    override fun getPPDrawing(): PPDrawing? {
        return ppDrawing
    }

    protected constructor(source: ByteArray, start: Int, len: Int) {
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        _children = findChildRecords(source, start + 8, len - 8)

        for (i in _children.indices) {
            if (_children[i] is SlideAtom) {
                slideAtom = _children[i] as SlideAtom?
            } else if (_children[i] is PPDrawing) {
                ppDrawing = _children[i] as PPDrawing?
            } else if (_children[i] is SlideShowSlideInfoAtom) {
                this.slideShowSlideInfoAtom = _children[i] as SlideShowSlideInfoAtom?
            } else if (_children[i] is SlideProgTagsContainer) {
                this.slideProgTagsContainer = _children[i] as SlideProgTagsContainer?
            } else if (_children[i] is HeadersFootersContainer) {
                headersFootersContainer = _children[i] as HeadersFootersContainer?
            }

            if (ppDrawing != null && _children[i] is ColorSchemeAtom) {
                _colorScheme = _children[i] as ColorSchemeAtom?
            }
        }
    }

    constructor() {
        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 15)
        LittleEndian.putUShort(_header!!, 2, _type.toInt())
        LittleEndian.putInt(_header!!, 4, 0)

        slideAtom = SlideAtom()
        ppDrawing = PPDrawing()

        val colorAtom = ColorSchemeAtom()

        _children = arrayOf(slideAtom!!, ppDrawing!!, colorAtom)
    }

    override fun getRecordType(): Long {
        return _type
    }

    override fun getColorScheme(): ColorSchemeAtom? {
        return _colorScheme
    }

    override fun dispose() {
        super.dispose()
        _header = null
        if (slideAtom != null) {
            slideAtom!!.dispose()
            slideAtom = null
        }
        if (ppDrawing != null) {
            ppDrawing!!.dispose()
            ppDrawing = null
        }
        if (_colorScheme != null) {
            _colorScheme!!.dispose()
            _colorScheme = null
        }

        if (this.slideShowSlideInfoAtom != null) {
            slideShowSlideInfoAtom!!.dispose()
            this.slideShowSlideInfoAtom = null
        }

        if (this.slideProgTagsContainer != null) {
            slideProgTagsContainer!!.dispose()
            this.slideProgTagsContainer = null
        }
    }

    companion object {
        private const val _type = 1006L
    }
}
