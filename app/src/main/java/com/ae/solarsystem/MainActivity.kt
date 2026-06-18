package com.ae.solarsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolarSystemTheme {
                SolarSystemScreen()
            }
        }
    }
}

@Composable
fun SolarSystemScreen() {
    var earthScale by remember { mutableFloatStateOf(1f) }
    var earthAlpha by remember { mutableFloatStateOf(1f) }
    var titleAlpha by remember { mutableFloatStateOf(1f) }
    var subtitleAlpha by remember { mutableFloatStateOf(1f) }
    var newTitleAlpha by remember { mutableFloatStateOf(0f) }
    var newSubtitleAlpha by remember { mutableFloatStateOf(0f) }

    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.value) {
        val normalizedScroll = (scrollState.value.toFloat() / 1200f).coerceIn(0f, 1f)
        earthScale = 1f - (normalizedScroll * 0.75f)
        titleAlpha = (1f - normalizedScroll * 2f).coerceIn(0f, 1f)
        subtitleAlpha = (1f - normalizedScroll * 1.8f).coerceIn(0f, 1f)
        newTitleAlpha = (normalizedScroll * 2f - 0.2f).coerceIn(0f, 1f)
        newSubtitleAlpha = (normalizedScroll * 1.8f - 0.15f).coerceIn(0f, 1f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF020D3C),
                        Color(0xFF0F172A),
                        Color(0xFF060816),
                        Color(0xFF000000)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ==================== SECTION 1: LARGE EARTH ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 1300.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                Text(
                    text = "Earth",
                    style = TextStyle(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.alpha(titleAlpha)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A tiny blue world drifting\nthrough the endless dark.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .alpha(subtitleAlpha)
                        .padding(horizontal = 24.dp)
                )

                // MASSIVE CONTAINER - ALLOWS FULL ZOOM WITHOUT CLIPPING
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1150.dp)
                        .alpha(earthAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    // LARGE ZOOMED EARTH - CROP FOR CLOSE-UP EFFECT
                    AsyncImage(
                        model = R.drawable.earth,
                        contentDescription = "Earth",
                        contentScale = ContentScale.Crop,  // CROP for zoomed effect
                        modifier = Modifier
                            .fillMaxWidth().graphicsLayer {
                            scaleX = earthScale
                            scaleY = earthScale

                            clip = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Swipe up to explore",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = SolarSystemColors.accentPink,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.alpha(if (titleAlpha > 0) 1f else 0f)
                )
            }

            // ==================== SECTION 2: NEW TITLE ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F172A).copy(alpha = 0.9f),
                                Color(0xFF060816)
                            )
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Our Solar System",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.alpha(newTitleAlpha)
                )

                Text(
                    text = "Earth is only one small part of a much larger story.",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .alpha(newSubtitleAlpha)
                        .padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                AsyncImage(
                    model = R.drawable.earth,
                    contentDescription = "Earth Small",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(180.dp)
                        .alpha(newSubtitleAlpha)
                        .clip(RoundedCornerShape(90.dp))
                        .graphicsLayer {
                            scaleX = earthScale * 1.5f
                            scaleY = earthScale * 1.5f
                        }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }

            // PLANET CARDS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                PlanetCard(
                    planetName = "Saturn",
                    planetSubtitle = "The Ring Master",
                    weight = "70kg → 74kg",
                    dayLength = "10.7 Hours",
                    temperature = "-178°C. Bring a jacket",
                    additionalInfo = "Lighter than water",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                PlanetCard(
                    planetName = "Mars",
                    planetSubtitle = "The next colony",
                    weight = "70kg → 27kg",
                    dayLength = "24.6 Hours",
                    temperature = "-80°C. Very cold",
                    additionalInfo = "Red Planet",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PlanetCard(
    planetName: String,
    planetSubtitle: String,
    weight: String,
    dayLength: String,
    temperature: String,
    additionalInfo: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1F2E).copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = planetName,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = planetSubtitle,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    icon = "⚖️",
                    label = "You Would Weigh",
                    value = weight,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = "☀️",
                    label = "One Day",
                    value = dayLength,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    icon = "🌡️",
                    label = "Temperature",
                    value = temperature,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = "ℹ️",
                    label = "Additional info",
                    value = additionalInfo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = Color(0xFF0F172A).copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(text = icon, style = TextStyle(fontSize = 16.sp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

object SolarSystemColors {
    val cardBackground = Color(0xFF1A1F2E).copy(alpha = 0.9f)
    val accentPink = Color(0xFFE91E63)
}

@Composable
fun SolarSystemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFE91E63),
            background = Color(0xFF000000),
            surface = SolarSystemColors.cardBackground
        ),
        content = content
    )
}