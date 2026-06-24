<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="NextSync" width="100"/>
  <h1>NextSync</h1>
  <p>Bidirectional file synchronization between Android and Nextcloud</p>

  <a href="https://github.com/DanyaSWorlD/NextSync/releases">
    <img src="https://img.shields.io/github/v/release/DanyaSWorlD/NextSync?style=for-the-badge&labelColor=18181b&color=6366f1" alt="Release"/>
  </a>
  <a href="https://github.com/DanyaSWorlD/NextSync/blob/main/README.md">
    <img src="https://img.shields.io/github/languages/code-size/DanyaSWorlD/NextSync?style=for-the-badge&labelColor=18181b&color=6366f1" alt="Code size"/>
  </a>
  <img src="https://img.shields.io/badge/minSdk-24-22c55e?style=for-the-badge&labelColor=18181b" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/targetSdk-36-22c55e?style=for-the-badge&labelColor=18181b" alt="Target SDK"/>
  <br/>
  <img src="https://img.shields.io/badge/Kotlin-2.2.0-7f52ff?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=18181b" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack_Compose-1.7-4285f4?style=for-the-badge&logo=jetpackcompose&logoColor=white&labelColor=18181b" alt="Compose"/>
  <img src="https://img.shields.io/badge/Material_3-3b82f6?style=for-the-badge&logo=materialdesign&logoColor=white&labelColor=18181b" alt="Material 3"/>
</div>

---

**NextSync** is a fast, lightweight Nextcloud file sync app for Android — no bloat, no distractions, just reliable syncing done right. Create sync tasks with configurable direction (upload, download, or bidirectional) and let the app handle the rest with periodic background sync and real-time file watching.

## Features

- **Nextcloud Login** — authenticate via the built-in WebView login flow; credentials stored locally
- **Sync Tasks** — name your tasks, pick local and remote folders, choose direction (ToCloud / ToDevice / bidirectional)
- **Folder Pickers** — browse local device storage and remote Nextcloud directories via WebDAV
- **Dashboard** — storage quota gauge, network & battery status, sync trigger, transfer stats
- **Background Sync** — automatic sync every 15 minutes via WorkManager; real-time `FileObserver` watching
- **Conflict Resolution** — local wins, remote wins, newer wins, keep both, or ask user
- **Progress Notifications** — transfer progress in the notification shade
- **Material Design 3** — light & dark theme with optional dynamic color (Android 12+)

## Screenshots

*Coming soon.*

## Download

<a href="https://play.google.com/store/apps/details?id=com.next.sync">
  <img src="https://img.shields.io/badge/Google_Play-414141?style=for-the-badge&logo=googleplay&logoColor=white&labelColor=18181b" alt="Google Play"/>
</a>
<a href="https://github.com/DanyaSWorlD/NextSync/releases">
  <img src="https://img.shields.io/github/v/release/DanyaSWorlD/NextSync?style=for-the-badge&logo=github&label=GitHub&labelColor=18181b&color=6366f1" alt="GitHub Release"/>
</a>

Available on **Google Play** and [GitHub Releases](https://github.com/DanyaSWorlD/NextSync/releases).

## Building

```bash
git clone https://github.com/DanyaSWorlD/NextSync.git
cd NextSync
./gradlew :app:assembleDebug   # Build debug APK
./gradlew :app:test            # Run unit tests
./gradlew :app:lint            # Run Android lint
```

Open the project in **Android Studio** (Meerkat or later) for development.

## Tech Stack

| Concern | Library |
|---|---|
| Language | [Kotlin](https://kotlinlang.org/) 2.2.0 |
| UI | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material 3](https://m3.material.io/) |
| DI | [Dagger Hilt](https://dagger.dev/hilt/) (kapt) |
| Database | [ObjectBox](https://objectbox.io/) |
| Navigation | [Navigation Compose](https://developer.android.com/guide/navigation/navigate) |
| Networking | [Nextcloud Android Library](https://github.com/nextcloud/android-library) (WebDAV) |
| Background | [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) |
| Serialization | [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) |
| Image Loading | [Coil](https://coil-kt.github.io/coil/) |
| Permissions | [XXPermissions](https://github.com/getActivity/XXPermissions) |

## Architecture

Single-activity app with Compose Navigation (3 tabs: Home, Tasks, Options). ViewModels expose state via `StateFlow`. A custom event bus (`DataBus`) handles cross-component communication. The sync engine follows a chain-of-responsibility pattern with pluggable strategies.

```
app/src/main/java/com/next/sync/
├── App.kt                          # @HiltAndroidApp
├── MainActivity.kt                 # Single activity
├── ui/                             # Screens (Login, Home, Tasks, CreateTask, Options, FolderPicker)
├── core/sync/                      # ISyncTask chain, ISyncStrategy, NextSync orchestrator
├── core/db/                        # ObjectBox entities (AccountEntity, TaskEntity, FileStateEntity, DirectoryEntity)
├── core/di/                        # Hilt modules, DataBus, helpers
├── core/model/                     # Domain models (SyncFlowDirection, NetworkInfo, BatteryInfo, FileStateItem)
└── background/workers/             # SyncCheckWorker (periodic), UploadWorker, DownloadWorker
```

## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.

## License

This project does not currently have a license. All rights reserved.
