# d3tr01t AI

Native Android assistant for Nothing Phone (3a), designed for offline-first operation.

## Current milestone
- Native Kotlin + Jetpack Compose application shell.
- Local/cloud mode selection UI.
- Network-state detection.
- GitHub Actions debug APK build.
- Model runtime is the next milestone; this scaffold does **not** yet contain an executable local LLM.

## Build
Open the project in Android Studio or run:

```bash
gradle :app:assembleDebug
```

Requires Android SDK 35 and JDK 17.

## Architecture
- Android app: Kotlin, Jetpack Compose.
- Planned local inference: llama.cpp Android/JNI runtime, GGUF model files.
- Planned local storage: Room/SQLite.
- Cloud provider: existing FREEzzz AI backend / OpenAI API, with secrets kept server-side.
- Offline mode must never silently send prompts to the network.
