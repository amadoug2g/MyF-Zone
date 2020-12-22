package mfz.myfzone_sport.myf_zone.model.notification

import mfz.myfzone_sport.myf_zone.util.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by Amadou on 20/12/2020
 */

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api: NotificationAPI by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }
}