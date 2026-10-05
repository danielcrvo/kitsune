-keep class com.yausername.youtubedl_android.** { *; }
-dontwarn com.yausername.youtubedl_android.**
-keep class com.yausername.ffmpeg.** { *; }
-dontwarn com.yausername.ffmpeg.**

-keepclassmembers class * {
    native <methods>;
}

-keep class org.apache.commons.compress.** { *; }
-dontwarn org.apache.commons.compress.**

-keep class org.apache.commons.io.** { *; }
-dontwarn org.apache.commons.io.**

-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keep class coil.** { *; }

-keepclassmembers class androidx.compose.ui.platform.InspectableValue { *; }

-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

-keepclassmembers class com.kitsune.app.ui.screens.MainViewModel {
    <init>(android.app.Application);
    <init>(android.app.Application, com.kitsune.app.core.storage.UserPreferencesRepository);
}


