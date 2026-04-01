# Firebase specific rules
-keepattributes SourceFile,LineNumberTable
-keep public class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# App specific classes that should not be obfuscated
-keep class com.example.vc_client_android_app.data.** { *; }

# General hardening
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
