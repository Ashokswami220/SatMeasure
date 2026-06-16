package com.example.satmeasure.ui.otherScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.RenderMode
import com.example.satmeasure.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun TutorialScreen(
    onBackClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // iOS-style back button in top left
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(
                        top = dimensionResource(id = R.dimen.spacing_lg),
                        start = dimensionResource(id = R.dimen.spacing_lg)
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = stringResource(id = R.string.cd_back),
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.spacing_sm_minus))
                )
            }

            // Centered Lottie animation
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.site_under_construction))
            
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                renderMode = RenderMode.SOFTWARE,
                enableMergePaths = true,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(dimensionResource(id = R.dimen.dimen_400))
            )
        }
    }
}