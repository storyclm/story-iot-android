# Story IoT for Android

Lib for work with StoryCLM data and files

## Installation

Use Gradle dependency for import lib from JitPack

Project build.gradle file

```bash
allprojects {
    repositories {
       ...
        maven { url "https://jitpack.io" }
       ...
    }
}
```

App build.gradle file

```bash
implementation 'com.github.storyclm:story-iot-android:1.0.2'
```

## Usage

```kotlin
import ru.breffi.lib.StoryIoTHttpConnector

StoryIoTHttpConnector.Builder(this)
            .setAppName(getString(R.string.app_name))
            .setAppVersion(BuildConfig.VERSION_NAME)
            .build()
```

## License
[MIT](https://choosealicense.com/licenses/mit/)