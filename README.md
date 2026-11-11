# My Homework Board

A native Android implementation of the supplied homework dashboard, designed for a 16:9 landscape display.

## Open and run

1. Open this folder in Android Studio.
2. Let Gradle finish syncing.
3. Select a landscape tablet, TV, or emulator running Android 6.0 or newer.
4. Run the `app` configuration.

The app launches full screen and locks itself to landscape orientation.

## Interaction

- Tap the highlighted Tuesday task to complete it.
- Continue tapping the next available task to finish Tuesday and unlock the remaining weekday columns.
- Tap an already completed Tuesday task to move progress back to that point.
- Tuesday progress is stored locally and restored when the app is reopened.
- The header date and time update automatically from the device.

## Build from a terminal

```bash
ANDROID_USER_HOME=/tmp/homework-board-android ./gradlew assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
