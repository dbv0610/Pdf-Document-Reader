# Only when the SDK itself is built (release AAR); consumer-rules.pro is added too.
# Everything outside com.editor.docsdk that no rule keeps is renamed and moved into one
# package, so the published AAR does not show the engine's structure.

-repackageclasses 'com.editor.docsdk.internal'
# the engine does not use code only found by optimizations: leave the code as written
-dontoptimize
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
# keep build/outputs/mapping/release/mapping.txt of each published version: it turns
# obfuscated stack traces from integrators back into readable ones (it is not published)
