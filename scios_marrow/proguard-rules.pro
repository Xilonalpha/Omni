# SCIOS MARROW SPECIFIC PROGUARD RULES

# Ktor Client & Plugins
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Google AI Client
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# Serialization
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**
