# app/proguard-rules.pro

# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide original source file name
-renamesourcefileattribute SourceFile

# Keep classes in com.thellex.payments package
-keep class com.thellex.pay.core.utils.AuthUtils { *; }
-keep class com.thellex.pay.network.** { *; }
-keep class com.thellex.pay.models.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-dontwarn retrofit2.**
-keep class com.squareup.retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# Gson
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-dontwarn com.google.gson.**

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*
-dontwarn kotlinx.serialization.**

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Glide (from your provided rules, completed)
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep data classes and their members
-keep class com.thellex.pay.models.* { *; }
-keepclassmembers class com.thellex.pay.models.* {
    <fields>;
    <methods>;
}

# Prevent R8 from removing Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep ViewBinding generated classes
-keep class com.thellex.pay.databinding.** { *; }

# Prevent warnings for Java reflection
-dontwarn java.lang.invoke.**

# Keep Kotlin metadata
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlin.**

# Prevent removal of unused classes accessed via reflection
-keep class ** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable public *;
}

# WebView with JavaScript (uncomment if used)
#-keepclassmembers class com.thellex.payments.** {
#    public *;
#}