# Chaquopy
-keep class com.chaquo.python.** { *; }

# Keep Python API classes
-keep class com.superstar.android.python.** { *; }

# License Manager
-keep class com.superstar.android.license.** { *; }

# Data Models & ViewModel
-keep class com.superstar.android.viewmodel.** { *; }
-keep class com.superstar.android.security.** { *; }

# Gson & JSON serialization
-keepattributes *Annotation*
-keepattributes Signature
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }
