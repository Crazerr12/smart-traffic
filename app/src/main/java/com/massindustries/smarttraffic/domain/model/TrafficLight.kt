package com.massindustries.smarttraffic.domain.model

import java.time.LocalTime

data class TrafficLight(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val phases: List<TrafficPhase>,
    val routes: List<CrossroadRoute>,
    val endTime: LocalTime,
)