package com.massindustries.smarttraffic.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.massindustries.smarttraffic.utils.UserLocation
import com.yandex.mapkit.mapview.MapView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.massindustries.smarttraffic.utils.FakeTrafficLights
import com.massindustries.smarttraffic.utils.MainDrivingRouteListener
import com.massindustries.smarttraffic.utils.RouteCreator
import com.massindustries.smarttraffic.utils.SpeedCalculator
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Purpose
import com.yandex.mapkit.location.SubscriptionSettings
import com.yandex.mapkit.location.UseInBackground

@Composable
fun MainView(modifier: Modifier = Modifier, mapView: MapView) {
    val context = LocalContext.current
    val locationManager = MapKitFactory.getInstance().createLocationManager()
    val userLocation by remember {
        mutableStateOf(
            UserLocation(
                map = mapView.mapWindow.map,
                context = context
            )
        )
    }
    val routeCreator by remember { mutableStateOf(RouteCreator(map = mapView)) }
    val drivingRouteListener by remember { mutableStateOf(MainDrivingRouteListener(mapView = mapView)) }
    val speedCalculator by remember {
        mutableStateOf(
            SpeedCalculator(
                locationManager = locationManager,
                map = mapView.mapWindow.map
            )
        )
    }

    val speed by speedCalculator.recommendedSpeed.collectAsState()
    val mainRoute by drivingRouteListener.mainRoute.collectAsState()

    locationManager.subscribeForLocationUpdates(
        SubscriptionSettings(UseInBackground.DISALLOW, Purpose.AUTOMOTIVE_NAVIGATION),
        userLocation
    )

    LaunchedEffect(mainRoute) {
        if (mainRoute != null) {
            speedCalculator.subscribeAtSpeed(
                mainRoute = mainRoute,
                trafficLights = FakeTrafficLights
            )
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { userLocation.setIsFollow(!userLocation.getIsFollow()) }) { }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            InputFields { points ->
                routeCreator.createRoute(
                    points = points,
                    drivingRouteListener = drivingRouteListener
                )

                locationManager.unsubscribe(userLocation)
            }

            YandexMapView(modifier = Modifier.weight(1f), mapView = mapView)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (speed != null) {
                    "Рекомендуемая скорость: ${speed!!.toInt()} км/ч"
                } else {
                    "Тебе не успеть ;)"
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun YandexMapView(modifier: Modifier = Modifier, mapView: MapView) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            mapView
        },
    )
}

@Composable
private fun ColumnScope.InputFields(onCreateRoute: (List<RequestPoint>) -> Unit) {
    var text1 by remember { mutableStateOf("54.18637462008575,45.15243988239639") }
    var text2 by remember { mutableStateOf("54.183631104465995,45.20295506249278") }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        value = text1,
        onValueChange = { text1 = it },
        placeholder = { Text(text = "Введите координаты") },
    )

    Spacer(modifier = Modifier.height(10.dp))

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        value = text2,
        onValueChange = { text2 = it },
        placeholder = { Text(text = "Введите координаты") },
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = {
            val doubleArray1 = text1.split(", ", ",").map { it.toDouble() }
            val doubleArray2 = text2.split(", ", ",").map { it.toDouble() }
            val routes = buildList {
                add(
                    RequestPoint(
                        Point(doubleArray1[0], doubleArray1[1]),
                        RequestPointType.WAYPOINT,
                        null,
                        null,
                        null
                    )
                )
                add(
                    RequestPoint(
                        Point(doubleArray2[0], doubleArray2[1]),
                        RequestPointType.WAYPOINT,
                        null,
                        null,
                        null
                    )
                )
            }
            onCreateRoute(routes)
        },
    ) {
        Text(text = "Построить маршрут")
    }

    Spacer(modifier = Modifier.height(12.dp))
}