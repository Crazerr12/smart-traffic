package com.massindustries.smarttraffic.domain.model

import java.time.LocalTime

data class TrafficPhase(
    val id: Int,
    val duration: Int,
    val cooldown: Int,
    val startTime: LocalTime
)
