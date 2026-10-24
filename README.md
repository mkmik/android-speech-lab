# Android Speech Lab

Minimal Android app comparing the system default speech recognizer with the on-device recognizer.

Install the debug APK from the latest successful GitHub Actions run. Grant microphone permission and tap either mode. Partial and final recognition results show elapsed time since recognition was requested (not true speech-to-text latency).

Requires Android 12+. Recognition availability depends on installed services. Try airplane mode to test offline behavior.

Build locally with Android SDK and Gradle: `gradle assembleDebug`.
