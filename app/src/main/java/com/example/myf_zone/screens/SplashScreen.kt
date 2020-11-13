package com.example.myf_zone.screens

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myf_zone.util.StorageUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class SplashScreen : AppCompatActivity() {
    private val TAG = SplashScreen::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(IO).launch {
            Log.d("SplashScreenTAGG", "Club list:  ${StorageUtil.clubList}")
        }


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