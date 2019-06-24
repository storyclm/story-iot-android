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
import ru.breffi.lib.models.Body
import ru.breffi.lib.models.StoryMessage
import ru.breffi.lib.network.MessageResponse
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var storyIoTHttpConnector: StoryIoTHttpConnector

    companion object {
        const val READ_REQUEST_CODE = 42
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storyIoTHttpConnector = StoryIoTHttpConnector.Builder(this)
            .setAppName(getString(R.string.app_name))
            .setAppVersion(BuildConfig.VERSION_NAME)
            .build()
        largeMessageButton.setOnClickListener { selectFile() }
        smallMessageButton.setOnClickListener {
            storyIoTHttpConnector.publishSmallMessagesWithRetrofit(testSmallMessage())
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
                val disposable = storyIoTHttpConnector.publishLargeMessages(testLargeMessage(file))
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
