package com.ae.solarsystem

import android.graphics.BlurMaskFilter
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import kotlin.math.max
import android.graphics.Color as AndroidColor
import android.graphics.Paint as FrameworkPaint

private val RubikFontFamily = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_bold, FontWeight.Bold),
    Font(R.font.rubik_extra_bold, FontWeight.ExtraBold)
)

private val LilyScriptFontFamily = FontFamily(
    Font(R.font.lily_script_one_regular, FontWeight.Normal)
)

/* ----------------------------- Shared Constants ----------------------------- */

private val HeroScrollRange = 260.dp

private val CardHeight = 252.dp
private val CardSpacing = 18.dp
private val CardCorner = 28.dp

private val StackRevealStep = 12.dp

private val PlanetCardHorizontalPadding = 20.dp
private val PlanetImageSize = 126.dp
private val PlanetImageOffsetX = 20.dp
private val PlanetImageOffsetY = (-14).dp

private val PlanetTextOffsetX = 168.dp
private val PlanetTextOffsetY = 42.dp

private val HeroEarthFinalTop = 36.dp
private const val HeroEarthStartScale = 1.72f
private const val HeroEarthEndScale = 0.66f

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
        with(density) { HeroScrollRange.toPx() }
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

    val totalScrollPx by remember(listState, density, screenHeight) {
        derivedStateOf {
            val heroHeightPx = with(density) { screenHeight.toPx() }
            val itemStridePx = with(density) { (CardHeight + CardSpacing).toPx() }

            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset.toFloat()
                else -> {
                    heroHeightPx +
                            ((listState.firstVisibleItemIndex - 1) * itemStridePx) +
                            listState.firstVisibleItemScrollOffset.toFloat()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SpaceBackground()


        HeroSection(
            heroProgress,
            screenHeight,
            modifier = Modifier.zIndex(1000f)
        )

        ScrollDriver(
            listState = listState,
            heroHeight = screenHeight
        )

        PlanetStackOverlay(
            totalScrollPx = totalScrollPx,
            heroHeight = screenHeight
        )
    }
}

@Composable
private fun ScrollDriver(
    listState: LazyListState,
    heroHeight: Dp
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
        contentPadding = PaddingValues(bottom = CardHeight + 48.dp)
    ) {
        item(key = "hero_spacer") {
            Spacer(modifier = Modifier.height(heroHeight))
        }

        itemsIndexed(
            items = planets,
            key = { _, planet -> planet.name }
        ) { _, _ ->
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardHeight)
            )
        }

        item(key = "bottom_spacer") {
            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun PlanetStackOverlay(
    totalScrollPx: Float,
    heroHeight: Dp
) {
    val density = LocalDensity.current

    val metrics = remember(density, heroHeight) {
        StackMetrics(
            heroHeightPx = with(density) { heroHeight.toPx() },
            cardHeightPx = with(density) { CardHeight.toPx() },
            cardSpacingPx = with(density) { CardSpacing.toPx() },
            stackTopPx = with(density) {
                300.dp.toPx()
            }, revealStepPx = with(density) { StackRevealStep.toPx() },
            itemCount = planets.size
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        planets.forEachIndexed { index, planet ->
            val state = calculateOverlayCardState(
                totalScrollPx = totalScrollPx,
                metrics = metrics,
                index = index
            )

            PlanetCard(
                planet = planet,
                planetImageAlpha = state.planetImageAlpha,
                modifier = Modifier
                    .padding(horizontal = PlanetCardHorizontalPadding)
                    .fillMaxWidth()
                    .height(CardHeight)
                    .graphicsLayer {
                        translationY = state.top
                    }
                    .zIndex(index.toFloat())
            )
        }
    }
}

@Immutable
private data class StackMetrics(
    val heroHeightPx: Float,
    val cardHeightPx: Float,
    val cardSpacingPx: Float,
    val stackTopPx: Float,
    val revealStepPx: Float,
    val itemCount: Int
)

@Immutable
private data class OverlayCardState(
    val top: Float,
    val planetImageAlpha: Float
)

private fun calculateOverlayCardState(
    totalScrollPx: Float,
    metrics: StackMetrics,
    index: Int
): OverlayCardState {
    val stride = metrics.cardHeightPx + metrics.cardSpacingPx
    val itemStart = metrics.heroHeightPx + (index * stride)
    val naturalTop = itemStart - totalScrollPx
    val stackedTop = metrics.stackTopPx + (index * metrics.revealStepPx)
    val top = max(naturalTop, stackedTop)

    val nextCardProgress = if (index < metrics.itemCount - 1) {
        val nextItemStart = metrics.heroHeightPx + ((index + 1) * stride)
        val nextNaturalTop = nextItemStart - totalScrollPx
        val nextStackedTop = metrics.stackTopPx + ((index + 1) * metrics.revealStepPx)
        val distanceToStack = (nextNaturalTop - nextStackedTop).coerceAtLeast(0f)
        val fadeDistance = metrics.cardHeightPx * 0.90f
        (1f - (distanceToStack / fadeDistance)).coerceIn(0f, 1f)
    } else {
        0f
    }

    return OverlayCardState(
        top = top,
        planetImageAlpha = lerp(1f, 0.32f, smoothProgress(nextCardProgress))
    )
}

@Composable
private fun HeroSection(
    progress: Float,
    screenHeight: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
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
    val density = LocalDensity.current
    val hiddenOffsetPx = with(density) { 220.dp.toPx() }

    val firstPhase = (progress / 0.5f).coerceIn(0f, 1f)
    val secondPhase = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 54.dp)
                .graphicsLayer {
                    translationY = lerp(0f, -hiddenOffsetPx, firstPhase)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Earth",
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
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontFamily = LilyScriptFontFamily,
                    fontWeight = FontWeight.Normal
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 132.dp)
                .graphicsLayer {
                    translationY = if (progress < 0.5f) {
                        -hiddenOffsetPx
                    } else {
                        lerp(-hiddenOffsetPx, 0f, secondPhase)
                    }
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
    val screenHeightPx = with(density) { screenHeight.toPx() }
    val finalTopPx = with(density) { HeroEarthFinalTop.toPx() }

    val scale = lerp(HeroEarthStartScale, HeroEarthEndScale, progress)
    val startTopPx = screenHeightPx * 0.40f
    val translationY = lerp(startTopPx, finalTopPx, progress)
    val alpha = lerp(1f, 0.5f, smoothProgress(progress))

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.earth),
            contentDescription = "Earth",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.translationY = translationY
                    this.alpha = alpha
                    transformOrigin = TransformOrigin(0.5f, 0f)
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
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {

        ArrowStack(
            modifier = Modifier.graphicsLayer {
                alpha = 1f - (eased * 2f).coerceIn(0f, 1f)
                translationY = -14f * eased
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Swipe up to explore",
            modifier = Modifier.graphicsLayer {
                alpha = 1f - (eased * 2f).coerceIn(0f, 1f)
            },
            style = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = RubikFontFamily
            )
        )
    }
}

@Composable
private fun ArrowStack(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-2).dp)
    ) {

        Icon(
            painter = painterResource(R.drawable.ic_arrow_up),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White,

            )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_up),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp),
            tint = Color.White.copy(alpha = 0.7f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_up),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp),
            tint = Color.White.copy(alpha = 0.4f)

        )
    }
}

