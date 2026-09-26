package com.reader.pdfviewer.exception

class PageRenderingException(@JvmField val page: Int, cause: Throwable?) : Exception(cause)
