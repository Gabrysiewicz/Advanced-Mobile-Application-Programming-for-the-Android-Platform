package com.example.masterand.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import com.example.masterand.database.Player
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.masterand.dao.PlayerDao
import com.example.masterand.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileListView(
    navController: NavHostController,
    playerDao: PlayerDao,
    viewModel: PlayerViewModel = viewModel()  // Injected ViewModel
) {
    // Collect the players as state from the ViewModel
    val players = viewModel.getPlayers(playerDao).collectAsState(initial = emptyList())

    // Track whether the scale should increase or decrease
    val isIncreased = remember { mutableStateOf(true) }

    // Create a coroutine scope to reverse the scale animation
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Wait for 1 second before toggling the scale
            isIncreased.value = !isIncreased.value
        }
    }

    // Scale animation based on isIncreased
    val scaleState = animateFloatAsState(
        targetValue = if (isIncreased.value) 1.2f else 1f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        )
    )

    // State for dialog visibility
    val showDialog = remember { mutableStateOf(false) }
    val selectedPlayer = remember { mutableStateOf<Player?>(null) }

    // Create a coroutine scope to delete the player with scores
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Animated Title: MasterAnd with scaling effect
            Text(
                text = "MasterAnd",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .scale(scaleState.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Select Profile",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Show the list of players
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players.value) { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        // Show dialog when long press detected
                                        selectedPlayer.value = player
                                        showDialog.value = true
                                    }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Display player's image here if there is one
                            if (player.imageUri != null) {
                                Image(
                                    painter = rememberImagePainter(player.imageUri),
                                    contentDescription = "Player Image",
                                    modifier = Modifier.size(50.dp)
                                )
                            } else {
                                // Provide a placeholder image if no image is available
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Player Image",
                                    modifier = Modifier.size(50.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text(player.name, style = MaterialTheme.typography.bodyLarge)
                                Text(player.email, style = MaterialTheme.typography.bodySmall)
                            }

                            // Button to navigate to GameView
                            Button(
                                onClick = {
                                    navController.navigate("ProfileView/${player.id}")
                                }
                            ) {
                                Text("Play As")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Profile Button
            Button(
                onClick = {
                    navController.navigate("CreateProfile")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create New Profile")
            }
        }

        // Show the dialog when the user performs longPress on a profile
        if (showDialog.value && selectedPlayer.value != null) {
            val player = selectedPlayer.value!!
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Profile Options") },
                text = {
                    Column {
                        Text("Choose an action for ${player.name}:")
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Perform the delete action inside the coroutine scope
                            coroutineScope.launch {
                                playerDao.deletePlayerWithScores(player)
                                showDialog.value = false

                                // Clear the navigation back stack after deletion, IMPORTANT: prevents bugs
                                navController.navigate("ProfileList") {
                                    popUpTo("ProfileList") { inclusive = true } // This clears all entries up to "ProfileList"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error // Set red color for delete button
                        )
                    ) {
                        Text("Delete with All Scores")
                    }
                }
                ,
                dismissButton = {
                    Button(
                        onClick = {
                            // Navigate to edit screen
                            navController.navigate("EditProfile/${player.id}")
                            showDialog.value = false
                        }
                    ) {
                        Text("Edit")
                    }
                }
            )
        }
    }
}






