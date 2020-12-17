package mfz.myfzone_sport.myf_zone.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.fragments.calendar.CalendarService.getEventsByDate
import mfz.myfzone_sport.myf_zone.fragments.calendar.CalendarService.globalEventList
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class SplashScreen : AppCompatActivity() {
    companion object {
        private val TAG = SplashScreen::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val delay: Long = 100
        Timer().schedule(delay) {
            changeActivity()
        }
        CoroutineScope(IO).launch {
            globalEventList = getEventsByDate()!!
        }
    }

    private fun changeActivity(){
        startActivity<MainScreen>()
        finish()
    }
}