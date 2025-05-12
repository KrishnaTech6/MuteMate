# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# General rules
-keep class com.google.** { *; }
-keep class android.accounts.** { *; }
-dontwarn javax.lang.model.element.Modifier

# Work Manager
-keep class androidx.work.** { *; }
-keepclassmembers class * implements androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Room Database rules
-keep class androidx.room.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# Keep your model classes used with Room
-keep class com.example.mutemate.model.** { *; }
-keep class com.example.mutemate.room.** { *; }

# Gson rules
-keep class com.google.gson.** { *; }
-keep class * implements java.io.Serializable { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# DataStore
-keep class androidx.datastore.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Jetpack Compose rules
-keep class androidx.compose.** { *; }
-keep class androidx.activity.compose.** { *; }
-keepclasseswithmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}

# Keep workers
-keep class com.example.mutemate.worker.** { *; }

# Keep accessiblity feature
-keep class com.example.mutemate.service.**{*;}

# Keep ViewModels and Factories
-keep class com.example.mutemate.viewmodel.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModelProvider$Factory {
    <init>(...);
    public <methods>;
}

# Keep AndroidViewModel and its constructors
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
    <init>(android.app.Application, ...);
}