package com.example.masterand.view

import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.masterand.dao.PlayerDao
import com.example.masterand.database.Player
import com.example.masterand.R

@Composable
fun ProfileView(navController: NavHostController, playerId: Int, playerDao: PlayerDao) {
    val player = remember { mutableStateOf<Player?>(null) }

    // Fetch the player data when the composable is launched
    LaunchedEffect(playerId) {
        player.value = playerDao.getPlayerById(playerId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            player.value?.let {
                // Display profile image or fallback to default image
                if (it.imageUri != null) {
                    val context = LocalContext.current
                    val bitmap = try {
                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            Uri.parse(it.imageUri)
                        ).asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }

                    bitmap?.let { imageBitmap ->
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Profile Image",
                            modifier = Modifier.size(128.dp)
                        )
                    }
                } else {
                    val defaultImage = painterResource(id = R.drawable.ic_launcher_foreground)
                    Image(
                        painter = defaultImage,
                        contentDescription = "Default Profile Image",
                        modifier = Modifier.size(128.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Name: ${it.name}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Email: ${it.email}", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))

                // Button for navigating back to Profile List
                Button(
                    onClick = { navController.navigate("ProfileList") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Player List")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button for High Score view
                Button(
                    onClick = { navController.navigate("HighScoreView/$playerId") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("High Score")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "To play choose color count",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Dynamic buttons for the Game View with color count
                // 2x3 grid layout for the buttons
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // 3 columns per row
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Loop through color counts from 4 to 10
                    (4..10).forEach { count ->
                        item {
                            // Circular button for each color count
                            Button(
                                onClick = { navController.navigate("GameView/$count/$playerId") },
                                modifier = Modifier
                                    .size(60.dp) // Makes button circular
                                    .padding(1.dp),
                                shape = CircleShape, // Makes button circular
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // Customize button color
                            ) {
                                Text("$count")
                            }
                        }
                    }
                }
            } ?: run {
                Text(text = "Player not found", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}


