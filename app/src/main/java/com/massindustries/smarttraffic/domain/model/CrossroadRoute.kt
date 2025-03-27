package com.massindustries.smarttraffic.domain.model

data class CrossroadRoute(
    val id: Int,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double,
    val endLongitude: Double,
    val neededPhase: TrafficPhase,
)