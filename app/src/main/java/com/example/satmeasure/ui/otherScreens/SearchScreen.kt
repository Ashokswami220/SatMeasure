package com.example.satmeasure.ui.otherScreens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onHowToClick: () -> Unit,
    onCoordinateSearch: (Double, Double) -> Unit,
    onTextSearch: (String) -> Unit
) {
    val context = LocalContext.current

    var coordinateInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val suggestions = listOf("Delhi", "Noida", "Mumbai", "Bangalore").filter {
        it.contains(searchQuery, ignoreCase = true) && searchQuery.isNotEmpty()
    }

    // THE FIX: We dynamically grab the EXACT color your phone uses for native SearchBars
    val nativeSearchBarColor = SearchBarDefaults.colors().containerColor

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- 1. THE CUSTOM SEARCH PILL ---
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search city or place name...") },
                leadingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotEmpty()) {
                            searchQuery = ""
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Back")
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    // Applied the exact native color here!
                    unfocusedContainerColor = nativeSearchBarColor,
                    focusedContainerColor = nativeSearchBarColor
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. THE SUGGESTIONS LIST (Appears when typing) ---
            AnimatedVisibility(
                visible = searchQuery.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it / 4 },
                exit = fadeOut() + slideOutVertically { it / 4 }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    // Also applying the native color to the dropdown for consistency
                    color = nativeSearchBarColor
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(suggestions) { suggestion ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = suggestion
                                        onTextSearch(suggestion)
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(text = suggestion, style = MaterialTheme.typography.bodyLarge)
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            // --- 3. THE COORDINATES CARD (Hides when typing) ---
            AnimatedVisibility(
                visible = searchQuery.isEmpty(),
                enter = fadeIn() + slideInVertically { it / 4 },
                exit = fadeOut() + slideOutVertically { it / 4 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    // Applied the exact native SearchBar color to the Card!
                    colors = CardDefaults.cardColors(
                        containerColor = nativeSearchBarColor
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Search by Coordinates",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = coordinateInput,
                            onValueChange = { coordinateInput = it },
                            label = { Text("Latitude, Longitude") },
                            placeholder = { Text("e.g. 28.5355, 77.3910") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            trailingIcon = {
                                if (coordinateInput.isNotEmpty()) {
                                    IconButton(onClick = { coordinateInput = "" }) {
                                        Icon(Icons.Default.Clear, "Clear")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val parts = coordinateInput.split(",")
                                if (parts.size == 2) {
                                    val lat = parts[0].trim().toDoubleOrNull()
                                    val lng = parts[1].trim().toDoubleOrNull()
                                    if (lat != null && lng != null) {
                                        onCoordinateSearch(lat, lng)
                                    } else {
                                        Toast.makeText(context, "Invalid numbers", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please use a comma to separate", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = coordinateInput.contains(",")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Go to Coordinates", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(modifier = Modifier.height(20.dp))

                        // Helper Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val gmmIntentUri = "https://maps.google.com/".toUri()
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                                    }
                                },
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open G-Maps")
                            }

                            OutlinedButton(
                                onClick = onHowToClick,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("How to Find")
                            }
                        }
                    }
                }
            }
        }
    }
}