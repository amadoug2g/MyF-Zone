package com.example.myf_zone.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val delay: Long = 100

        Timer().schedule(delay) {
            changeActivity()
        }
    }

    private fun changeActivity(){
        startActivity<MainScreen>()
        finish()
    }
}