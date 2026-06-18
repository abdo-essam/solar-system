package com.ae.solarsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

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
    val scrollState = rememberScrollState()

    val progress by remember {
        derivedStateOf {
            (scrollState.value / 1200f).coerceIn(0f, 1f)
        }
    }

    // EARTH: 1400dp → 200dp
    val earthSize by remember {
        derivedStateOf { lerp(1400.dp, 200.dp, progress) }
    }

    // EARTH OPACITY: 100% → 50%
    val earthOpacity by remember {
        derivedStateOf { 1f - (progress * 0.5f).coerceIn(0f, 0.5f) }
    }

    val heroTitleAlpha by remember {
        derivedStateOf { (1f - progress * 2f).coerceIn(0f, 1f) }
    }
    val heroSubtitleAlpha by remember {
        derivedStateOf { (1f - progress * 1.8f).coerceIn(0f, 1f) }
    }

    // HERO TEXT MOVES UP
    val heroTextOffsetY by remember {
        derivedStateOf { lerp(0.dp, (-200).dp, progress) }
    }

    // SOLAR SYSTEM TEXT MOVES DOWN
    val solarSystemTextOffsetY by remember {
        derivedStateOf { lerp(300.dp, 0.dp, progress) }
    }

    val sectionTitleAlpha by remember {
        derivedStateOf { ((progress - 0.18f) / 0.35f).coerceIn(0f, 1f) }
    }
    val sectionSubtitleAlpha by remember {
        derivedStateOf { ((progress - 0.22f) / 0.35f).coerceIn(0f, 1f) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF05060A),
                        Color(0xFF0A1230),
                        Color(0xFF101A3A),
                        Color(0xFF090D1F),
                        Color(0xFF05060A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1100.dp)
            ) {
                // EARTH IMAGE - SHRINKS & FADES (Z-index 1)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .alpha(earthOpacity),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.earth),
                        contentDescription = "Earth",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(earthSize)
                    )
                }

                // HERO TEXT - MOVES UP (Z-index 2)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 72.dp)
                        .offset(y = heroTextOffsetY)
                        .zIndex(2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Earth",
                        style = TextStyle(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.alpha(heroTitleAlpha)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "A tiny blue world drifting\nthrough the endless dark.",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .alpha(heroSubtitleAlpha)
                            .padding(horizontal = 24.dp)
                    )
                }

                Text(
                    text = "Swipe up to explore",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE91E63)
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                        .alpha(heroTitleAlpha)
                        .zIndex(2f)
                )

                // SOLAR SYSTEM TEXT - MOVES DOWN (Z-index 3)
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = solarSystemTextOffsetY)
                        .zIndex(3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Our Solar System",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.alpha(sectionTitleAlpha)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Earth is only one small part of a much larger story.",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .alpha(sectionSubtitleAlpha)
                            .padding(horizontal = 24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Image(
                    painter = painterResource(id = R.drawable.earth),
                    contentDescription = "Small Earth",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(180.dp)
                        .alpha(sectionSubtitleAlpha)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

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