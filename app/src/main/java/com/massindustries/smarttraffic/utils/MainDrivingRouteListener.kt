package com.massindustries.smarttraffic.utils

import android.util.Log
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.PolylinePosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "Driving route listener"

class MainDrivingRouteListener(
    private val mapView: MapView
) : DrivingSession.DrivingRouteListener {
    private val _mainRoute: MutableStateFlow<DrivingRoute?> = MutableStateFlow(null)
    val mainRoute: StateFlow<DrivingRoute?> = _mainRoute.asStateFlow()

    override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
        _mainRoute.update {
            calculateCheapestRoute(routes = drivingRoutes)
        }

        if (mainRoute.value != null) {
            mapView.mapWindow.map.mapObjects.addPolyline(mainRoute.value!!.geometry)

            val trafficLightsPoints = getTrafficLightsPoints(
                trafficLightsPolylinePositions = mainRoute.value!!.trafficLights.map { it.position },
                routePoints = mainRoute.value!!.geometry.points
            )
        }
    }

    override fun onDrivingRoutesError(error: Error) {
        Log.e(TAG, error.toString())
    }

    private fun getTrafficLightsPoints(
        trafficLightsPolylinePositions: List<PolylinePosition>,
        routePoints: List<Point>,
    ): List<Point> = trafficLightsPolylinePositions.map { routePoints[it.segmentIndex] }

    private fun calculateCheapestRoute(routes: List<DrivingRoute?>): DrivingRoute? {
        var cost = Double.MAX_VALUE

        for (route in routes) {
            if (route != null) {
                val trafficLightSize = route.trafficLights.size
                val hundredMeters = route.metadata.weight.distance.value / 100

                val routeCost = trafficLightSize * 3 + hundredMeters

                if (routeCost < cost) {
                    cost = routeCost
                    return route
                }
            }
        }

        return null
    }
}