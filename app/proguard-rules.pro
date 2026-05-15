# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ---- Kotlin ----
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }

# ---- Hilt (Dependency Injection) ----
-keep class dagger.hilt.** { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ---- Room (Database) ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.**

# ---- Firebase ----
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# ---- Firebase Crashlytics ----
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ---- Google Maps ----
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.maps.**

# ---- Navigation Component ----
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ---- WorkManager ----
-keep class androidx.work.** { *; }
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ---- App-specific models ----
# Keep all data classes used with Room to prevent field name obfuscation
-keep class com.madhusiri.app.data.model.** { *; }
-keep class com.madhusiri.app.core.data.** { *; }
