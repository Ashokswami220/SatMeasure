package com.example.satmeasure

import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSheet() {
    BottomSheetScaffold(
        sheetMaxWidth = 400.dp,
        sheetContent = {}
    ) {}
}
