package com.example.satmeasure.ui.map

import com.mapbox.geojson.Point
import kotlin.math.*

object MathHelpers {

    fun calculateCenter(points: List<Point>): Point {
        if (points.isEmpty()) return Point.fromLngLat(0.0, 0.0)
        var lat = 0.0
        var lng = 0.0
        for (p in points) {
            lat += p.latitude()
            lng += p.longitude()
        }
        return Point.fromLngLat(lng / points.size, lat / points.size)
    }

    /**
     * Calculates the destination point given a start point, distance (meters), and bearing (degrees).
     */
    fun destinationPoint(start: Point, distanceMeters: Double, bearingDegrees: Double): Point {
        val r = 6371000.0 // Earth's radius in meters
        val lat1 = Math.toRadians(start.latitude())
        val lon1 = Math.toRadians(start.longitude())
        val brng = Math.toRadians(bearingDegrees)

        val lat2 = asin(sin(lat1) * cos(distanceMeters / r) + cos(lat1) * sin(distanceMeters / r) * cos(brng))
        val lon2 = lon1 + atan2(sin(brng) * sin(distanceMeters / r) * cos(lat1), cos(distanceMeters / r) - sin(lat1) * sin(lat2))

        return Point.fromLngLat(Math.toDegrees(lon2), Math.toDegrees(lat2))
    }

    /**
     * Calculates distance between two points in meters.
     */
    fun distanceInMeters(p1: Point, p2: Point): Double {
        val r = 6371000.0 // Earth's radius in meters
        val lat1 = Math.toRadians(p1.latitude())
        val lat2 = Math.toRadians(p2.latitude())
        val dLat = Math.toRadians(p2.latitude() - p1.latitude())
        val dLon = Math.toRadians(p2.longitude() - p1.longitude())

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    fun generateCirclePolygon(center: Point, radiusMeters: Double): List<Point> {
        val points = mutableListOf<Point>()
        for (i in 0 until 64) {
            val angle = (360.0 / 64) * i
            points.add(destinationPoint(center, radiusMeters, angle))
        }
        return points
    }
}
