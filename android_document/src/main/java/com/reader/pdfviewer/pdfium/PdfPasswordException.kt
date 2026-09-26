package com.reader.pdfviewer.pdfium

import java.io.IOException

/** Thrown when a PDF requires a password or the supplied password is incorrect.  */
class PdfPasswordException(message: String?) : IOException(message)
