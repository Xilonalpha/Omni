# SCIOS MARROW CONSUMER PROGUARD RULES
# These rules are automatically applied to any app module that consumes this library

-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep HttpTimeout specifically as it's often missing in stacktraces
-keep class io.ktor.client.plugins.HttpTimeout { *; }
-keep class io.ktor.client.plugins.HttpTimeout$Config { *; }
