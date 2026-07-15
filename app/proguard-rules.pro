# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes Signature
-keep class com.smartedu.tv.data.model.** { *; }
-keep class com.smartedu.tv.data.api.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# ExoPlayer
-keep class androidx.media3.** { *; }
