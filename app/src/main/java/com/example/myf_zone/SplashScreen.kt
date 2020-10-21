package com.example.myf_zone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val delay: Long = 1500

        Timer().schedule(delay) {
            changeActivity()
        }
    }

    private fun changeActivity(){
        startActivity<MainActivity>()
        finish()
    }
}