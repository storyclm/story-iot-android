package ru.breffi.storyiot

import android.Manifest.permission
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import ru.breffi.lib.StoryIoTHttpConnector
import ru.breffi.lib.StoryParams
import ru.breffi.lib.models.Body
import ru.breffi.lib.models.IoTConfig
import ru.breffi.lib.models.StoryMessage
import ru.breffi.lib.network.FeedResponse
import ru.breffi.lib.network.MessageResponse
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var storyIoTHttpConnector: StoryIoTHttpConnector

    companion object {
        const val READ_REQUEST_CODE = 42
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val сonfig = IoTConfig(
            "https://staging-iot.storychannels.app",
            "b47bbc659eb344888f9f92ed3261d8dc",
            "df94b12c3355425eb4efa406f09e8b9f",
            "163af6783ae14d5f829288d1ca44950e"
        )
        storyIoTHttpConnector = StoryIoTHttpConnector.Builder(this, сonfig)
            .setAppName(getString(R.string.app_name))
            .setAppVersion(BuildConfig.VERSION_NAME)
            .build()
        largeMessageButton.setOnClickListener { selectFile() }
        smallMessageButton.setOnClickListener {
            storyIoTHttpConnector.publishSmallMessage(testSmallMessage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ smallMessageResponse: MessageResponse? ->
                    Log.e(StoryIoTHttpConnector.TAG, "publishSmallMessage $smallMessageResponse")
                    Toast.makeText(this, " Small message success", Toast.LENGTH_SHORT).show()
                }, { t ->
                    t.printStackTrace()
                    Toast.makeText(this, " Small message failure", Toast.LENGTH_SHORT).show()
                })
        }
        feedButton.setOnClickListener { storyIoTHttpConnector.getFeed("", StoryParams.DIRECTION_FORWARD, 10)
            .flatMap { response ->
                Log.e(StoryIoTHttpConnector.TAG, "get feed token 1 = ${response.token}")
                storyIoTHttpConnector.getFeed(response.token, StoryParams.DIRECTION_FORWARD, 10) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ feedResponse: FeedResponse? ->
                Log.e(StoryIoTHttpConnector.TAG, "get feed token 2 = ${feedResponse?.token}")
                Toast.makeText(this, "Get feed success", Toast.LENGTH_SHORT).show()
            }, { t ->
                t.printStackTrace()
                Toast.makeText(this, "Get feed failure", Toast.LENGTH_SHORT).show()
            })}

        getMessageButton.setOnClickListener { storyIoTHttpConnector.getMessage("98f78a7ab3834babbeb6e73ba911e2f9")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ messageResponse ->
                Log.e(StoryIoTHttpConnector.TAG, "get message success = ${messageResponse?.id}")
                Toast.makeText(this, "Get message success = ${messageResponse?.id}", Toast.LENGTH_SHORT).show()
            }, { t ->
                t.printStackTrace()
                Toast.makeText(this, "Get message failure", Toast.LENGTH_SHORT).show()
            })}

        updateMessageButton.setOnClickListener { storyIoTHttpConnector.updateMetadataMessage("test", Date().toString(), "98f78a7ab3834babbeb6e73ba911e2f9")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ messageResponse ->
                Log.e(StoryIoTHttpConnector.TAG, "update message success = ${messageResponse?.id}")
                Toast.makeText(this, "Update message success = ${messageResponse?.id}", Toast.LENGTH_SHORT).show()
            }, { t ->
                t.printStackTrace()
                Toast.makeText(this, "Update message failure", Toast.LENGTH_SHORT).show()
            })}

        deleteMessageButton.setOnClickListener { storyIoTHttpConnector.deleteMetadataMessage("test",  "98f78a7ab3834babbeb6e73ba911e2f9")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ messageResponse ->
                Log.e(StoryIoTHttpConnector.TAG, "Delete message success = ${messageResponse?.id}")
                Toast.makeText(this, "Delete message success = ${messageResponse?.id}", Toast.LENGTH_SHORT).show()
            }, { t ->
                t.printStackTrace()
                Toast.makeText(this, "Delete message failure", Toast.LENGTH_SHORT).show()
            })}
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

    fun testLargeMessage(file: File): StoryMessage {
        var smallMessage = StoryMessage()
        smallMessage.eventId = "clm.session"
        smallMessage.userId = "71529FCA-3154-44F5-A462-66323E464F23"
        smallMessage.correlationToken = "96529FCA-6666-44F5-A462-66323E464444"
        smallMessage.id = "32"
        smallMessage.operationType = "u"
        smallMessage.deviceId = "96we9FCA-6666-44F5-A462-66323E464444"
        smallMessage.body = file
        return smallMessage
    }

    fun selectFile() {
        val disposable = RxPermissions(this)
            .request(permission.READ_EXTERNAL_STORAGE)
            .subscribe { granted ->
                if (granted) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"
                    startActivityForResult(intent, READ_REQUEST_CODE)
                }
            }
    }

    public override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        resultData: Intent?
    ) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            var uri: Uri?
            if (resultData != null) {
                uri = resultData.data
                var file = File(PathUtils.getPath(this, uri))
                Log.e(TAG, "file = ${file.name}, ${file.absolutePath}")
                val disposable = storyIoTHttpConnector.publishLargeMessage(testLargeMessage(file))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ smallMessageResponse: MessageResponse? ->
                        Log.e(StoryIoTHttpConnector.TAG, "publishLargeMessage $smallMessageResponse")
                        Toast.makeText(this, " Large message success", Toast.LENGTH_SHORT).show()
                    }, { t ->
                        t.printStackTrace()
                        Toast.makeText(this, " Large message failure", Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }
}
