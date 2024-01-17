package com.example.calculator_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val actionBar = supportActionBar

        // Check if ActionBar is not null before hiding it
        actionBar?.hide()
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(2500)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val intent = Intent(
                        this@SplashActivity,
                        MainActivity::class.java
                    )
                    startActivity(intent)
                }
            }
        }
        thread.start()
    }
}