@Composable
private fun SpaceBackground() {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF07040D),
                            Color(0xFF0A1025),
                            Color(0xFF102042),
                            Color(0xFF06111E),
                            Color(0xFF04070F)
                        )
                    )
                )
        )

        Image(
            painter = painterResource(R.drawable.stars),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.66f
        )
    }
}
@Composable
private fun PlanetShadowAboveCard(
    shadowColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val blurPx = with(density) { 100.dp.toPx() }
    val planetSizePx = with(density) { PlanetImageSize.toPx() }
    val offsetXPx = with(density) { PlanetImageOffsetX.toPx() }
    val offsetYPx = with(density) { PlanetImageOffsetY.toPx() }

    Canvas(
        modifier = modifier.graphicsLayer {
            clip = false
        }
    ) {
        drawIntoCanvas { canvas ->
            val paint = FrameworkPaint().apply {
                isAntiAlias = true
                this.color = shadowColor.copy(alpha = 0.50f).toArgb()
                maskFilter = BlurMaskFilter(blurPx / 2f, BlurMaskFilter.Blur.NORMAL)
            }

            val cx = offsetXPx + (planetSizePx * 0.50f)
            val cy = offsetYPx + (planetSizePx * 0.54f)
            val radius = planetSizePx * 0.58f

            canvas.nativeCanvas.drawCircle(cx, cy, radius, paint)
        }
    }
}

