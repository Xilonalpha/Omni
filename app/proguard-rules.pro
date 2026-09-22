# SCI-OS SOVEREIGN CORE PROGUARD RULES v16.3 - PRODUCTION ULTIMATE

# Retain generic signatures for reflection (CRITICAL FOR GSON)
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# GSON SPECIFIC PROTECTIONS
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep @interface com.google.gson.annotations.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.TypeConverter
-keep @androidx.room.Entity class ** { *; }

# Ktor, Netty & Coroutines
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Netty & BlockHound
-dontwarn io.netty.**
-keep class io.netty.** { *; }
-dontwarn reactor.blockhound.**
-dontwarn sun.security.ext.**
-dontwarn sun.security.util.**

# Annotation Processors Cleanup
-dontwarn javax.lang.model.**
-dontwarn autovalue.shaded.**
-dontwarn com.squareup.javapoet.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# ARCore & Filament
-keep class com.google.ar.core.** { *; }
-keep class com.google.ar.sceneform.** { *; }
-keep class com.google.android.filament.** { *; }

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }
-dontwarn org.tensorflow.lite.gpu.**

# Hilt
-keep class * extends dagger.hilt.android.flags.HiltWrapper*
-keep @dagger.hilt.android.HiltAndroidApp class * {*;}
-keep class * extends dagger.hilt.processor.internal.aggregatedroot.codegen._* {*;}
-keep class * extends dagger.hilt.android.internal.legacy.AggregatedElementProxy {*;}

# Keep SCI-OS Models (The Marrow)
-keep class com.chemscanner.omniscient.data.models.** { *; }
-keep class com.planetscanner.app.data.models.** { *; }

# Keep ViewModels & Composables
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep @androidx.compose.runtime.Composable class * { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep resources
-keep class **.R$* { *; }
