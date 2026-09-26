cmake_minimum_required(VERSION 3.22.1)
project(jniPdfium LANGUAGES CXX)

# Prebuilt PDFium, selected for the ABI requested by Gradle.
add_library(pdfium SHARED IMPORTED)
set_target_properties(pdfium PROPERTIES
    IMPORTED_LOCATION "${CMAKE_CURRENT_SOURCE_DIR}/lib/${ANDROID_ABI}/libpdfium.so"
)

add_library(jniPdfium SHARED src/mainJNILib.cpp)
target_include_directories(jniPdfium PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/include")
target_compile_definitions(jniPdfium PRIVATE HAVE_PTHREADS)
target_compile_options(jniPdfium PRIVATE -fexceptions)
target_link_libraries(jniPdfium PRIVATE pdfium log android jnigraphics)

# Preserve 16 KB page alignment for Android 15+ devices.
target_link_options(jniPdfium PRIVATE "-Wl,-z,max-page-size=16384")
