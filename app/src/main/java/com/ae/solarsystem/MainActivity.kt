package com.ae.solarsystem

import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.util.lerp as composeLerp
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

// Portrait hero animation
private val HeroScrollRange = 260.dp

// Cards
private val CardHeight = 242.dp
private val CardSpacing = 18.dp
private val CardCorner = 20.dp

// Stacking overlay
private val StackRevealStep = 12.dp
private val StackTopOffset = 300.dp
private const val CardFadeDistanceFraction = 0.90f
private const val PlanetImageMinAlpha = 0.32f

// Planet card layout
private val PlanetCardHorizontalPadding = 20.dp
private val PlanetImageSize = 126.dp
private val PlanetImageOffsetX = 20.dp
private val PlanetImageOffsetY = (-14).dp
private val PlanetTextOffsetX = 150.dp
private val PlanetTextOffsetY = 16.dp

// Hero Earth animation
private val HeroEarthFinalTop = 36.dp
private const val HeroEarthStartScale = 1.72f
private const val HeroEarthEndScale = 0.66f
private const val HeroEarthStartTopFraction = 0.40f
private const val HeroEarthMinAlpha = 0.5f

// Hero text animation — two-phase split point
private const val HeroPhaseThreshold = 0.5f

private val HeroTitleTopPadding = 54.dp
private val HeroSubtitleTopPadding = 132.dp
private val HeroTextHiddenOffset = 220.dp

// Landscape constants
private const val LandscapeLeftPanelFraction = 0.42f
private val LandscapeCardHorizontalPadding = 12.dp
private val LandscapeCardSpacing = 12.dp
// Cards stack starting much closer to the top since the panel is shorter than a full screen
private val LandscapeStackTopOffset = 16.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.BLACK)
        )

        setContent {
            MaterialTheme {
                SolarSystemScreen()
            }
        }
    }
}

@Composable
private fun SolarSystemScreen() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout()
    } else {
        PortraitLayout()
    }
}

/* ----------------------------- Portrait Layout ----------------------------- */

@Composable
private fun PortraitLayout() {
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = configuration.screenHeightDp.dp

    val heroScrollRangePx = remember(density) {
        with(density) { HeroScrollRange.toPx() }
    }

    val heroProgress = remember(heroScrollRangePx) {
        derivedStateOf {
            val scroll = if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                heroScrollRangePx
            }
            (scroll / heroScrollRangePx).coerceIn(0f, 1f)
        }
    }

    val totalScrollPx = remember(density, screenHeight) {
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
            progressProvider = { heroProgress.value },
            screenHeight = screenHeight,
            modifier = Modifier.zIndex(1000f)
        )

        PortraitScrollDriver(
            listState = listState,
            heroHeight = screenHeight
        )

        PlanetStackOverlay(
            totalScrollPxProvider = { totalScrollPx.value },
            heroHeight = screenHeight
        )
    }
}

/* ----------------------------- Landscape Layout ----------------------------- */

