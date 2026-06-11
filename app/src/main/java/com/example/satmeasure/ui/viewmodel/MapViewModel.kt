package com.example.satmeasure.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.satmeasure.ui.map.models.CalcMode
import com.example.satmeasure.model.MeasurementRecord
import com.example.satmeasure.model.PointData
import com.example.satmeasure.repo.DatabaseRepository
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
data class MapUiState(
    val currentUserLocation: Point? = null,
    val cameraTargetBounds: List<Point>? = null,
    val isCalcExpanded: Boolean = false,
    val activeMode: CalcMode? = null,
    val completedMode: CalcMode? = null,
    
    val pinPoints: List<Point> = emptyList(),
    val redoPinPoints: List<Point> = emptyList(),
    
    val drawPoints: List<Point> = emptyList(),
    val drawPointsHistory: List<List<Point>> = emptyList(),
    val redoDrawPointsHistory: List<List<Point>> = emptyList(),
    
    val shapePoints: List<Point> = emptyList(),
    val shapePointsHistory: List<List<Point>> = emptyList(),
    val redoShapePointsHistory: List<List<Point>> = emptyList(),

    val isDrawing: Boolean = false,
    val isShapeDropped: Boolean = false,
    
    val showDiscardDialog: Boolean = false,
    val showClearDialog: Boolean = false,
    
    val loadedMeasurementName: String? = null,
    val loadedMeasurementId: String? = null,
    val isReadOnly: Boolean = false,
    val pdfExportOptions: com.example.satmeasure.ui.components.PdfExportOptions = com.example.satmeasure.ui.components.PdfExportOptions()
)

sealed class MapAction {
    data class SetCurrentUserLocation(val location: Point?) : MapAction()
    data class SetCameraTargetBounds(val bounds: List<Point>?) : MapAction()
    data class SetCalcExpanded(val expanded: Boolean) : MapAction()
    data class SetActiveMode(val mode: CalcMode?) : MapAction()
    data class SetCompletedMode(val mode: CalcMode?) : MapAction()
    data class SetIsDrawing(val isDrawing: Boolean) : MapAction()
    data class SetShapeDropped(val isDropped: Boolean) : MapAction()
    data class SetShowDiscardDialog(val show: Boolean) : MapAction()
    data class SetShowClearDialog(val show: Boolean) : MapAction()
    data class SetPdfExportOptions(val options: com.example.satmeasure.ui.components.PdfExportOptions) : MapAction()

    // Pin Actions
    data class AddPinPoint(val point: Point) : MapAction()
    data class UpdatePinPoint(val index: Int, val point: Point) : MapAction()
    object RemoveLastPinPoint : MapAction()
    object UndoPin : MapAction()
    object RedoPin : MapAction()
    object ClearPins : MapAction()

    // Draw Actions
    data class AddDrawPoint(val point: Point) : MapAction()
    data class SetDrawPoints(val points: List<Point>) : MapAction()
    data class CommitDrawPath(val newPath: List<Point>) : MapAction()
    object UndoDraw : MapAction()
    object RedoDraw : MapAction()
    object ClearDraw : MapAction()

    // Shape Actions
    data class SetShapePoints(val points: List<Point>) : MapAction()
    data class CommitShapePath(val newPath: List<Point>) : MapAction()
    object UndoShape : MapAction()
    object RedoShape : MapAction()
    object ClearShape : MapAction()
    
