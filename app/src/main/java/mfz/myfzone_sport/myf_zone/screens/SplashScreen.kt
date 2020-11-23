package mfz.myfzone_sport.myf_zone.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class SplashScreen : AppCompatActivity() {
    private val TAG = SplashScreen::class.java.simpleName

//    var globalEventList = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//
//        CoroutineScope(IO).launch {
//            globalEventList = getEventsByDate()!!
//        }


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