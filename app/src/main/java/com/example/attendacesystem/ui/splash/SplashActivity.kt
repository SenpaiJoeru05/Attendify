package com.example.attendacesystem.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.attendacesystem.ui.attendance.AttendanceActivity
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {
    private val splashScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_splash) // Uncomment if you have a layout

        splashScope.launch {
            delay(1500)
            startActivity(Intent(this@SplashActivity, AttendanceActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        splashScope.cancel()
        super.onDestroy()
    }
}