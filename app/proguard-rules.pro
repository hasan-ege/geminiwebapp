# Add project specific ProGuard rules here.

# Keep the MainActivity and its inner classes from being obfuscated
-keep class com.bozkurt.geminiwebapp.MainActivity {
    *;
}

-keep class com.bozkurt.geminiwebapp.MainActivity$* {
    *;
}
