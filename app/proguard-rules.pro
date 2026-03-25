# ProGuard / R8 rules for android-file-converter
# Generated: 2026-03-25
# IMPORTANT: Keep rules are TARGETED — no blanket keeps.

# ---------------------------------------------------------------------------
# 1. Crash debugging: preserve source file names and line numbers
# ---------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# 2. Annotations and inner classes (required by kotlinx.serialization)
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses

# ---------------------------------------------------------------------------
# 3. Kotlin Metadata (required for reflection-based Kotlin features)
# ---------------------------------------------------------------------------
-keepattributes RuntimeVisibleAnnotations

-keep class kotlin.Metadata { *; }

# ---------------------------------------------------------------------------
# 4. @JavascriptInterface bridge — CRITICAL for pandoc.wasm WebView bridge
#    The anonymous object in PandocEngine.kt uses @JavascriptInterface on
#    onInitComplete() and onInitError(). R8 must not rename or strip these.
#    Rule keeps methods BY ANNOTATION across all classes (not a blanket keep).
# ---------------------------------------------------------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ---------------------------------------------------------------------------
# 5. Enum classes — keep values() and valueOf() to prevent runtime failures
# ---------------------------------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---------------------------------------------------------------------------
# 6. kotlinx.serialization — keep @Serializable class companion objects
#    and their serializer() methods so the JSON codec works at runtime.
#    Applies to: RawConversionResult, ConverterScreen, AboutScreen, W
# ---------------------------------------------------------------------------
-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.Serializable

# Keep companion objects of @Serializable classes
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep serializer() on nested $Companion classes
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializer() on top-level $Companion classes
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep the serializer descriptor fields used by kotlinx.serialization
-keepclassmembers class kotlinx.serialization.internal.** { *; }
