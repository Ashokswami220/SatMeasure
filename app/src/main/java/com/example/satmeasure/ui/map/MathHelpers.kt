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
        val bearing = Math.toRadians(bearingDegrees)

        val lat2 = asin(
            sin(lat1) * cos(distanceMeters / r) + cos(lat1) * sin(distanceMeters / r) * cos(bearing)
        )
        val lon2 = lon1 + atan2(
            sin(bearing) * sin(distanceMeters / r) * cos(lat1),
            cos(distanceMeters / r) - sin(lat1) * sin(lat2)
        )

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

    /**
     * Calculates the initial bearing from point p1 to point p2 in degrees.
     */
    fun calculateBearing(p1: Point, p2: Point): Double {
        val lat1 = Math.toRadians(p1.latitude())
        val lat2 = Math.toRadians(p2.latitude())
        val dLon = Math.toRadians(p2.longitude() - p1.longitude())

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = atan2(y, x)
        return (Math.toDegrees(bearing) + 360) % 360
    }

    /**
     * Rotates a point around a center point by a given angle in degrees.
     */
    fun rotatePoint(center: Point, point: Point, angleDegrees: Double): Point {
        val distance = distanceInMeters(center, point)
        if (distance == 0.0) return point
        val currentBearing = calculateBearing(center, point)
        val newBearing = (currentBearing + angleDegrees + 360) % 360
        return destinationPoint(center, distance, newBearing)
    }

    /**
     * Calculates the geographic midpoint between two points.
     */
    fun midPoint(p1: Point, p2: Point): Point {
        val dLon = Math.toRadians(p2.longitude() - p1.longitude())
        val lat1 = Math.toRadians(p1.latitude())
        val lat2 = Math.toRadians(p2.latitude())
        val lon1 = Math.toRadians(p1.longitude())

        val bx = cos(lat2) * cos(dLon)
        val by = cos(lat2) * sin(dLon)
        val lat3 = atan2(sin(lat1) + sin(lat2), sqrt((cos(lat1) + bx) * (cos(lat1) + bx) + by * by))
        val lon3 = lon1 + atan2(by, cos(lat1) + bx)

        return Point.fromLngLat(Math.toDegrees(lon3), Math.toDegrees(lat3))
    }

    /**
     * Calculates Area (m^2) and Perimeter (m) for a given list of geographic points.
     */
    fun calculatePolygonMeasurements(points: List<Point>): Pair<Double, Double> {
        if (points.size < 3) return Pair(0.0, calculatePerimeter(points))

        var area = 0.0
        val r = 6378137.0 // Earth radius in meters WGS84

        // Ensure closed polygon
        val pts = if (points.first() != points.last()) points + points.first() else points

        if (pts.size > 2) {
            var sum = 0.0
            for (i in 0 until pts.size - 1) {
                val p1 = pts[i]
                val p2 = pts[i + 1]
                val x1 = Math.toRadians(p1.longitude())
                val y1 = Math.toRadians(p1.latitude())
                val x2 = Math.toRadians(p2.longitude())
                val y2 = Math.toRadians(p2.latitude())

                sum += (x2 - x1) * (2 + sin(y1) + sin(y2))
            }
            area = abs(sum * r * r / 2.0)
        }

        return Pair(area, calculatePerimeter(points))
    }

    /**
     * Calculates the perimeter (length) of the polygon in meters.
     */
    fun calculatePerimeter(points: List<Point>): Double {
        if (points.size < 2) return 0.0
        var perimeter = 0.0
        for (i in 0 until points.size - 1) {
            perimeter += distanceInMeters(points[i], points[i + 1])
        }
        if (points.size > 2) {
            perimeter += distanceInMeters(points.last(), points.first())
        }
        return perimeter
    }
}