@Composable
private fun LandscapeLayout() {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    // screenHeightDp is the SHORT dimension in landscape
    val panelHeight = configuration.screenHeightDp.dp

    // Earth progress is driven by card scroll: full animation over 3 card strides.
    // This ensures Phase 1 text exits (~stride×1.5) right as the first card slides in.
    val earthScrollRangePx = remember(density) {
        with(density) { (CardHeight + LandscapeCardSpacing).toPx() * 3f }
    }

    val totalScrollPx = remember(density) {
        derivedStateOf {
            val itemStridePx = with(density) { (CardHeight + LandscapeCardSpacing).toPx() }
            (listState.firstVisibleItemIndex * itemStridePx) +
                    listState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val earthProgress = remember(earthScrollRangePx) {
        derivedStateOf {
            (totalScrollPx.value / earthScrollRangePx).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SpaceBackground()

        Row(modifier = Modifier.fillMaxSize()) {
            // ---- Left panel: Earth + phase 2 text (centered on Earth) ----
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(LandscapeLeftPanelFraction)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                LandscapeEarth(progressProvider = { earthProgress.value })
                // Phase 2 text: drops in from above, slightly above center to track Earth drift
                LandscapePhase2Text(progressProvider = { earthProgress.value })
            }

            // ---- Right panel: cards + Phase 1 text centered in empty space ----
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                LandscapeScrollDriver(listState = listState)
                // Pass panelHeight so cards start below the Phase 1 text area
                LandscapePlanetStackOverlay(
                    totalScrollPxProvider = { totalScrollPx.value },
                    panelHeight = panelHeight
                )
                // Phase 1 text: centered + slightly below midpoint, exits upward
                LandscapePhase1Text(progressProvider = { earthProgress.value })
            }
        }
    }
}

/**
 * Transparent scroll driver for the landscape right panel.
 * No initial spacer — planet phantom spacers start at index 0 so cards are visible immediately.
 */
@Composable
private fun LandscapeScrollDriver(listState: LazyListState) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(LandscapeCardSpacing),
        contentPadding = PaddingValues(
            top = LandscapeStackTopOffset,
            bottom = CardHeight + 24.dp
        )
    ) {
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
            Spacer(modifier = Modifier.height(24.dp).navigationBarsPadding())
        }
    }
}

/**
 * Positions planet cards as an overlay inside the landscape right panel.
 * heroHeightPx = 58% of panel height so cards start in the lower half,
 * clearing the centered Phase 1 text above them.
 */
@Composable
private fun LandscapePlanetStackOverlay(
    totalScrollPxProvider: () -> Float,
    panelHeight: Dp
) {
    val density = LocalDensity.current

    val metrics = remember(density, panelHeight) {
        StackMetrics(
            // Cards start at 102% of panel height — just below the bottom edge, completely hidden
            heroHeightPx = with(density) { panelHeight.toPx() } * 1.02f,
            cardHeightPx = with(density) { CardHeight.toPx() },
            cardSpacingPx = with(density) { LandscapeCardSpacing.toPx() },
            stackTopPx = with(density) { LandscapeStackTopOffset.toPx() },
            revealStepPx = with(density) { StackRevealStep.toPx() },
            itemCount = planets.size
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        planets.forEachIndexed { index, planet ->
            PlanetCard(
                planet = planet,
                cardStateProvider = {
                    calculateOverlayCardState(
                        totalScrollPx = totalScrollPxProvider(),
                        metrics = metrics,
                        index = index
                    )
                },
                modifier = Modifier
                    .padding(horizontal = LandscapeCardHorizontalPadding)
                    .fillMaxWidth()
                    .height(CardHeight)
                    .zIndex(index.toFloat())
            )
        }
    }
}

/**
 * Earth image for landscape left panel. Animates scale/alpha/position during the
 * panel-spacer scroll phase (progress 0→1), mirroring portrait [HeroEarth].
 */
@Composable
private fun LandscapeEarth(progressProvider: () -> Float) {
    Image(
        painter = painterResource(id = R.drawable.earth),
        contentDescription = "Earth",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val p = progressProvider()
                // Scale down and drift up as cards slide in, mirroring portrait hero animation
                val scale = lerpClamped(1.18f, 0.82f, p)
                scaleX = scale
                scaleY = scale
                alpha = lerpClamped(1f, 0.60f, smoothStep(p))
                // Slight downward start offset (+3%) so Earth sits lower initially
                translationY = lerpClamped(size.height * 0.03f, -size.height * 0.10f, p)
            }
    )
}

/**
 * Phase 1 text for the landscape RIGHT panel.
 * "Earth" title + subtitle + SwipeCue. Centered with a slight downward bias (+28dp)
 * so it sits in the empty space above the cards. Slides UP and fades OUT on scroll.
 */
