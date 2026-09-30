# Nothing is needed for DocSDK: its rules come with the AAR (consumer rules).
# The tests of this sample reach into its own classes:
-keep class com.example.docsdksample.** { *; }
# Espresso brings errorprone annotations, which name compiler-only classes
-dontwarn javax.lang.model.**
# the test APK runs on the Kotlin classes of the minified app
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
