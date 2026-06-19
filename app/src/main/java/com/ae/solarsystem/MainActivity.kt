package com.ae.solarsystem

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import kotlin.random.Random

private val RubikFontFamily = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_bold, FontWeight.Bold),
    Font(R.font.rubik_extra_bold, FontWeight.ExtraBold)
)

private val LilyScriptFontFamily = FontFamily(
    Font(R.font.lily_script_one_regular, FontWeight.Normal)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.BLACK)
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SolarSystemScreen()
        }
    }
}

@Composable
private fun SolarSystemScreen() {
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenHeight = configuration.screenHeightDp.dp

    val heroScrollRangePx = remember(density) {
        with(density) { 260.dp.toPx() }
    }

    val heroProgress by remember(listState, heroScrollRangePx) {
        derivedStateOf {
            val scroll = if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                heroScrollRangePx
            }
            (scroll / heroScrollRangePx).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SpaceBackground()

        HeroSection(
            progress = heroProgress,
            screenHeight = screenHeight
        )

        PlanetStackList(
            listState = listState,
            heroHeight = screenHeight
        )
    }
}

@Composable
private fun PlanetStackList(
    listState: androidx.compose.foundation.lazy.LazyListState,
    heroHeight: Dp
) {
    val density = LocalDensity.current

    val cardHeight = 252.dp
    val cardSpacing = 18.dp
    val stackTop = 356.dp
    val revealStep = 46.dp

    val stackTopPx = with(density) { stackTop.toPx() }
    val revealStepPx = with(density) { revealStep.toPx() }

    val visibleItems by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item(key = "hero_spacer") {
            Spacer(modifier = Modifier.height(heroHeight))
        }

        itemsIndexed(
            items = planets,
            key = { _, item -> item.name }
        ) { index, planet ->
            val lazyItemIndex = index + 1
            val itemInfo = visibleItems.firstOrNull { it.index == lazyItemIndex }

            val translationY = remember(itemInfo, index, stackTopPx, revealStepPx) {
                calculateStackTranslationY(
                    itemInfo = itemInfo,
                    stackIndex = index,
                    stackTopPx = stackTopPx,
                    revealStepPx = revealStepPx
                )
            }

            PlanetCard(
                planet = planet,
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = if (index == 0) 0.dp else cardSpacing
                    )
                    .fillMaxWidth()
                    .height(cardHeight)
                    .graphicsLayer {
                        this.translationY = translationY
                    }
                    .zIndex(index.toFloat())
            )
        }

        item {
            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

private fun calculateStackTranslationY(
    itemInfo: LazyListItemInfo?,
    stackIndex: Int,
    stackTopPx: Float,
    revealStepPx: Float
): Float {
    if (itemInfo == null) return 0f

    val currentTop = itemInfo.offset.toFloat()
    val targetTop = stackTopPx + stackIndex * revealStepPx

    return if (currentTop < targetTop) {
        targetTop - currentTop
    } else {
        0f
    }
}

@Composable
private fun HeroSection(
    progress: Float,
    screenHeight: Dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        HeroEarth(
            progress = progress,
            screenHeight = screenHeight
        )

        HeroTexts(progress = progress)

        SwipeCue(progress = progress)
    }
}

@Composable
private fun HeroTexts(progress: Float) {
    val eased = smoothProgress(progress)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Earth",
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - (eased * 1.25f).coerceIn(0f, 1f)
                    translationY = -72f * eased
                },
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 58.sp,
                    lineHeight = 60.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = RubikFontFamily
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A tiny blue world drifting\nthrough the endless dark.",
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - (eased * 1.4f).coerceIn(0f, 1f)
                    translationY = -56f * eased
                },
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontFamily = LilyScriptFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 132.dp)
                .graphicsLayer {
                    alpha = ((eased - 0.20f) / 0.22f).coerceIn(0f, 1f)
                    translationY = lerp(26f, 0f, ((eased - 0.10f) / 0.28f).coerceIn(0f, 1f))
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Our Solar System",
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RubikFontFamily
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Earth is only one small part of a much larger\nstory.",
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.86f),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontFamily = LilyScriptFontFamily
                )
            )
        }
    }
}

