package com.example.masterand.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.masterand.dao.PlayerDao
import com.example.masterand.database.Player
import kotlinx.coroutines.launch

@Composable
fun EditProfileView(navController: NavHostController, playerId: Int, playerDao: PlayerDao) {
    // Track the state of the inputs
    var player by remember { mutableStateOf<Player?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    val coroutineScope = rememberCoroutineScope()

    // Coroutine to fetch player data
    LaunchedEffect(playerId) {
        playerDao.getPlayerById(playerId)?.let {
            player = it
            name = it.name
            email = it.email
            imageUri = it.imageUri?.let { uri -> Uri.parse(uri) }
        }
        isLoading = false
    }

    // Email validation regex pattern
    val emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$".toRegex()

    // Error messages
    val emailError = if (email.isNotEmpty() && !email.matches(emailPattern)) {
        "Invalid email format"
    } else {
        null
    }

    val nameError = if (name.length < 3) {
        "Name must be at least 3 characters"
    } else {
        null
    }

    // Show loading spinner until player data is fetched
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Edit Profile", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(16.dp))

            // Name Input Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter Name") },
                isError = nameError != null,  // Show error if the name is too short
                modifier = Modifier.fillMaxWidth()
            )
            // Show name error message if any
            if (nameError != null) {
                Text(
                    text = nameError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter Email") },
                isError = emailError != null,  // Show error if the email is invalid
                modifier = Modifier.fillMaxWidth()
            )
            // Show email error message if any
            if (emailError != null) {
                Text(
                    text = emailError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload Image Button
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Upload Image")
            }

            // Display selected image (if any)
            imageUri?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Profile Button
            Button(
                onClick = {
                    if (name.isNotEmpty() && email.isNotEmpty() && email.matches(emailPattern) && name.length >= 3) {
                        val updatedPlayer = player?.copy(name = name, email = email, imageUri = imageUri?.toString())
                        updatedPlayer?.let {
                            coroutineScope.launch {
                                // Update player in the background
                                playerDao.updatePlayer(it)

                                // Navigate back to Profile List
                                navController.navigate("ProfileList") {
                                    popUpTo("ProfileList") { inclusive = true }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.length >= 3 && email.isNotEmpty() && email.matches(emailPattern)  // Enable if valid
            ) {
                Text("Save Changes")
            }
        }
    }
}
