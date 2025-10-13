# Random Tap Rush

Random Tap Rush is a lightweight Android tapping challenge built with Jetpack Compose. Targets appear in random locations and sizes across the playfield—land consecutive hits to build your combo multiplier before the 30-second timer expires.

## Game rules
- Tap the glowing orb to earn points. Each successful tap increases the combo multiplier.
- Missing a tap resets the combo but you can keep playing until the timer reaches zero.
- Hit the **Start** button to begin a fresh run at any time.

## Project structure
- `app/src/main/java/com/example/randomtaprush/` – Main Compose UI and game logic.
- `app/src/main/java/com/example/randomtaprush/ui/theme/` – Material theme definitions.
- `app/src/main/res/` – Android resources including strings, themes, and launcher assets.

## Building the APK
Ensure you have the latest Android SDK (API 34), Android Studio, Java 17, and a local Gradle installation (version 8.5 or newer). Then run:

```bash
gradle assembleDebug
```

The generated APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.