    // Global Actions
    object ClearAll : MapAction()
    object ExitReadOnly : MapAction()
}

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val dbRepo = DatabaseRepository()

    private val _savedMeasurements = MutableStateFlow<List<MeasurementRecord>>(emptyList())
    val savedMeasurements: StateFlow<List<MeasurementRecord>> = _savedMeasurements.asStateFlow()

    fun loadMeasurements(userId: String) {
        viewModelScope.launch {
            val result = dbRepo.getMeasurements(userId)
            if (result.isSuccess) {
                _savedMeasurements.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun loadSharedMeasurement(ownerId: String, plotId: String) {
        viewModelScope.launch {
            val result = dbRepo.getSharedMeasurement(ownerId, plotId)
            if (result.isSuccess) {
                result.getOrNull()?.let { record ->
                    loadRecordIntoMap(record, isEditable = false)
                }
            }
        }
    }

    fun loadRecordIntoMap(record: MeasurementRecord, isEditable: Boolean = false) {
        // Convert stored points back to Mapbox Point objects
        val points = record.points.map { Point.fromLngLat(it.lng, it.lat) }
        
        if (isEditable) {
            _uiState.update { it.copy(
                shapePoints = points,
                activeMode = CalcMode.SHAPES,
                completedMode = null,
                isCalcExpanded = true,
                isShapeDropped = true,
                loadedMeasurementName = record.name,
                loadedMeasurementId = record.id,
                isReadOnly = false
            )}
        } else {
            _uiState.update { it.copy(
                shapePoints = points,
                activeMode = null,
                completedMode = CalcMode.SHAPES,
                isCalcExpanded = false,
                isShapeDropped = true,
                loadedMeasurementName = record.name,
                loadedMeasurementId = record.id,
                isReadOnly = true
            )}
        }
        
        // Center camera if there are points
        if (points.isNotEmpty()) {
            onAction(MapAction.SetCameraTargetBounds(points))
        }
    }

    fun deleteMeasurement(userId: String, recordId: String) {
        viewModelScope.launch {
            dbRepo.deleteMeasurement(userId, recordId)
            loadMeasurements(userId) // reload
        }
    }

    fun deleteAllMeasurements(userId: String) {
        viewModelScope.launch {
            dbRepo.deleteAllMeasurements(userId)
            loadMeasurements(userId) // reload
        }
    }

    fun saveMeasurement(
        name: String, 
        userId: String, 
        areaSqMeters: Double, 
        perimeterMeters: Double, 
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val currentPoints = when (_uiState.value.completedMode ?: _uiState.value.activeMode) {
                CalcMode.PINS -> _uiState.value.pinPoints
                CalcMode.DRAW -> _uiState.value.drawPoints
                CalcMode.SHAPES -> _uiState.value.shapePoints
                else -> emptyList()
            }
            
            if (currentPoints.isEmpty()) {
                onResult(false, "No points to save")
                return@launch
            }
            
            val pointDataList = currentPoints.map { PointData(it.latitude(), it.longitude()) }
            val center = pointDataList.firstOrNull()
            
            val record = MeasurementRecord(
                name = name,
                userId = userId,
                timestamp = System.currentTimeMillis(),
                areaSqMeters = areaSqMeters,
                perimeterMeters = perimeterMeters,
                centerPoint = center,
                points = pointDataList,
                id = _uiState.value.loadedMeasurementId ?: ""
            )
            
            val result = dbRepo.saveMeasurement(userId, record)
            if (result.isSuccess) {
                // Clear the loaded name/id so next time it's empty
                _uiState.update { it.copy(loadedMeasurementName = null, loadedMeasurementId = null, isReadOnly = false) }
                onResult(true, "Saved successfully!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to save")
            }
        }
    }

    fun onAction(action: MapAction) {
        when(action) {
            is MapAction.SetCurrentUserLocation -> _uiState.update { it.copy(currentUserLocation = action.location) }
            is MapAction.SetCameraTargetBounds -> _uiState.update { it.copy(cameraTargetBounds = action.bounds) }
            is MapAction.SetCalcExpanded -> _uiState.update { it.copy(isCalcExpanded = action.expanded) }
            is MapAction.SetActiveMode -> _uiState.update { it.copy(activeMode = action.mode) }
            is MapAction.SetCompletedMode -> _uiState.update { it.copy(completedMode = action.mode) }
            is MapAction.SetIsDrawing -> _uiState.update { it.copy(isDrawing = action.isDrawing) }
            is MapAction.SetShapeDropped -> _uiState.update { it.copy(isShapeDropped = action.isDropped) }
            is MapAction.SetShowDiscardDialog -> _uiState.update { it.copy(showDiscardDialog = action.show) }
            is MapAction.SetShowClearDialog -> _uiState.update { it.copy(showClearDialog = action.show) }
            is MapAction.SetPdfExportOptions -> _uiState.update { it.copy(pdfExportOptions = action.options) }
            
            // PIN LOGIC
            is MapAction.AddPinPoint -> _uiState.update { state ->
                val newPins = state.pinPoints + action.point
                state.copy(pinPoints = newPins, redoPinPoints = emptyList())
            }
            is MapAction.UpdatePinPoint -> _uiState.update { state ->
                val newPins = state.pinPoints.toMutableList()
                if (action.index in newPins.indices) {
                    newPins[action.index] = action.point
                }
                state.copy(pinPoints = newPins)
            }
            is MapAction.RemoveLastPinPoint -> _uiState.update { state ->
                if (state.pinPoints.isNotEmpty()) {
                    val newPins = state.pinPoints.dropLast(1)
                    state.copy(pinPoints = newPins)
                } else state
            }
            is MapAction.UndoPin -> _uiState.update { state ->
                if (state.pinPoints.isNotEmpty()) {
                    val last = state.pinPoints.last()
                    val newPins = state.pinPoints.dropLast(1)
                    val newRedo = state.redoPinPoints + last
                    state.copy(pinPoints = newPins, redoPinPoints = newRedo)
                } else state
            }
            is MapAction.RedoPin -> _uiState.update { state ->
                if (state.redoPinPoints.isNotEmpty()) {
                    val next = state.redoPinPoints.last()
                    val newRedo = state.redoPinPoints.dropLast(1)
                    val newPins = state.pinPoints + next
                    state.copy(pinPoints = newPins, redoPinPoints = newRedo)
                } else state
            }
            is MapAction.ClearPins -> _uiState.update { state ->
                state.copy(pinPoints = emptyList(), redoPinPoints = emptyList())
            }
            
            // DRAW LOGIC
            is MapAction.AddDrawPoint -> _uiState.update { state ->
                val newDraw = state.drawPoints + action.point
                state.copy(drawPoints = newDraw)
            }
            is MapAction.SetDrawPoints -> _uiState.update { state -> state.copy(drawPoints = action.points) }
            is MapAction.CommitDrawPath -> _uiState.update { state ->
                val newHistory = state.drawPointsHistory + listOf(state.drawPoints)
                state.copy(drawPoints = action.newPath, drawPointsHistory = newHistory, redoDrawPointsHistory = emptyList())
            }
            is MapAction.UndoDraw -> _uiState.update { state ->
                if (state.drawPointsHistory.isNotEmpty()) {
                    val last = state.drawPointsHistory.last()
                    val newHistory = state.drawPointsHistory.dropLast(1)
                    val newRedo = state.redoDrawPointsHistory + listOf(state.drawPoints)
                    state.copy(drawPoints = last, drawPointsHistory = newHistory, redoDrawPointsHistory = newRedo)
                } else state
            }
            is MapAction.RedoDraw -> _uiState.update { state ->
                if (state.redoDrawPointsHistory.isNotEmpty()) {
                    val next = state.redoDrawPointsHistory.last()
                    val newRedo = state.redoDrawPointsHistory.dropLast(1)
                    val newHistory = state.drawPointsHistory + listOf(state.drawPoints)
                    state.copy(drawPoints = next, drawPointsHistory = newHistory, redoDrawPointsHistory = newRedo)
                } else state
            }
            is MapAction.ClearDraw -> _uiState.update { state ->
                state.copy(drawPoints = emptyList(), drawPointsHistory = emptyList(), redoDrawPointsHistory = emptyList())
            }
            
            // SHAPE LOGIC
            is MapAction.SetShapePoints -> _uiState.update { state ->
                state.copy(shapePoints = action.points)
            }
            is MapAction.CommitShapePath -> _uiState.update { state ->
                val newHistory = state.shapePointsHistory + listOf(state.shapePoints)
                state.copy(shapePoints = action.newPath, shapePointsHistory = newHistory, redoShapePointsHistory = emptyList())
            }
            is MapAction.UndoShape -> _uiState.update { state ->
                if (state.shapePointsHistory.isNotEmpty()) {
                    val last = state.shapePointsHistory.last()
                    val newHistory = state.shapePointsHistory.dropLast(1)
                    val newRedo = state.redoShapePointsHistory + listOf(state.shapePoints)
                    state.copy(shapePoints = last, shapePointsHistory = newHistory, redoShapePointsHistory = newRedo)
                } else state
            }
            is MapAction.RedoShape -> _uiState.update { state ->
                if (state.redoShapePointsHistory.isNotEmpty()) {
                    val next = state.redoShapePointsHistory.last()
                    val newRedo = state.redoShapePointsHistory.dropLast(1)
                    val newHistory = state.shapePointsHistory + listOf(state.shapePoints)
                    state.copy(shapePoints = next, shapePointsHistory = newHistory, redoShapePointsHistory = newRedo)
                } else state
            }
            is MapAction.ClearShape -> _uiState.update { state ->
                state.copy(shapePoints = emptyList(), shapePointsHistory = emptyList(), redoShapePointsHistory = emptyList())
            }
            
            is MapAction.ClearAll -> _uiState.update { state ->
                state.copy(
                    activeMode = null,
                    completedMode = null,
                    isCalcExpanded = false,
                    isShapeDropped = false,
                    isDrawing = false,
                    pinPoints = emptyList(),
                    redoPinPoints = emptyList(),
                    drawPoints = emptyList(),
                    drawPointsHistory = emptyList(),
                    redoDrawPointsHistory = emptyList(),
                    shapePoints = emptyList(),
                    shapePointsHistory = emptyList(),
                    redoShapePointsHistory = emptyList(),
                    loadedMeasurementName = null,
                    loadedMeasurementId = null,
                    isReadOnly = false,
                    pdfExportOptions = state.pdfExportOptions.copy(name = "")
                )
            }
            is MapAction.ExitReadOnly -> _uiState.update { state ->
                state.copy(
                    activeMode = null,
                    completedMode = null,
                    isCalcExpanded = false,
                    isShapeDropped = false,
                    isDrawing = false,
                    shapePoints = emptyList(),
                    loadedMeasurementName = null,
                    loadedMeasurementId = null,
                    isReadOnly = false,
                    pdfExportOptions = state.pdfExportOptions.copy(name = "")
                )
            }
        }
    }
}
