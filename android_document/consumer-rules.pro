-keep class com.reader.pdfviewer.pdfium.** { *; }

# PdfBox (only used to encrypt a copy): JPEG 2000 support is an optional library we do not ship
-dontwarn com.gemalto.jp2.**
# security handlers are made by reflection from their policy class
-keep class com.tom_roush.pdfbox.pdmodel.encryption.** { *; }
