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

Initialization
```kotlin
import ru.breffi.lib.StoryIoTHttpConnector

StoryIoTHttpConnector.Builder(this)
            .setAppName(getString(R.string.app_name))
            .setAppVersion(BuildConfig.VERSION_NAME)
            .build()
```

Publish small message
```kotlin
storyIoTHttpConnector.publishSmallMessage(storyMessage)
```

Publish large message with file or any other large data
```kotlin
storyMessage.body = file
storyIoTHttpConnector.publishLargeMessage(storyMessage)
```

Get messages feed
```kotlin
storyIoTHttpConnector.getFeed("CorrelationToken", StoryParams.DIRECTION_FORWARD, 10)
```

Get message by id
```kotlin
storyIoTHttpConnector.getMessage("id")
```

Create or update message metadata
```kotlin
storyIoTHttpConnector.updateMetadataMessage("metaDataName", "metaDataValue", "messageId")
```

Delete message metadata
```kotlin
storyIoTHttpConnector.deleteMetadataMessage("metaDataName",  "messageId")
```

## License
[MIT](https://choosealicense.com/licenses/mit/)