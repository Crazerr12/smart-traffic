package com.massindustries.smarttraffic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.massindustries.smarttraffic.ui.theme.SmartTrafficTheme
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.PolylinePosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.massindustries.smarttraffic.ui.main.MainView
import com.yandex.mapkit.directions.driving.ConditionsListener
import com.yandex.mapkit.directions.driving.internal.DrivingRouteBinding
import com.yandex.mapkit.geometry.geo.PolylineIndex.Priority
import com.yandex.mapkit.geometry.geo.PolylineUtils
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import com.yandex.mapkit.location.SubscriptionSettings
import com.yandex.mapkit.location.UseInBackground

class MainActivity : ComponentActivity() {
    private lateinit var yandexMapView: MapView

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted =
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            val coarseLocationGranted =
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

            if (fineLocationGranted && coarseLocationGranted) {
                Log.i("Request permission launcher", "All location permissions granted")
            } else if (fineLocationGranted) {
                Log.i("Request permission launcher", "Fine location permission granted")
            } else {
                Log.i("Request permission launcher", "permissions doesn't granted")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapKitFactory.initialize(this)
        yandexMapView = MapView(this)

        askLocationPermission {
            setContent {
                SmartTrafficTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MainView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            mapView = yandexMapView
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        yandexMapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        yandexMapView.onStop()
    }

    private fun askLocationPermission(onGranted: () -> Unit) {
        Log.i("Ask permission", "check for fine and coarse permissions")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("Ask permission", "permissions already granted")
            onGranted()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

            onGranted()
        }
    }
}

fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
    var drawable = ContextCompat.getDrawable(this, drawableId) ?: return null

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

private fun getNearestCoordinatesOnTheRoute(
    currentCoordinates: Point,
    routePoints: List<Point>
): Point {
    return routePoints.first { it.latitude >= currentCoordinates.latitude && it.longitude >= currentCoordinates.longitude }
}

fun distanceBetweenPointsOnRoute(route: DrivingRoute, first: Point, second: Point): Double {
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