@Composable
private fun HeroEarth(
    progress: Float,
    screenHeight: Dp
) {
    val density = LocalDensity.current
    val eased = smoothProgress(progress)

    val screenHeightPx = with(density) { screenHeight.toPx() }
    val finalTopPx = with(density) { 36.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.earth),
            contentDescription = "Earth",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    val startScale = 1.72f
                    val endScale = 0.66f
                    val scale = lerp(startScale, endScale, eased)

                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f)

                    val startTopPx = screenHeightPx * 0.52f
                    translationY = lerp(startTopPx, finalTopPx, eased)

                    alpha = lerp(1f, 0.5f, eased)
                    clip = false
                }
        )
    }
}

@Composable
private fun SwipeCue(progress: Float) {
    val eased = smoothProgress(progress)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 28.dp, height = 42.dp)
                .graphicsLayer {
                    alpha = 1f - (eased * 2f).coerceIn(0f, 1f)
                    translationY = -14f * eased
                }
        ) {
            repeat(2) { index ->
                val y = 12f + index * 10f
                val color = Color.White.copy(alpha = 0.92f - (index * 0.22f))

                drawLine(
                    color = color,
                    start = Offset(size.width / 2f - 7f, y),
                    end = Offset(size.width / 2f, y + 7f),
                    strokeWidth = 2.4f,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = color,
                    start = Offset(size.width / 2f + 7f, y),
                    end = Offset(size.width / 2f, y + 7f),
                    strokeWidth = 2.4f,
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Swipe up to explore",
            modifier = Modifier.graphicsLayer {
                alpha = 1f - (eased * 2f).coerceIn(0f, 1f)
            },
            style = TextStyle(
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = RubikFontFamily
            )
        )
    }
}

@Composable
private fun SpaceBackground() {
    val stars = remember {
        val random = Random(42)
        List(92) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radius = 0.7f + random.nextFloat() * 1.8f,
                alpha = 0.18f + random.nextFloat() * 0.68f,
                cross = random.nextFloat() > 0.88f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF07040D),
                    Color(0xFF0A1025),
                    Color(0xFF102042),
                    Color(0xFF06111E),
                    Color(0xFF04070F)
                )
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF184F95).copy(alpha = 0.34f),
                    Color(0xFF0B2758).copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.50f, size.height * 0.60f),
                radius = size.width * 0.95f
            ),
            radius = size.width * 0.95f,
            center = Offset(size.width * 0.50f, size.height * 0.60f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF7B59FF).copy(alpha = 0.16f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.76f, size.height * 0.14f),
                radius = size.width * 0.62f
            ),
            radius = size.width * 0.62f,
            center = Offset(size.width * 0.76f, size.height * 0.14f)
        )

        stars.forEach { star ->
            val center = Offset(star.x * size.width, star.y * size.height)
            val color = Color.White.copy(alpha = star.alpha)

            if (star.cross) {
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - star.radius * 4f),
                    end = Offset(center.x, center.y + star.radius * 4f),
                    strokeWidth = star.radius * 0.55f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(center.x - star.radius * 4f, center.y),
                    end = Offset(center.x + star.radius * 4f, center.y),
                    strokeWidth = star.radius * 0.55f,
                    cap = StrokeCap.Round
                )
            }

            drawCircle(
                color = color,
                radius = star.radius,
                center = center
            )
        }
    }
}

@Composable
private fun PlanetCard(
    planet: Planet,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF09162D).copy(alpha = 0.97f),
                                Color(0xFF060F20).copy(alpha = 0.99f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF2A3551).copy(alpha = 0.80f),
                        shape = RoundedCornerShape(28.dp)
                    )
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            planet.glow.copy(alpha = 0.36f),
                            planet.glow.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.22f, size.height * 0.26f),
                        radius = size.width * 0.44f
                    ),
                    radius = size.width * 0.44f,
                    center = Offset(size.width * 0.22f, size.height * 0.26f)
                )
            }

            PlanetImage(
                drawableId = planet.drawableId,
                size = 126.dp,
                modifier = Modifier.offset(x = 20.dp, y = (-14).dp)
            )

            Column(
                modifier = Modifier
                    .offset(x = 168.dp, y = 42.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = planet.name,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = RubikFontFamily
                    )
                )

                Text(
                    text = planet.subtitle,
                    modifier = Modifier.padding(top = 4.dp),
                    style = TextStyle(
                        color = Color(0xFFBAC3D5),
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = RubikFontFamily
                    )
                )
            }

            InfoGrid(
                planet = planet,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 18.dp, bottom = 16.dp)
                    .height(110.dp)
            )
        }
    }
}
@Composable
private fun PlanetImage(
    drawableId: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = drawableId),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}

