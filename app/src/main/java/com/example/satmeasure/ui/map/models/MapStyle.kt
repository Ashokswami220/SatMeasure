package com.example.satmeasure.ui.map.models

import androidx.compose.ui.graphics.vector.ImageVector

data class MapStyleOption(
    val id: String,
    val name: String,
    val lightStyleUri: String,
    val darkStyleUri: String,
    val imageRes: Int
)
