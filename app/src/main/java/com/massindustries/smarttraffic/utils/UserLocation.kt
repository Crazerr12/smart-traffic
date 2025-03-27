package com.massindustries.smarttraffic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.massindustries.smarttraffic.R
import kotlinx.coroutines.launch

private const val TAG = "User location"

class UserLocation(
    private var map: Map,
    private val context: Context,
) : LocationListener {

    private var userPlacemark: PlacemarkMapObject? = null
    private var isFollow = false
    private var updateJob: Job? = null
    private var isTimeToUpdate = true

    override fun onLocationUpdated(location: Location) {
        Log.d(TAG, "Current position: ${location.position}")

        updateUserLocationMarker(location.position)

        if (isFollow && isTimeToUpdate) {
            moveCamera(location.position)
            isTimeToUpdate = false
        }
    }

    override fun onLocationStatusUpdated(p0: LocationStatus) {
        Log.i(TAG, "New location status is ${p0.name}")
    }

    fun setIsFollow(isFollow: Boolean) {
        this.isFollow = isFollow
        if (isFollow) {
            startObserveCameraPosition()
        } else {
            stopObserveCameraPosition()
        }
    }

    fun getIsFollow(): Boolean = isFollow

    fun setMap(map: Map) {
        this.map = map
    }

    private fun startObserveCameraPosition() {
        updateJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                isTimeToUpdate = true
                delay(2000L)
            }
        }
    }

    private fun stopObserveCameraPosition() {
        updateJob?.cancel()
    }

    private fun moveCamera(point: Point) {
        map.move(
            CameraPosition(point, 16f, 0f, 30f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    private fun updateUserLocationMarker(point: Point) {
        if (userPlacemark == null) {
            userPlacemark = map.mapObjects.addPlacemark(
                point,
                ImageProvider.fromBitmap(context.getBitmapFromVectorDrawable(R.drawable.ic_my_location))
            )
        } else {
            userPlacemark?.geometry = point
        }
    }

    fun clearLocationMarker() {
        userPlacemark?.let {
            map.mapObjects.remove(it)
            userPlacemark = null
        }
    }

    fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(this, drawableId) ?: return null

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ) ?: return null
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}