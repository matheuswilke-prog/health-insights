# Preserve stack trace line numbers for crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Hilt — keep generated component classes and injection entry points.
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# Kotlin coroutines — required for internal state machine classes.
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }

# Kotlin metadata — required for reflection-free serialization and DI.
-keep class kotlin.Metadata { *; }

# Jetpack Compose — tooling and internal reflection hooks.
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**