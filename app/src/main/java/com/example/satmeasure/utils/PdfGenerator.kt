package com.example.satmeasure.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.satmeasure.model.PointData
import com.example.satmeasure.ui.map.AreaUnit
import com.example.satmeasure.ui.map.MeasurementConverter
import com.mapbox.maps.Snapshotter

import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapSnapshotOptions
import com.mapbox.maps.Size
import com.mapbox.maps.EdgeInsets
import com.mapbox.geojson.Point
import com.example.satmeasure.ui.components.PdfExportOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfGenerator {
    suspend fun generatePdf(
        context: Context,
        options: PdfExportOptions,
        areaMeters: Double,
        perimeterMeters: Double,
        points: List<PointData>,
        centerLat: Double,
        centerLng: Double,
        zoom: Double,
        mapStyle: String
    ): File {
        // 1. Generate Mapbox Snapshot
        val snapshotBitmap = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Bitmap?> { continuation ->
                try {
                    val snapshotterOptions = MapSnapshotOptions.Builder()
                        .size(Size(800.0f, 800.0f))
                        .pixelRatio(2.0f)
                        .build()
                    
                    val snapshotter = Snapshotter(context, snapshotterOptions)
                    snapshotter.setStyleUri(mapStyle)
                    val camera = if (points.size >= 2) {
                        snapshotter.cameraForCoordinates(coordinates = points.map { Point.fromLngLat(it.lng, it.lat) }, padding = EdgeInsets(80.0, 80.0, 80.0, 80.0), bearing = 0.0, pitch = 0.0)
                    } else {
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(centerLng, centerLat))
                            .zoom(zoom) // Use passed zoom directly
                            .build()
                    }
                    snapshotter.setCamera(camera)
                    
                    snapshotter.start { snapshot, _ ->
                        // Determine the actual zoom and center used by the snapshotter
                        val finalZoom = camera.zoom ?: zoom
                        val finalCenter = camera.center?.let { PointData(it.latitude(), it.longitude()) } ?: PointData(centerLat, centerLng)
                        
                        if (snapshot != null && points.isNotEmpty()) {
                            // Draw the polygon on the bitmap using Spherical Mercator projection
                            val mutableBitmap = snapshot.copy(Bitmap.Config.ARGB_8888, true)
                            val canvas = Canvas(mutableBitmap)
                            val paint = Paint().apply {
                                color = Color.parseColor("#3bb2d0")
                                strokeWidth = 5f
                                style = Paint.Style.STROKE
                                isAntiAlias = true
                            }
                            val fillPaint = Paint().apply {
                                color = Color.parseColor("#4D3bb2d0") // 30% opacity
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val dotPaint = Paint().apply {
                                color = Color.WHITE
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val dotStrokePaint = Paint().apply {
                                color = Color.parseColor("#3bb2d0")
                                strokeWidth = 3f
                                style = Paint.Style.STROKE
                                isAntiAlias = true
                            }

                            val width = mutableBitmap.width.toDouble()
                            val height = mutableBitmap.height.toDouble()

                            // Projection math
                            val tileSize = 512.0
                            val scale = Math.pow(2.0, finalZoom)
                            val worldSize = tileSize * scale

                            fun latLngToPixel(lat: Double, lng: Double): Pair<Double, Double> {
                                val x = (lng + 180.0) / 360.0 * worldSize
                                val latRad = Math.toRadians(lat)
                                val y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * worldSize
                                return Pair(x, y)
                            }

                            val centerPixel = latLngToPixel(finalCenter.lat, finalCenter.lng)

                            val path = android.graphics.Path()
                            var firstPoint: Pair<Double, Double>? = null

                            points.forEachIndexed { index, pt ->
                                val px = latLngToPixel(pt.lat, pt.lng)
                                val screenX = (px.first - centerPixel.first + width / 2.0).toFloat()
                                val screenY = (px.second - centerPixel.second + height / 2.0).toFloat()

                                if (index == 0) {
                                    path.moveTo(screenX, screenY)
                                    firstPoint = Pair(screenX.toDouble(), screenY.toDouble())
                                } else {
                                    path.lineTo(screenX, screenY)
                                }
                            }
                            
                            if (points.size >= 3) {
                                path.close()
                                canvas.drawPath(path, fillPaint)
                            }
                            canvas.drawPath(path, paint)

                            // Draw dots
                            points.forEach { pt ->
                                val px = latLngToPixel(pt.lat, pt.lng)
                                val screenX = (px.first - centerPixel.first + width / 2.0).toFloat()
                                val screenY = (px.second - centerPixel.second + height / 2.0).toFloat()
                                canvas.drawCircle(screenX, screenY, 8f, dotPaint)
                                canvas.drawCircle(screenX, screenY, 8f, dotStrokePaint)
                            }

                            continuation.resume(mutableBitmap)
                        } else {
                            continuation.resume(snapshot)
                        }
                    }
                    
                    continuation.invokeOnCancellation {
                        snapshotter.cancel()
                    }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }
        }

        // 2. Generate PDF
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var currentPage = document.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        val paint = Paint()

        var currentY = 50f
        
        fun checkPageBreak(requiredHeight: Float) {
            if (currentY + requiredHeight > 800f) {
                document.finishPage(currentPage)
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                currentY = 50f
            }
        }

        // Draw Header
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("SatMeasure - Measurement Report", 50f, currentY, paint)
        currentY += 30f

        // Draw Date and Name
        paint.textSize = 16f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        canvas.drawText("Date: $dateString", 50f, currentY, paint)
        currentY += 55f
        
        // Draw Summary Stats
        paint.textSize = 14f

        if (options.includePerimeter) {
            checkPageBreak(120f)
            paint.isFakeBoldText = true
            canvas.drawText("--- Perimeter ---", 50f, currentY, paint)
            paint.isFakeBoldText = false
            currentY += 20f

            val km = perimeterMeters / 1000.0
            val feet = perimeterMeters * 3.28084
            val miles = perimeterMeters * 0.000621371

            canvas.drawText(String.format(Locale.getDefault(), "Meters: %.2f m", perimeterMeters), 50f, currentY, paint)
            currentY += 20f
            canvas.drawText(String.format(Locale.getDefault(), "Kilometers: %.4f km", km), 50f, currentY, paint)
            currentY += 20f
            canvas.drawText(String.format(Locale.getDefault(), "Feet: %.2f ft", feet), 50f, currentY, paint)
            currentY += 20f
            canvas.drawText(String.format(Locale.getDefault(), "Miles: %.4f mi", miles), 50f, currentY, paint)
            currentY += 10f
        }

        val areaSqFt = areaMeters * 10.7639
        
        val globalUnits = listOf(AreaUnit.SquareMeter, AreaUnit.SquareYard, AreaUnit.Acre, AreaUnit.Hectare)
        val bighaUnits = MeasurementConverter.getAllBighaUnits()
        val localUnits = MeasurementConverter.getOtherLocalUnits().map { it.second }

        fun printCategory(title: String, units: List<AreaUnit>) {
            val selectedInCat = units.filter { options.selectedUnits.contains(it) }
            if (selectedInCat.isNotEmpty()) {
                val headerHeight = 30f
                val itemsHeight = selectedInCat.size * 20f
                checkPageBreak(headerHeight + itemsHeight)
                
                paint.isFakeBoldText = true
                canvas.drawText("--- $title ---", 50f, currentY, paint)
                paint.isFakeBoldText = false
                currentY += 20f

                for (unit in selectedInCat) {
                    val displayName = when(unit) {
                        is AreaUnit.Bigha -> "Bigha (${unit.state.displayName})"
                        else -> unit.displayName
                    }
                    val value = if (unit is AreaUnit.SquareMeter) areaMeters else MeasurementConverter.convertArea(areaSqFt, unit)
                    val str = String.format(Locale.getDefault(), "%s: %.4f", displayName, value)
                    canvas.drawText(str, 50f, currentY, paint)
                    currentY += 20f
                }
                currentY += 10f // gap after category
            }
        }

        printCategory("Global Units", globalUnits)
        printCategory("Bigha Units", bighaUnits)
        printCategory("Local Units", localUnits)
        
        // Draw Map Snapshot
        if (snapshotBitmap != null) {
            val scale = 400f / snapshotBitmap.width
            val scaledWidth = (snapshotBitmap.width * scale).toInt()
            val scaledHeight = (snapshotBitmap.height * scale).toInt()
            
            checkPageBreak(scaledHeight + 30f)
            
            val scaledBitmap = Bitmap.createScaledBitmap(snapshotBitmap, scaledWidth, scaledHeight, true)
            
            val xPos = (595f - scaledWidth) / 2f
            canvas.drawBitmap(scaledBitmap, xPos, currentY, paint)
            currentY += scaledHeight + 30f
        }

        // Draw Coordinates List
        if (options.includeCoordinates) {
            checkPageBreak(40f)
            paint.isFakeBoldText = true
            paint.textSize = 16f
            canvas.drawText("Coordinates (${points.size} points):", 50f, currentY, paint)
            paint.isFakeBoldText = false
            paint.textSize = 12f
            currentY += 20f
            
            points.forEachIndexed { i, pt ->
                checkPageBreak(15f)
                val coordStr = String.format(Locale.getDefault(), "P%d: %.6f, %.6f", i+1, pt.lat, pt.lng)
                canvas.drawText(coordStr, 50f, currentY, paint)
                currentY += 15f
            }
        }

        document.finishPage(currentPage)
        
        val file = File(context.cacheDir, "${options.name.replace(' ', '_')}.pdf")
        withContext(Dispatchers.IO) {
            document.writeTo(FileOutputStream(file))
        }
        document.close()
        
        return file
    }
}
