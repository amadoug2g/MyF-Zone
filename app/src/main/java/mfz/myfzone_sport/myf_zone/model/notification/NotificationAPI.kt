package mfz.myfzone_sport.myf_zone.model.notification

import mfz.myfzone_sport.myf_zone.util.Constants.CONTENT_TYPE
import mfz.myfzone_sport.myf_zone.util.Constants.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


/**
 * Created by Amadou on 20/12/2020
 */

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}