@Composable
private fun InfoGrid(
    planet: Planet,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                iconDrawableId = R.drawable.ic_weight_scale,
                label = "You Would Weigh",
                value = planet.weight,
                modifier = Modifier.weight(1f)
            )

            VerticalDivider()

            InfoItem(
                iconDrawableId = R.drawable.ic_sun_01,
                label = "One Day",
                value = planet.day,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.16f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                iconDrawableId = R.drawable.ic_temperature,
                label = "Temperature",
                value = planet.temperature,
                modifier = Modifier.weight(1f)
            )

            VerticalDivider()

            InfoItem(
                iconDrawableId = R.drawable.ic_alert_circle,
                label = "Additional info",
                value = planet.info,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .width(0.5.dp)
            .height(34.dp)
            .background(Color.White.copy(alpha = 0.16f))
    )
}

@Composable
private fun InfoItem(
    iconDrawableId: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 2.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconDrawableId),
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(Color(0xFFD0D8EA))
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = label,
                style = TextStyle(
                    color = Color(0xFFA7B0C6),
                    fontSize = 9.2.sp,
                    lineHeight = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = RubikFontFamily
                )
            )

            Text(
                text = value,
                modifier = Modifier.padding(top = 2.dp),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 10.4.sp,
                    lineHeight = 11.8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = RubikFontFamily
                )
            )
        }
    }
}

@Stable
private fun smoothProgress(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

@Stable
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    val t = fraction.coerceIn(0f, 1f)
    return start + (end - start) * t
}

@Immutable
private data class Planet(
    val name: String,
    val subtitle: String,
    val weight: String,
    val day: String,
    val temperature: String,
    val info: String,
    val drawableId: Int,
    val glow: Color
)

@Immutable
private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val cross: Boolean
)

private val planets = listOf(
    Planet(
        name = "Mercury",
        subtitle = "The Fastest Planet",
        weight = "70kg -> 26kg",
        day = "1,408 Hours",
        temperature = "167°C",
        info = "Birthday every\n88 days",
        drawableId = R.drawable.mercury,
        glow = Color(0xFF6A9AC7)
    ),
    Planet(
        name = "Venus",
        subtitle = "The Toxic Beauty",
        weight = "70kg -> 63kg",
        day = "243 Days",
        temperature = "465°C",
        info = "Sun rises from\nWest",
        drawableId = R.drawable.venus,
        glow = Color(0xFFFFB83D)
    ),
    Planet(
        name = "Mars",
        subtitle = "The next colony",
        weight = "70kg -> 27kg",
        day = "24.6 Hours",
        temperature = "-65°C, Bring a\njacket",
        info = "Red Dust Storms",
        drawableId = R.drawable.mars,
        glow = Color(0xFFFF6846)
    ),
    Planet(
        name = "Jupiter",
        subtitle = "The Heavy Giant",
        weight = "70kg -> 177kg",
        day = "9.9 Hours",
        temperature = "-110°C, Bring a\njacket",
        info = "Has 95 Moons",
        drawableId = R.drawable.jupiter,
        glow = Color(0xFFFF9E64)
    ),
    Planet(
        name = "Saturn",
        subtitle = "The Ring Master",
        weight = "70kg -> 74kg",
        day = "10.7 Hours",
        temperature = "-178°C, Bring a\njacket",
        info = "Lighter than\nwater",
        drawableId = R.drawable.saturn,
        glow = Color(0xFFFFD18B)
    ),
    Planet(
        name = "Uranus",
        subtitle = "The Lazy Iceberg",
        weight = "70kg -> 62kg",
        day = "17 Hours",
        temperature = "-224°C, Bring 3\njacket",
        info = "diamond Shower",
        drawableId = R.drawable.uranus,
        glow = Color(0xFF47E7E8)
    ),
    Planet(
        name = "Neptune",
        subtitle = "The Windy World",
        weight = "70kg -> 79kg",
        day = "16 Hours",
        temperature = "-214°C, Bring 3\njacket",
        info = "Wind faster than\nSound",
        drawableId = R.drawable.neptune,
        glow = Color(0xFF5EBEFF)
    )
)