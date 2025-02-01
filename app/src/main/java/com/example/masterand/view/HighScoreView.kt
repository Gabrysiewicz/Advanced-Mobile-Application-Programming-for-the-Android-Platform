import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.masterand.dao.GameResultWithPlayer
import com.example.masterand.dao.PlayerDao
import com.example.masterand.R

@Composable
fun HighScoreView(navController: NavHostController, playerDao: PlayerDao, playerId: Int) {
    // State to hold the high scores
    val highScores = remember { mutableStateOf<List<GameResultWithPlayer>>(emptyList()) }

    // Fetch the high scores from the database in a LaunchedEffect
    LaunchedEffect(Unit) {
        // Fetching high scores from the database
        highScores.value = playerDao.getHighScores()
    }

    // Display the high scores
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "High Scores",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(highScores.value) { index, gameResultWithPlayer ->
                    HighScoreRow(gameResultWithPlayer)
                }
            }

            // Buttons at the bottom
            Spacer(modifier = Modifier.height(16.dp))

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

            // Return to Player List button
            Button(
                onClick = {
                    navController.popBackStack("ProfileList", inclusive = false)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to Player List")
            }
        }
    }
}


@Composable
fun HighScoreRow(gameResultWithPlayer: GameResultWithPlayer) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Display the profile picture (if available)
        Image(
            painter = rememberImagePainter(
                data = gameResultWithPlayer.player.imageUri.takeIf { !it.isNullOrEmpty() }
                    ?: R.drawable.ic_launcher_foreground // Default image
            ),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Display the player's name
        Text(
            text = gameResultWithPlayer.player.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Display the color count and score
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Color Count: ${gameResultWithPlayer.gameResult.colorCount}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Score: ${gameResultWithPlayer.gameResult.score}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}



