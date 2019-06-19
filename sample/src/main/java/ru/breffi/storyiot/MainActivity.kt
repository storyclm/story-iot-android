package ru.breffi.storyiot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.breffi.lib.StoryIoTHttpConnector

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val storyIoTHttpConnector = StoryIoTHttpConnector.Builder(this)
            .build()
    }
}
