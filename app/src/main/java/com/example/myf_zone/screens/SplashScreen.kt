package com.example.myf_zone.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.concurrent.schedule

class SplashScreen : AppCompatActivity() {
    private val TAG = SplashScreen::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        CoroutineScope(IO).launch {
//            val sportList = getSportList()
//            val sportId = getSportId(sportList[0].name)
//
//            val categoryList = CategoryUtil.strGetCategoryList(sportId)
//            Log.d("SportUtil", "Category list:  $categoryList")
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