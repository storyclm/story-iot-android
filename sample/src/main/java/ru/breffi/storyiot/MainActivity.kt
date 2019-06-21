package ru.breffi.storyiot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.breffi.lib.StoryIoTHttpConnector
import ru.breffi.lib.models.Body
import ru.breffi.lib.models.StoryMessage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val storyIoTHttpConnector = StoryIoTHttpConnector.Builder(this)
            .setAppName(getString(R.string.app_name))
            .setAppVersion(BuildConfig.VERSION_NAME)
            .build()
//        storyIoTHttpConnector.publishSmallMessagesWithRetrofit(arrayListOf(testSmallMessage()))
        storyIoTHttpConnector.publishLargeMessages(arrayListOf(testLargeMessage()))
    }

    fun testSmallMessage(): StoryMessage {
        var smallMessage = StoryMessage()
        smallMessage.eventId = "clm.session"
        smallMessage.userId = "71529FCA-3154-44F5-A462-66323E464F23"
        smallMessage.correlationToken = "96529FCA-6666-44F5-A462-66323E464444"
        smallMessage.id = "32"
        smallMessage.operationType = "u"
        smallMessage.deviceId = "96we9FCA-6666-44F5-A462-66323E464444"
        val body = Body()
        body.id = "id"
        body.value = "value"
        smallMessage.body = body
        return smallMessage
    }

    fun testLargeMessage(): StoryMessage {
        var smallMessage = StoryMessage()
        smallMessage.eventId = "clm.session"
        smallMessage.userId = "71529FCA-3154-44F5-A462-66323E464F23"
        smallMessage.correlationToken = "96529FCA-6666-44F5-A462-66323E464444"
        smallMessage.id = "32"
        smallMessage.operationType = "u"
        smallMessage.deviceId = "96we9FCA-6666-44F5-A462-66323E464444"
        smallMessage.body = "Филипп Крикоров"
        return smallMessage
    }
}
