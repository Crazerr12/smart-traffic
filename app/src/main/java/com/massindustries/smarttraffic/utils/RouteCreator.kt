package com.massindustries.smarttraffic.utils

import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.directions.driving.VehicleType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error

class RouteCreator(
    private val map: MapView,
) {
    fun createRoute(
        points: List<RequestPoint>,
        drivingRouteListener: DrivingSession.DrivingRouteListener
    ) {
        val drivingRouter =
            DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)

        val drivingOptions = DrivingOptions().apply {
            routesCount = 5
        }

        val vehicleOptions = VehicleOptions().apply {
            vehicleType = VehicleType.DEFAULT
        }

        val drivingSession = drivingRouter.requestRoutes(
            points,
            drivingOptions,
            vehicleOptions,
            drivingRouteListener
        )
    }
}