package com.example.randomtaprush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.randomtaprush.ui.theme.RandomTapRushTheme
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RandomTapRushTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RandomTapRushApp()
                }
            }
        }
    }
}

data class LeaderboardEntry(val name: String, val score: Int)

data class GameState(
    val isRunning: Boolean = false,
    val timeRemaining: Int = 30,
    val score: Int = 0,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    val targetPosition: Offset = Offset(120f, 120f),
    val targetRadius: Float = 120f,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
)

@Composable
fun RandomTapRushApp() {
    var state by remember { mutableStateOf(GameState()) }
    var playAreaSize by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(state.isRunning) {
        if (state.isRunning) {
            while (state.timeRemaining > 0) {
                delay(1000)
                state = state.copy(timeRemaining = state.timeRemaining - 1)
            }
            state = state.copy(isRunning = false, combo = 0)
        }
    }

    LaunchedEffect(playAreaSize) {
        if (playAreaSize.x > 0f && playAreaSize.y > 0f && !state.isRunning) {
            val (target, radius) = randomTarget()
            state = state.copy(targetPosition = target, targetRadius = radius)
        }
    }

    fun randomTarget(): Pair<Offset, Float> {
        val width = playAreaSize.x
        val height = playAreaSize.y
        if (width <= 0f || height <= 0f) {
            return state.targetPosition to state.targetRadius
        }
        val minRadius = min(width, height) * 0.1f
        val maxRadius = min(width, height) * 0.25f
        val radius = Random.nextFloat() * (maxRadius - minRadius) + minRadius
        val x = Random.nextFloat() * (width - radius * 2) + radius
        val y = Random.nextFloat() * (height - radius * 2) + radius
        return Offset(x, y) to radius
    }

    fun resetGame() {
        val (target, radius) = randomTarget()
        state = GameState(
            isRunning = true,
            timeRemaining = 30,
            score = 0,
            combo = 0,
            bestCombo = 0,
            targetPosition = target,
            targetRadius = radius,
            leaderboard = state.leaderboard
        )
    }

    fun registerHit() {
        if (!state.isRunning) return
        val added = 10 + state.combo * 2
        val newCombo = state.combo + 1
        val newScore = state.score + added
        val newBestCombo = maxOf(state.bestCombo, newCombo)
        val (target, radius) = randomTarget()
        state = state.copy(
            score = newScore,
            combo = newCombo,
            bestCombo = newBestCombo,
            targetPosition = target,
            targetRadius = radius
        )
    }

    fun registerMiss() {
        if (!state.isRunning) return
        state = state.copy(combo = 0)
    }

    RandomTapRushScreen(
        state = state,
        onStart = { resetGame() },
        onHit = { registerHit() },
        onMiss = { registerMiss() },
        onPlayAreaMeasured = { playAreaSize = it }
    )
}

@Composable
fun RandomTapRushScreen(
    state: GameState,
    onStart: () -> Unit,
    onHit: () -> Unit,
    onMiss: () -> Unit,
    onPlayAreaMeasured: (Offset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GameHeader(state = state, onStart = onStart)
        PlayField(
            state = state,
            onHit = onHit,
            onMiss = onMiss,
            onPlayAreaMeasured = onPlayAreaMeasured
        )
        Leaderboard(entries = state.leaderboard)
    }
}

@Composable
fun GameHeader(state: GameState, onStart: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(id = R.string.tap_instructions),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.time_remaining, state.timeRemaining),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = stringResource(id = R.string.score_label, state.score),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(id = R.string.combo_label, state.combo, state.bestCombo),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onStart,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(text = if (state.isRunning) stringResource(id = R.string.restart_button) else stringResource(id = R.string.start_button))
            }
        }
    }
}

@Composable
fun PlayField(
    state: GameState,
    onHit: () -> Unit,
    onMiss: () -> Unit,
    onPlayAreaMeasured: (Offset) -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = state.isRunning) { onMiss() }
                .padding(12.dp)
                .onSizeChanged { onPlayAreaMeasured(Offset(it.width.toFloat(), it.height.toFloat())) }
        ) {
            if (state.isRunning) {
                TargetCircle(state = state, onHit = onHit)
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.waiting_to_start),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TargetCircle(state: GameState, onHit: () -> Unit) {
    val radiusPx by animateFloatAsState(targetValue = state.targetRadius, label = "targetRadius")
    val density = LocalDensity.current
    val diameterDp = with(density) { (radiusPx * 2f).toDp() }
    val offsetXDp = with(density) { (state.targetPosition.x - radiusPx).toDp() }
    val offsetYDp = with(density) { (state.targetPosition.y - radiusPx).toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = offsetXDp, top = offsetYDp)
                .size(diameterDp)
                .clip(CircleShape)
                .clickable { onHit() }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = MaterialTheme.colorScheme.primary, radius = size.minDimension / 2f)
                drawCircle(color = Color.White.copy(alpha = 0.2f), radius = size.minDimension / 2f, style = Stroke(width = 12f))
            }
        }
    }
}

@Composable
fun Leaderboard(entries: List<LeaderboardEntry>) {
    val sampleLeaderboard = if (entries.isEmpty()) {
        listOf(
            LeaderboardEntry("You", 0),
            LeaderboardEntry("Luna", 120),
            LeaderboardEntry("Kai", 100)
        )
    } else entries

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.leaderboard_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleLeaderboard) { entry ->
                    LeaderboardRow(entry = entry)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = stringResource(id = R.string.leaderboard_item, entry.name, entry.score),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(12.dp)
        )
    }
}
