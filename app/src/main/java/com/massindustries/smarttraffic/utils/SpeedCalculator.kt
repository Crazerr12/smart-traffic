package com.massindustries.smarttraffic.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.massindustries.smarttraffic.domain.model.TrafficLight
import com.yandex.mapkit.Animation
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.geo.PolylineIndex.Priority
import com.yandex.mapkit.geometry.geo.PolylineUtils
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import com.yandex.mapkit.location.SubscriptionSettings
import com.yandex.mapkit.location.UseInBackground
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val TAG = "Speed calculator"

class SpeedCalculator(
    private val locationManager: LocationManager,
    private var map: Map,
) {
    private val _recommendedSpeed: MutableStateFlow<Double?> = MutableStateFlow(0.0)
    val recommendedSpeed: StateFlow<Double?> = _recommendedSpeed.asStateFlow()

    private var currentTrafficIndex: Int? = null

    private var isFollow = false
    private var updateJob: Job? = null
    private var isTimeToUpdate = true

    private var mainRoute: DrivingRoute? = null
    private var trafficLights: List<TrafficLight> = emptyList()

    private val locationListener = object : LocationListener {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onLocationUpdated(p0: Location) {
            Log.d(
                TAG,
                "Current latitude and longitude: ${p0.position.latitude}, ${p0.position.longitude}"
            )

            if (isFollow && isTimeToUpdate) {
                map.move(
                    CameraPosition(
                        p0.position,
                        16f,
                        0f,
                        30f,
                    ),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
                isTimeToUpdate = false
            }

            if (mainRoute != null) {
                val position = mainRoute!!.position

                var nearestPositionIndex = -1
                var minDistance = Double.MAX_VALUE

                for ((index, point) in mainRoute!!.geometry.points.withIndex()) {
                    val distance = distanceBetweenPoints(p0.position, point)
                    if (distance < minDistance) {
                        minDistance = distance
                        nearestPositionIndex = index
                    }
                }

                currentTrafficIndex =
                    mainRoute!!.trafficLights.firstOrNull { it.position.segmentIndex > nearestPositionIndex }?.position?.segmentIndex

                if (currentTrafficIndex != null) {
                    val trafficPoint = mainRoute!!.geometry.points[currentTrafficIndex!!]

                    val neededTrafficLight =
                        trafficLights.firstOrNull() { it.latitude == trafficPoint.latitude && it.longitude == trafficPoint.longitude }

                    if (neededTrafficLight != null) {
                        val nextPoint = mainRoute!!.geometry.points[currentTrafficIndex!! + 1]
                        val prevPoint = mainRoute!!.geometry.points[currentTrafficIndex!! - 1]

                        val neededRoute =
                            neededTrafficLight.routes.first {
                                it.startLatitude == prevPoint.latitude && it.startLongitude == prevPoint.longitude
                                        && it.endLatitude == nextPoint.latitude && it.endLongitude == nextPoint.longitude
                            }

                        val neededPhase = neededRoute.neededPhase

                        val distance = distanceBetweenPointsOnRoute(
                            route = mainRoute!!,
                            first = mainRoute!!.geometry.points[position.segmentIndex],
                            second = Point(
                                neededTrafficLight.latitude,
                                neededTrafficLight.longitude
                            )
                        )

                        Log.d(TAG, "Current distance to the nearest traffic light: $distance")

                        val startTime = neededPhase.startTime
                        val timeNow = LocalTime.now()

                        var tempSpeed: Double? = null

                        for (speed in listOf(
                            16.67, //60
                            15.28, //55
                            13.89, //50
                            12.5, //45
                            11.11, //40
                            9.72, // 35
                            8.33 //30
                        )) {
                            Log.d(
                                TAG, "Осталось времени: ${
                                    (Duration.between(startTime, timeNow)
                                        .toSeconds() % (neededPhase.duration + neededPhase.cooldown + (distance / speed))).toDouble()
                                }"
                            )

                            val time = ((abs(
                                Duration.between(startTime, timeNow)
                                    .toSeconds()
                            ) % (neededPhase.duration + neededPhase.cooldown)).toDouble())

                            if ((time - neededPhase.cooldown + (distance / speed)) in (0.0..neededPhase.duration - 5.0)
                            ) {
                                tempSpeed = speed * 3.6
                                break
                            } else {
                                tempSpeed = null
                            }
                        }
                        _recommendedSpeed.update { tempSpeed }
                    }
                }
            }
        }

        override fun onLocationStatusUpdated(p0: LocationStatus) {
            Log.i(TAG, "New location status is ${p0.name}")
        }
    }

    fun subscribeAtSpeed(mainRoute: DrivingRoute?, trafficLights: List<TrafficLight>) {
        this.mainRoute = mainRoute
        this.trafficLights = trafficLights

        locationManager.subscribeForLocationUpdates(
            SubscriptionSettings(
                UseInBackground.DISALLOW,
                Purpose.AUTOMOTIVE_NAVIGATION
            ), locationListener
        )
    }

    private fun distanceBetweenPointsOnRoute(
        route: DrivingRoute,
        first: Point,
        second: Point
    ): Double {
        val polylineIndex = PolylineUtils.createPolylineIndex(route.geometry)
        val firstPosition =
            polylineIndex.closestPolylinePosition(first, Priority.CLOSEST_TO_RAW_POINT, 1.0)!!
        val secondPosition =
            polylineIndex.closestPolylinePosition(second, Priority.CLOSEST_TO_RAW_POINT, 1.0)!!
        return PolylineUtils.distanceBetweenPolylinePositions(
            route.geometry,
            firstPosition,
            secondPosition
        )
    }

    fun setIsFollow(isFollow: Boolean) {
        this.isFollow = isFollow
        if (isFollow) {
            startObserveCameraPosition()
        } else {
            stopObserveCameraPosition()
        }
    }

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

    // Функция для расчета расстояния между двумя точками
    fun distanceBetweenPoints(point1: Point, point2: Point): Double {
        val earthRadius = 6371000 // Радиус Земли в метрах
        val dLat = Math.toRadians(point2.latitude - point1.latitude)
        val dLon = Math.toRadians(point2.longitude - point1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(point1.latitude)) * Math.cos(Math.toRadians(point2.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}