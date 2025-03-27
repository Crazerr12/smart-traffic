package com.massindustries.smarttraffic.utils

import com.massindustries.smarttraffic.domain.model.CrossroadRoute
import com.massindustries.smarttraffic.domain.model.TrafficLight
import com.massindustries.smarttraffic.domain.model.TrafficPhase
import java.time.LocalTime

val FakePhases = listOf(
    TrafficPhase(
        id = 1,
        duration = 32,
        cooldown = 67,
        startTime = LocalTime.of(20, 29, 58)
    ),
    TrafficPhase(
        id = 2,
        duration = 27,
        cooldown = 62,
        startTime = LocalTime.of(21, 7, 5)
    ),
    TrafficPhase(
        id = 3,
        duration = 32,
        cooldown = 72,
        startTime = LocalTime.of(21, 14, 18)
    ),
    TrafficPhase(
        id = 4,
        duration = 40,
        cooldown = 74,
        startTime = LocalTime.of(21, 15, 28)
    ),
    TrafficPhase(
        id = 5,
        duration = 40,
        cooldown = 55,
        startTime = LocalTime.of(8, 0, 11)
    ),
    TrafficPhase(
        id = 6,
        duration = 35,
        cooldown = 90,
        startTime = LocalTime.of(8, 0, 42)
    ),
    TrafficPhase(
        id = 7,
        duration = 50,
        cooldown = 60,
        startTime = LocalTime.of(7, 59, 20)
    ),
)

val FakeRoutes = listOf(
    CrossroadRoute(
        id = 1,
        startLatitude = 54.185523,
        startLongitude = 45.155787,
        endLatitude = 54.18551,
        endLongitude = 45.156178,
        neededPhase = FakePhases[0]
    ),
    CrossroadRoute(
        id = 2,
        startLatitude = 54.185264,
        startLongitude = 45.161163,
        endLatitude = 54.185246,
        endLongitude = 45.16155,
        neededPhase = FakePhases[1]
    ),
    CrossroadRoute(
        id = 3,
        startLatitude = 54.184766,
        startLongitude = 45.171564,
        endLatitude = 54.184766,
        endLongitude = 45.172029,
        neededPhase = FakePhases[2]
    ),
    CrossroadRoute(
        id = 4,
        startLatitude = 54.184586,
        startLongitude = 45.176534,
        endLatitude = 54.184563,
        endLongitude = 45.177085,
        neededPhase = FakePhases[3]
    ),
    CrossroadRoute(
        id = 5,
        startLatitude = 54.184357,
        startLongitude = 45.18208,
        endLatitude = 54.184341,
        endLongitude = 45.182428,
        neededPhase = FakePhases[4]
    ),
    CrossroadRoute(
        id = 6,
        startLatitude = 54.184068,
        startLongitude = 45.187233,
        endLatitude = 54.184043,
        endLongitude = 45.187747,
        neededPhase = FakePhases[5]
    ),
    CrossroadRoute(
        id = 7,
        startLatitude = 54.183654,
        startLongitude = 45.20225,
        endLatitude = 54.183642,
        endLongitude = 45.202956,
        neededPhase = FakePhases[6]
    ),
)

val FakeTrafficLights = listOf<TrafficLight>(
    TrafficLight(
        id = 1,
        latitude = 54.185519,
        longitude = 45.15594,
        phases = emptyList(),
        routes = listOf(FakeRoutes[0]),
        endTime = LocalTime.of(22, 0, 0)
    ),
    TrafficLight(
        id = 2,
        latitude = 54.185254,
        longitude = 45.161382,
        phases = emptyList(),
        routes = listOf(FakeRoutes[1]),
        endTime = LocalTime.of(22, 0, 0)
    ),
    TrafficLight(
        id = 3,
        latitude = 54.184763,
        longitude = 45.171695,
        phases = emptyList(),
        routes = listOf(FakeRoutes[2]),
        endTime = LocalTime.of(22, 0, 0)
    ),
    TrafficLight(
        id = 4,
        latitude = 54.184582,
        longitude = 45.176644,
        phases = emptyList(),
        routes = listOf(FakeRoutes[3]),
        endTime = LocalTime.of(22, 0, 0)
    ),
    TrafficLight(
        id = 5,
        latitude = 54.184348,
        longitude = 45.182275,
        phases = emptyList(),
        routes = listOf(FakeRoutes[4]),
        endTime = LocalTime.of(22, 0, 0)
    ),
    TrafficLight(
        id = 6,
        latitude = 54.184053,
        longitude = 45.187487,
        phases = emptyList(),
        routes = listOf(FakeRoutes[5]),
        endTime = LocalTime.of(22, 0, 0)
    ),
    TrafficLight(
        id = 7,
        latitude = 54.183642,
        longitude = 45.202956,
        phases = emptyList(),
        routes = listOf(FakeRoutes[6]),
        endTime = LocalTime.of(22, 0, 0)
    ),
)