@Composable
private fun LandscapePhase1Text(progressProvider: () -> Float) {
    val density = LocalDensity.current
    val hiddenOffsetPx = remember(density) { with(density) { 100.dp.toPx() } }
    // Small downward bias so the text rests just below the visual center of the panel,
    // placing it clearly in the empty space above the card stack.
    val biasDownPx = remember(density) { with(density) { 28.dp.toPx() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .graphicsLayer {
                val firstPhase = (progressProvider() / HeroPhaseThreshold).coerceIn(0f, 1f)
                // Start at +biasDownPx (below center), exit at -hiddenOffsetPx (above panel)
                translationY = lerpClamped(biasDownPx, -hiddenOffsetPx, firstPhase)
                alpha = lerpClamped(1f, 0f, firstPhase)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Earth",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color.White,
                fontSize = 42.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = RubikFontFamily
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "A tiny blue world drifting\nthrough the endless dark.",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontFamily = LilyScriptFontFamily,
                fontWeight = FontWeight.Normal
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        // SwipeCue inline — same fade logic as portrait
        ArrowStack(
            modifier = Modifier.graphicsLayer {
                val eased = smoothStep(progressProvider())
                alpha = 1f - (eased * 2f).coerceIn(0f, 1f)
                translationY = -14f * eased
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Swipe to explore",
            modifier = Modifier.graphicsLayer {
                val eased = smoothStep(progressProvider())
                alpha = 1f - (eased * 2f).coerceIn(0f, 1f)
            },
            style = TextStyle(
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = RubikFontFamily
            )
        )
    }
}

/**
 * Phase 2 text for the landscape LEFT panel.
 * "Our Solar System" centered on the Earth image.
 * Drops in from above and settles slightly above center (-22dp) to track the Earth's
 * upward drift (Earth translationY = -10% of height at progress=1).
 */
@Composable
private fun LandscapePhase2Text(progressProvider: () -> Float) {
    val density = LocalDensity.current
    val hiddenOffsetPx = remember(density) { with(density) { 80.dp.toPx() } }
    // Match the Earth's upward drift so text appears centered on the moving planet
    val biasUpPx = remember(density) { with(density) { 22.dp.toPx() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val progress = progressProvider()
                val secondPhase = ((progress - HeroPhaseThreshold) / HeroPhaseThreshold)
                    .coerceIn(0f, 1f)
                alpha = lerpClamped(0f, 1f, secondPhase)
                // Drops from -hiddenOffsetPx (above) to -biasUpPx (slightly above center)
                translationY = lerpClamped(-hiddenOffsetPx, -biasUpPx, secondPhase)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Our Solar\nSystem",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color.White,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RubikFontFamily
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Explore the planets",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                fontFamily = LilyScriptFontFamily
            )
        )
    }
}

@Composable
private fun PortraitScrollDriver(
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
    totalScrollPxProvider: () -> Float,
    heroHeight: Dp
) {
    val density = LocalDensity.current

    val metrics = remember(density, heroHeight) {
        StackMetrics(
            heroHeightPx = with(density) { heroHeight.toPx() },
            cardHeightPx = with(density) { CardHeight.toPx() },
            cardSpacingPx = with(density) { CardSpacing.toPx() },
            stackTopPx = with(density) { StackTopOffset.toPx() },
            revealStepPx = with(density) { StackRevealStep.toPx() },
            itemCount = planets.size
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        planets.forEachIndexed { index, planet ->
            PlanetCard(
                planet = planet,
                cardStateProvider = {
                    calculateOverlayCardState(
                        totalScrollPx = totalScrollPxProvider(),
                        metrics = metrics,
                        index = index
                    )
                },
                modifier = Modifier
                    .padding(horizontal = PlanetCardHorizontalPadding)
                    .fillMaxWidth()
                    .height(CardHeight)
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
        val fadeDistance = metrics.cardHeightPx * CardFadeDistanceFraction
        (1f - (distanceToStack / fadeDistance)).coerceIn(0f, 1f)
    } else {
        0f
    }

    return OverlayCardState(
        top = top,
        planetImageAlpha = lerpClamped(1f, PlanetImageMinAlpha, smoothStep(nextCardProgress))
    )
}

@Composable
private fun HeroSection(
    progressProvider: () -> Float,
    screenHeight: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HeroEarth(
            progressProvider = progressProvider,
            screenHeight = screenHeight
        )
        HeroTexts(progressProvider = progressProvider)
        SwipeCue(progressProvider = progressProvider)
    }
}

@Composable
private fun HeroTexts(progressProvider: () -> Float) {
    val density = LocalDensity.current
    // Remembered so density-to-px conversion doesn't re-run on every recomposition
    val hiddenOffsetPx = remember(density) { with(density) { HeroTextHiddenOffset.toPx() } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = HeroTitleTopPadding)
                .graphicsLayer {
                    val firstPhase = (progressProvider() / HeroPhaseThreshold).coerceIn(0f, 1f)
                    translationY = lerpClamped(0f, -hiddenOffsetPx, firstPhase)
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
                .padding(top = HeroSubtitleTopPadding)
                .graphicsLayer {
                    val progress = progressProvider()
                    translationY = if (progress < HeroPhaseThreshold) {
                        -hiddenOffsetPx
                    } else {
                        val secondPhase = ((progress - HeroPhaseThreshold) / HeroPhaseThreshold)
                            .coerceIn(0f, 1f)
                        lerpClamped(-hiddenOffsetPx, 0f, secondPhase)
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
private fun BoxScope.HeroEarth(
    progressProvider: () -> Float,
    screenHeight: Dp
) {
    val density = LocalDensity.current
    // Remembered: these depend only on density and compile-time constants
    val startTopPx = remember(density, screenHeight) {
        with(density) { screenHeight.toPx() } * HeroEarthStartTopFraction
    }
    val finalTopPx = remember(density) { with(density) { HeroEarthFinalTop.toPx() } }

    // Uses BoxScope.align — parent HeroSection's Box provides the scope
    Image(
        painter = painterResource(id = R.drawable.earth),
        contentDescription = "Earth",
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .graphicsLayer {
                val progress = progressProvider()
                val scale = lerpClamped(HeroEarthStartScale, HeroEarthEndScale, progress)
                scaleX = scale
                scaleY = scale
                translationY = lerpClamped(startTopPx, finalTopPx, progress)
                alpha = lerpClamped(1f, HeroEarthMinAlpha, smoothStep(progress))
                transformOrigin = TransformOrigin(0.5f, 0f)
                clip = false
            }
    )
}

@Composable
private fun SwipeCue(progressProvider: () -> Float) {
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
                val eased = smoothStep(progressProvider())
                alpha = 1f - (eased * 2f).coerceIn(0f, 1f)
                translationY = -14f * eased
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Swipe up to explore",
            modifier = Modifier.graphicsLayer {
                val eased = smoothStep(progressProvider())
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
            tint = Color.White
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_up),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White.copy(alpha = 0.7f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_up),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun SpaceBackground() {
    // Single Box with gradient applied directly — removes one unnecessary layout node
    Box(
        modifier = Modifier
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
    ) {
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

    // Remembered so dp→px conversions don't re-run on every recomposition
    val shadowGeometry = remember(density) {
        val blurPx = with(density) { 100.dp.toPx() }
        val planetSizePx = with(density) { PlanetImageSize.toPx() }
        val offsetXPx = with(density) { PlanetImageOffsetX.toPx() }
        val offsetYPx = with(density) { PlanetImageOffsetY.toPx() }
        ShadowGeometry(blurPx, planetSizePx, offsetXPx, offsetYPx)
    }

    // Paint is recreated only when color or blur changes
    val paint = remember(shadowColor, shadowGeometry.blurPx) {
        FrameworkPaint().apply {
            isAntiAlias = true
            color = shadowColor.copy(alpha = 0.50f).toArgb()
            maskFilter = BlurMaskFilter(shadowGeometry.blurPx / 2f, BlurMaskFilter.Blur.NORMAL)
        }
    }

    Canvas(
        modifier = modifier.graphicsLayer { clip = false }
    ) {
        drawIntoCanvas { canvas ->
            val cx = shadowGeometry.offsetXPx + (shadowGeometry.planetSizePx * 0.50f)
            val cy = shadowGeometry.offsetYPx + (shadowGeometry.planetSizePx * 0.54f)
            val radius = shadowGeometry.planetSizePx * 0.58f
            canvas.nativeCanvas.drawCircle(cx, cy, radius, paint)
        }
    }
}

/** Holds pre-computed pixel values for shadow placement; avoids re-allocating on each frame. */
@Immutable
private data class ShadowGeometry(
    val blurPx: Float,
    val planetSizePx: Float,
    val offsetXPx: Float,
    val offsetYPx: Float
)

@Composable
private fun PlanetCard(
    planet: Planet,
    cardStateProvider: () -> OverlayCardState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.graphicsLayer {
            clip = false
            // Read provider once — avoids double allocation and double calculation per frame
            val state = cardStateProvider()
            translationY = state.top
        }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(CardCorner))
                .background(Color(0xFF0B1223).copy(alpha = 0.8f))
                .border(
                    width = 0.5.dp,
                    color = Color(0xFF2F2E2E),
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
                    // Second read is intentional and isolated to this layer only
                    alpha = cardStateProvider().planetImageAlpha
                }
        )

        Column(
            modifier = Modifier
                .offset(x = PlanetTextOffsetX, y = PlanetTextOffsetY)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = planet.name,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RubikFontFamily
                )
            )

            Text(
                text = planet.subtitle,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.66f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
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
    @DrawableRes drawableId: Int,
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
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                iconDrawableId = R.drawable.ic_weight_scale,
                label = "You Would Weigh",
                value = planet.weight,
                isTemperature = false,
                modifier = Modifier.weight(1f)
            )

            InfoDivider()

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
                .padding(horizontal = 4.dp)
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.16f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                iconDrawableId = R.drawable.ic_temperature,
                label = "Temperature",
                value = planet.temperature,
                isTemperature = true,
                modifier = Modifier.weight(1f)
            )

            InfoDivider()

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
private fun InfoDivider() {
    Box(
        modifier = Modifier
            .padding(all = 12.dp)
            .width(0.5.dp)
            .height(30.dp)
            .background(Color.White.copy(alpha = 0.16f))
    )
}

@Composable
private fun InfoItem(
    @DrawableRes iconDrawableId: Int,
    label: String,
    value: String,
    isTemperature: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconDrawableId),
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.66f))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                style = TextStyle(
                    color = Color(0xFFA7B0C6),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = RubikFontFamily
                )
            )

            if (isTemperature) {
                Text(
                    text = buildTemperatureAnnotatedString(value),
                    modifier = Modifier.padding(top = 2.dp),
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        fontFamily = RubikFontFamily
                    )
                )
            } else {
                Text(
                    text = value,
                    modifier = Modifier.padding(top = 2.dp),
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
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
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.88f)
                )
            )
            append(value)
            pop()
        }
    } else {
        buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.88f)
                )
            )
            append(value.take(commaIndex + 1))
            pop()

            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.66f)
                )
            )
            append(value.drop(commaIndex + 1))
            pop()
        }
    }
}

private fun smoothStep(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

/**
 * Linearly interpolates between [start] and [end] by [fraction], clamping fraction to [0,1].
 * Named distinctly from the standard [composeLerp] to make the clamping behaviour explicit.
 */
private fun lerpClamped(start: Float, end: Float, fraction: Float): Float =
    composeLerp(start, end, fraction.coerceIn(0f, 1f))

@Immutable
private data class Planet(
    val name: String,
    val subtitle: String,
    val weight: String,
    val day: String,
    val temperature: String,
    val info: String,
    @DrawableRes val drawableId: Int,
    val shadowColor: Color
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