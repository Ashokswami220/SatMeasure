package com.example.satmeasure.ui.otherScreens

import com.example.satmeasure.utils.HapticHelper

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.satmeasure.BuildConfig
import com.example.satmeasure.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class PlaceSuggestion(val name: String, val lat: Double, val lng: Double)

suspend fun searchPlaces(
    query: String, token: String, location: com.mapbox.geojson.Point?
): List<PlaceSuggestion> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    var urlStr = "https://api.mapbox.com/geocoding/v5/mapbox.places/${
        URLEncoder.encode(
            query, "UTF-8"
        )
    }.json?access_token=$token&types=place,address,poi,neighborhood,locality"
    if (location != null) {
        urlStr += "&proximity=${location.longitude()},${location.latitude()}"
    }
    try {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader()
                .use { it.readText() }
            val json = JSONObject(response)
            val features = json.optJSONArray("features") ?: return@withContext emptyList()
            val results = mutableListOf<PlaceSuggestion>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val placeName = feature.optString("place_name", "")
                val center = feature.optJSONArray("center")
                if (center != null && center.length() >= 2) {
                    val lng = center.getDouble(0)
                    val lat = center.getDouble(1)
                    results.add(PlaceSuggestion(placeName, lat, lng))
                }
            }
            results
        } else emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onHowToClick: () -> Unit,
    onCoordinateSearch: (Double, Double) -> Unit,
    currentUserLocation: com.mapbox.geojson.Point? = null
) {
    val context = LocalContext.current

    var coordinateInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<PlaceSuggestion>()) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length > 2) {
            delay(500) // debounce
            val results = searchPlaces(
                searchQuery.trim(), BuildConfig.MAPBOX_ACCESS_TOKEN, currentUserLocation
            )
            suggestions = results
        } else {
            suggestions = emptyList()
        }
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
                placeholder = { Text(stringResource(R.string.hint_search_city)) },
                leadingIcon = {
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
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
                                        HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                        searchQuery = suggestion.name
                                        onCoordinateSearch(suggestion.lat, suggestion.lng)
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = suggestion.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
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
                            text = stringResource(R.string.title_search_by_coordinates),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = coordinateInput,
                            onValueChange = { coordinateInput = it },
                            label = { Text(stringResource(R.string.label_lat_lng)) },
                            placeholder = { Text(stringResource(R.string.hint_lat_lng_example)) },
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

                        val msgInvalidNumbers = stringResource(id = R.string.msg_invalid_numbers)
                        val msgUseComma = stringResource(id = R.string.msg_use_comma_to_separate)

                        Button(
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                val parts = coordinateInput.split(",")
                                if (parts.size == 2) {
                                    val lat = parts[0].trim().toDoubleOrNull()
                                    val lng = parts[1].trim().toDoubleOrNull()
                                    if (lat != null && lng != null) {
                                        onCoordinateSearch(lat, lng)
                                    } else {
                                        Toast.makeText(context, msgInvalidNumbers, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, msgUseComma, Toast.LENGTH_SHORT).show()
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
                            Text(
                                stringResource(R.string.action_go_to_coordinates),
                                style = MaterialTheme.typography.titleMedium
                            )
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
                                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                    val gmmIntentUri = "https://maps.google.com/".toUri()
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        )
                                    }
                                },
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Default.Map, contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.action_open_gmaps))
                            }

                            OutlinedButton(
                                onClick = {
                                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                    onHowToClick()
                                },
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Default.TouchApp, contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.action_how_to_find))
                            }
                        }
                    }
                }
            }
        }
    }
}