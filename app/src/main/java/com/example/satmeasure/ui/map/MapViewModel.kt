package com.example.satmeasure.ui.map

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import com.example.satmeasure.ui.map.models.CalcMode

class MapViewModel : ViewModel() {
    val isTrackingUser = mutableStateOf(false)
    val isCalcExpanded = mutableStateOf(false)
    
    val activeMode = mutableStateOf<CalcMode?>(null)
    val completedMode = mutableStateOf<CalcMode?>(null)

    val pinPoints = mutableStateListOf<Point>()
    val redoPinPoints = mutableStateListOf<Point>()
    
    val drawPoints = mutableStateListOf<Point>()
    val drawPointsHistory = mutableStateListOf<List<Point>>()
    val redoDrawPointsHistory = mutableStateListOf<List<Point>>()
    
    val shapePoints = mutableStateListOf<Point>()
    val shapePointsHistory = mutableStateListOf<List<Point>>()
    val redoShapePointsHistory = mutableStateListOf<List<Point>>()

    val isDrawing = mutableStateOf(false)
    val isShapeDropped = mutableStateOf(false)
    
    val showDiscardDialog = mutableStateOf(false)
    val showClearDialog = mutableStateOf(false)
}
