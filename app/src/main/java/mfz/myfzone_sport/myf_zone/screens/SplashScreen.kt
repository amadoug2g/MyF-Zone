package mfz.myfzone_sport.myf_zone.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventsByDate
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.globalEventList
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class SplashScreen : AppCompatActivity() {
    companion object {
        private val TAG = SplashScreen::class.java.simpleName
    }

//    var globalEventList = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(IO).launch {
            val delay: Long = 0
            globalEventList = getEventsByDate()!!

            Timer().schedule(delay) {
                changeActivity()
            }
        }
    }

    private fun changeActivity(){
        startActivity<MainScreen>()
        finish()
    }
}