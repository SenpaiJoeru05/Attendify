package com.example.attendacesystem.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.attendacesystem.databinding.ActivityMainMenuBinding
import com.example.attendacesystem.ui.attendance.AttendanceActivity
import com.example.attendacesystem.ui.registration.RegistrationActivity
import java.text.SimpleDateFormat
import java.util.*

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTime()

        binding.buttonCheckIn.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java).putExtra("mode", "checkin"))
        }
        binding.buttonCheckOut.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java).putExtra("mode", "checkout"))
        }
        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun updateTime() {
        binding.textTime.text = timeFormat.format(Date())
        binding.textDate.text = dateFormat.format(Date())
        handler.postDelayed({ updateTime() }, 1000)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}