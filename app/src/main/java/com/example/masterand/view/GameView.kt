import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.masterand.dao.PlayerDao
import com.example.masterand.database.GameResult

@Composable
fun GameView(
    navController: NavHostController,
    availableColors: List<Color>,
    playerId: Int,
    playerDao: PlayerDao
) {
    val targetColors = remember { availableColors.shuffled().take(4) }
    val maxAttempts = 10
    var attempts by remember { mutableStateOf(listOf<Pair<List<Color>, List<Color>>>()) }
    var currentGuess by remember { mutableStateOf(List(4) { Color.Gray }) }
    var gameWon by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var currentAttemptConfirmed by remember { mutableStateOf(false) }

    // Save game result when game is won
    LaunchedEffect(gameWon) {
        if (gameWon) {
            val score = maxAttempts - attempts.size

            // Ensure playerDao is not null before proceeding
            playerDao?.let {
                val gameResult = GameResult(
                    playerId = playerId,
                    colorCount = availableColors.size,
                    score = score
                )

                // Insert the game result into the database
                it.insertGameResult(gameResult)

                // Navigate to High Score view
                navController.navigate("HighScoreView/$playerId")
            } ?: run {
                Log.e("GameView", "playerDao is null!")
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Attempts: ${attempts.size} / $maxAttempts",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attempts.size) { index ->
                    AttemptRow(attempts[index])
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Guess
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                currentGuess.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(color, CircleShape)
                            .clickable(enabled = !currentAttemptConfirmed) {
                                // Get available colors excluding those already in current guess
                                val usedColors = currentGuess.filterNot { it == Color.Gray }
                                val availableForSelection = availableColors.filterNot { it in usedColors }

                                // Find next available color
                                val nextColor = when {
                                    color == Color.Gray -> availableForSelection.firstOrNull() ?: color
                                    else -> {
                                        val currentIndex = availableColors.indexOf(color)
                                        availableForSelection.firstOrNull { availableColors.indexOf(it) > currentIndex }
                                            ?: availableForSelection.firstOrNull()
                                            ?: color
                                    }
                                }

                                currentGuess = currentGuess.toMutableList().apply {
                                    this[index] = nextColor
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button for guessing the colors
            Button(
                onClick = {
                    val feedback = evaluateGuess(currentGuess, targetColors)
                    attempts = attempts + (currentGuess to feedback)
                    currentAttemptConfirmed = true

                    if (feedback.all { it == Color.Green }) {
                        gameWon = true
                    } else if (attempts.size >= maxAttempts) {
                        gameOver = true
                    } else {
                        // Reset for next attempt
                        currentGuess = List(4) { Color.Gray }
                        currentAttemptConfirmed = false
                    }
                },
                enabled = !gameWon && !gameOver && !currentAttemptConfirmed &&
                        currentGuess.all { it != Color.Gray } &&
                        currentGuess.distinct().size == 4,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm")
            }

            // Display game over or game won buttons
            if (gameWon || gameOver) {
                Spacer(modifier = Modifier.height(16.dp))

                // Button to go back to Profile
                Button(
                    onClick = {
                        navController.navigate("ProfileView/$playerId") {
                            popUpTo("ProfileView") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Profile")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button to go to Highscore view
                Button(
                    onClick = {
                        navController.navigate("HighScoreView/$playerId") {
                            popUpTo("HighScoreView") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("See Highscores")
                }
            }
        }
    }
}


@Composable
private fun AttemptRow(attempt: Pair<List<Color>, List<Color>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Guess
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            attempt.first.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Feedback
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            attempt.second.forEach { feedbackColor ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(feedbackColor, CircleShape)
                )
            }
        }
    }
}

private fun evaluateGuess(guess: List<Color>, target: List<Color>): List<Color> {
    val feedback = mutableListOf<Color>()
    val unusedTargetIndices = target.indices.toMutableSet()
    val unusedGuessIndices = guess.indices.toMutableSet()

    // First pass: find exact matches (red)
    guess.indices.forEach { i ->
        if (guess[i] == target[i]) {
            feedback.add(Color.Green)
            unusedTargetIndices.remove(i)
            unusedGuessIndices.remove(i)
        }
    }

    // Second pass: find color matches in wrong positions (yellow)
    val remainingTargetColors = unusedTargetIndices.map { target[it] }
    unusedGuessIndices.forEach { i ->
        val guessColor = guess[i]
        if (guessColor in remainingTargetColors) {
            feedback.add(Color.Yellow)
            remainingTargetColors.indexOf(guessColor).let { targetIndex ->
                unusedTargetIndices.remove(targetIndex)
            }
        } else {
            feedback.add(Color.Gray)
        }
    }

    // Instead of sorting, arrange in a consistent order: Green, Yellow, Gray
    return feedback.sortedBy { color ->
        when (color) {
            Color.Green -> 0
            Color.Yellow -> 1
            else -> 2
        }
    }
}
