package com.example.satmeasure.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MeasurementRecord(
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var timestamp: Long = 0L,
    var areaSqMeters: Double = 0.0,
    var perimeterMeters: Double = 0.0,
    var centerPoint: PointData? = null,
    var points: List<PointData> = emptyList()
)

@IgnoreExtraProperties
data class PointData(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)
