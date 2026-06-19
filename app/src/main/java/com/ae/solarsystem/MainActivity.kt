package com.ae.solarsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
            (scrollState.value / 1000f).coerceIn(0f, 1f)
        }
    }

    val earthMoveProgress by remember {
        derivedStateOf {
            (scrollState.value / 650f).coerceIn(0f, 1f)
        }
    }

    val firstProgress by remember {
        derivedStateOf {
            (scrollState.value / 400f).coerceIn(0f, 1f)
        }
    }

    val secondProgress by remember {
        derivedStateOf {
            ((scrollState.value - 80f) / 220f).coerceIn(0f, 1f)
        }
    }

    val firstOffsetY by remember {
        derivedStateOf {
            lerpDp(0.dp, (-280).dp, firstProgress)
        }
    }

    val firstAlpha by remember {
        derivedStateOf {
            1f - firstProgress
        }
    }

    val secondOffsetY by remember {
        derivedStateOf {
            lerpDp((-140).dp, 250.dp, secondProgress)
        }
    }

    val secondAlpha by remember {
        derivedStateOf {
            (secondProgress * 1.2f).coerceIn(0f, 1f)
        }
    }

    val secondScale by remember {
        derivedStateOf {
            0.96f + (0.04f * secondProgress)
        }
    }

    val earthSize by remember {
        derivedStateOf {
            lerpDp(1400.dp, 200.dp, earthMoveProgress)
        }
    }

    val earthOffsetY by remember {
        derivedStateOf {
            lerpDp(170.dp, 36.dp, earthMoveProgress)
        }
    }

    val earthOpacity by remember {
        derivedStateOf {
            1f - (earthMoveProgress * 0.5f).coerceIn(0f, 0.5f)
        }
    }

    val swipeAlpha by remember {
        derivedStateOf {
            (1f - firstProgress * 1.2f).coerceIn(0f, 1f)
        }
    }

    val swipeOffsetY by remember {
        derivedStateOf {
            (scrollState.value / 4f).dp
        }
    }

    val boxHeight by remember {
        derivedStateOf {
            lerpDp(920.dp, 250.dp, progress)
        }
    }

    val planets = remember {
        listOf(
            PlanetUiModel(
                name = "Saturn",
                subtitle = "The Ring Master",
                weight = "70kg → 74kg",
                dayLength = "10.7 Hours",
                temperature = "-178°C. Bring a jacket",
                additionalInfo = "Lighter than water"
            ),
            PlanetUiModel(
                name = "Mars",
                subtitle = "The next colony",
                weight = "70kg → 27kg",
                dayLength = "24.6 Hours",
                temperature = "-80°C. Very cold",
                additionalInfo = "Red Planet"
            )
        )
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
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxHeight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .alpha(earthOpacity),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.earth),
                        contentDescription = "Earth",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .offset(y = earthOffsetY)
                            .size(earthSize)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(top = 56.dp)
                        .offset(y = firstOffsetY)
                        .alpha(firstAlpha)
                        .fillMaxWidth()
                        .zIndex(2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Earth",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "A tiny blue world drifting\nthrough the endless dark.",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = secondOffsetY)
                        .alpha(secondAlpha)
                        .graphicsLayer {
                            scaleX = secondScale
                            scaleY = secondScale
                        }
                        .zIndex(5f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Our Solar System",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Earth is only one small part of a much larger story.",
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = swipeOffsetY)
                        .alpha(swipeAlpha)
                        .padding(bottom = 60.dp)
                        .zIndex(6f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(3) {
                        Text(
                            text = "^",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height((-6).dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Swipe up to explore",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                planets.forEach { planet ->
                    PlanetCard(
                        planet = planet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PlanetCard(
    planet: PlanetUiModel,
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
                        text = planet.name,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = planet.subtitle,
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
                    value = planet.weight,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = "☀️",
                    label = "One Day",
                    value = planet.dayLength,
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
                    value = planet.temperature,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = "ℹ️",
                    label = "Additional info",
                    value = planet.additionalInfo,
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
        Text(
            text = icon,
            style = TextStyle(fontSize = 16.sp)
        )
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

@Immutable
private data class PlanetUiModel(
    val name: String,
    val subtitle: String,
    val weight: String,
    val dayLength: String,
    val temperature: String,
    val additionalInfo: String
)

private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp {
    return start + (stop - start) * fraction
}

@Stable
object SolarSystemColors {
    val CardBackground = Color(0xFF1A1F2E).copy(alpha = 0.9f)
}

@Composable
fun SolarSystemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFE91E63),
            background = Color(0xFF000000),
            surface = SolarSystemColors.CardBackground
        ),
        content = content
    )
}