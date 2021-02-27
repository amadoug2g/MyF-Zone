package com.myfzone_sport.myf_zone.model.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.myfzone_sport.myf_zone.model.event.Event

/**
 * Created by Amadou on 03/02/2021, 13:39
 *
 * : converts Drawable to Bitmap
 *
 */

object BitmapHelper {
    /**
     * Demonstrates converting a [Drawable] to a [BitmapDescriptor],
     * for use as a marker icon. Taken from ApiDemos on GitHub:
     * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/kotlin/app/src/main/java/com/example/kotlindemos/MarkerDemoActivity.kt
     */

    fun vectorToBitmap(
        context: Context,
        @DrawableRes id: Int,
        @ColorInt color: Int
    ): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(context.resources, id, null)
        if (vectorDrawable == null) {
            Log.e("BitmapHelper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        Log.i("BitmapHelper", "Resource found")
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun eventToBitmap(
        context: Context,
        event: Event
    ): BitmapDescriptor {
        val vectorDrawable =
            ResourcesCompat.getDrawable(context.resources, event.eventTypeImage, null)
        if (vectorDrawable == null) {
            Log.e("BitmapHelper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun drawableToBitmap(
        drawable: Drawable
    ): BitmapDescriptor {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
//        DrawableCompat.setTint(drawable, color)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}