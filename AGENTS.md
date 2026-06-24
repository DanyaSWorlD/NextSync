# NextSync — Agent Guide

Single-module Android app syncing files with Nextcloud.

## Build & run

```bash
./gradlew :app:assembleDebug          # build APK
./gradlew :app:test                   # unit tests (JUnit 4 only)
./gradlew :app:lint                   # Android lint
```

No UI test runner configured beyond default Espresso. No pre-commit hooks, formatter, or typecheck step.

## Key tech

| Concern | Choice | Notes |
|---|---|---|
| DI | Hilt (kapt) | `@HiltAndroidApp`, `@AndroidEntryPoint` |
| DB | ObjectBox | Codegen via `io.objectbox` plugin; model dir `app/objectbox-models/` |
| Serialization | kotlinx.serialization | Plugin `org.jetbrains.kotlin.plugin.serialization` |
| Navigation | Navigation Compose | Bottom tabs: Home / Tasks / Options |
| Background | WorkManager | `SyncCheckWorker` every 15 min |
| Nextcloud | `com.github.nextcloud:android-library` | |
| Image loading | Coil | |
| Permissions | XXPermissions | |
| Event bus | Custom `DataBus` (DI singleton) | Callback-based, not Flow |
| State | ViewModel + StateFlow | Hilt-integrated ViewModels via `hiltViewModel()` |

## Build config

- Gradle 8.13, AGP 8.11.1, Kotlin 2.2.0
- compileSdk/targetSdk = 36, minSdk = 24, Java 17
- `configuration-cache=false` in `gradle.properties`
- ProGuard disabled (`minifyEnabled = false`)
- kapt: `correctErrorTypes = true`, `useBuildCache = false`

## Architecture

- `App.kt` — `@HiltAndroidApp`, init ObjectBox
- `MainActivity.kt` — single activity, Compose entry, sets up periodic `SyncCheckWorker` and requests storage perm
- `ui/` — screens (Login, Home, Tasks, CreateTask, Options, FolderPicker)
- `core/sync/` — sync engine:
  - `NextSync` — orchestrator
  - `SyncWorker` — runs task chain
  - `ISyncTask` — chain-of-responsibility pattern (UploadTask, DownloadTask, DeleteRemoteTask, DeleteLocalTask, FolderRemoteTask, FolderLocalTask)
  - `ISyncStrategy` — decides actions per file pair (Local/Remote wins, Newer wins, etc.)
- `core/db/` — ObjectBox entities: `AccountEntity`, `DirectoryEntity`, `FileStateEntity`, `TaskEntity`
- `core/di/` — Hilt modules
- `background/workers/` — `UploadWorker`, `DownloadWorker`, `SyncCheckWorker` (periodic)

## Codegen

- **ObjectBox**: Annotate entities with `@Entity io.objectbox.annotation.Entity`, run `./gradlew :app:build` to regenerate `MyObjectBox`
- **Hilt**: `kapt` generates Dagger components at compile time
- `@Entity` classes live in `core/db/data/`

## Testing

- Test dirs: `src/test/` (unit), `src/androidTest/` (instrumented)
- Framework: JUnit 4 + Espresso (instrumented only)
- No Robolectric, no snapshot tests, no integration test suite
- Single `ExampleUnitTest.kt` and `ExampleInstrumentedTest.kt` exist