@Composable
private fun PlanetCard(
    planet: Planet,
    planetImageAlpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.graphicsLayer {
            clip = false
        }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(CardCorner))
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
                    shape = RoundedCornerShape(CardCorner)
                )
        )

        PlanetShadowAboveCard(
            shadowColor = planet.shadowColor,
            modifier = Modifier.matchParentSize()
        )

        PlanetImage(
            drawableId = planet.drawableId,
            size = PlanetImageSize,
            modifier = Modifier
                .offset(x = PlanetImageOffsetX, y = PlanetImageOffsetY)
                .graphicsLayer {
                    alpha = planetImageAlpha
                }
        )

        Column(
            modifier = Modifier
                .offset(x = PlanetTextOffsetX, y = PlanetTextOffsetY)
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
                isTemperature = false,
                modifier = Modifier.weight(1f)
            )

            VerticalDivider()

            InfoItem(
                iconDrawableId = R.drawable.ic_sun_01,
                label = "One Day",
                value = planet.day,
                isTemperature = false,
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
                isTemperature = true,
                modifier = Modifier.weight(1f)
            )

            VerticalDivider()

            InfoItem(
                iconDrawableId = R.drawable.ic_alert_circle,
                label = "Additional info",
                value = planet.info,
                isTemperature = false,
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
    isTemperature: Boolean,
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

            if (isTemperature) {
                Text(
                    text = buildTemperatureAnnotatedString(value),
                    modifier = Modifier.padding(top = 2.dp),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 10.4.sp,
                        lineHeight = 11.8.sp,
                        fontFamily = RubikFontFamily
                    )
                )
            } else {
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
}

private fun buildTemperatureAnnotatedString(value: String): AnnotatedString {
    val commaIndex = value.indexOf(',')
    return if (commaIndex == -1) {
        buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
            append(value)
            pop()
        }
    } else {
        buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
            append(value.take(commaIndex + 1))
            pop()

            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            )
            append(value.drop(commaIndex + 1))
            pop()
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

private fun Color.toArgb(): Int {
    return AndroidColor.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
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
    val shadowColor: Color
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
        shadowColor = Color(0xFF095B91)
    ),
    Planet(
        name = "Venus",
        subtitle = "The Toxic Beauty",
        weight = "70kg -> 63kg",
        day = "243 Days",
        temperature = "465°C",
        info = "Sun rises from\nWest",
        drawableId = R.drawable.venus,
        shadowColor = Color(0xFFC69E4A)
    ),
    Planet(
        name = "Mars",
        subtitle = "The next colony",
        weight = "70kg -> 27kg",
        day = "24.6 Hours",
        temperature = "-65°C, Bring a\njacket",
        info = "Red Dust Storms",
        drawableId = R.drawable.mars,
        shadowColor = Color(0xFFFF844E)
    ),
    Planet(
        name = "Jupiter",
        subtitle = "The Heavy Giant",
        weight = "70kg -> 177kg",
        day = "9.9 Hours",
        temperature = "-110°C, Bring a\njacket",
        info = "Has 95 Moons",
        drawableId = R.drawable.jupiter,
        shadowColor = Color(0xFFFF8332)
    ),
    Planet(
        name = "Saturn",
        subtitle = "The Ring Master",
        weight = "70kg -> 74kg",
        day = "10.7 Hours",
        temperature = "-178°C, Bring a\njacket",
        info = "Lighter than\nwater",
        drawableId = R.drawable.saturn,
        shadowColor = Color(0xFFAB4F20)
    ),
    Planet(
        name = "Uranus",
        subtitle = "The Lazy Iceberg",
        weight = "70kg -> 62kg",
        day = "17 Hours",
        temperature = "-224°C, Bring 3\njacket",
        info = "diamond Shower",
        drawableId = R.drawable.uranus,
        shadowColor = Color(0xFF31CFDB)
    ),
    Planet(
        name = "Neptune",
        subtitle = "The Windy World",
        weight = "70kg -> 79kg",
        day = "16 Hours",
        temperature = "-214°C, Bring 3\njacket",
        info = "Wind faster than\nSound",
        drawableId = R.drawable.neptune,
        shadowColor = Color(0xFF2CA6DB)
    )
)