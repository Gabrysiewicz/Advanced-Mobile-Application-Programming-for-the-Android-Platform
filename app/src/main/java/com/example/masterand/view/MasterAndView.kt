package com.example.masterand.view

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.masterand.R
import kotlinx.coroutines.delay

@Composable
fun MasterAndView(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var colorCount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Regex for validating email
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    var isGrowing by remember { mutableStateOf(true) }
    val textScale by animateFloatAsState(
        targetValue = if (isGrowing) 2f else 1.5f,
        animationSpec = tween(durationMillis = 2000)
    )

    LaunchedEffect(key1 = isGrowing) {
        delay(2000)
        isGrowing = !isGrowing
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
                text = "MasterAnd",
                style = TextStyle(fontSize = 30.sp, color = Color.White),
                modifier = Modifier.scale(textScale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(128.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    val context = LocalContext.current
                    val bitmap = try {
                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            selectedImageUri
                        ).asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Selected Image",
                            modifier = Modifier.size(128.dp)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Placeholder Icon",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Upload Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter name") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.isNotEmpty() && name.isBlank()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter email") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.isNotEmpty() && !emailRegex.matches(email)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = colorCount,
                onValueChange = { colorCount = it },
                label = { Text("Enter number of colors (4-10)") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.isNotEmpty() && (colorCount.isBlank() ||
                        colorCount.toIntOrNull() == null ||
                        colorCount.toIntOrNull() !in 4..10)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Validate inputs
                    when {
                        name.isBlank() -> errorMessage = "Name cannot be empty"
                        email.isBlank() -> errorMessage = "Email cannot be empty"
                        !emailRegex.matches(email) -> errorMessage = "Enter a valid email"
                        colorCount.isBlank() -> errorMessage = "Number of colors cannot be empty"
                        colorCount.toIntOrNull() == null -> errorMessage = "Number of colors must be a valid number"
                        colorCount.toIntOrNull() !in 4..10 -> errorMessage = "Number of colors must be between 4 and 10"
                        else -> {
                            errorMessage = ""
                            try {
                                navController.currentBackStackEntry?.arguments?.apply {
                                    putString("playerName", name)
                                    putString("playerEmail", email)
                                    putString("imageUri", selectedImageUri?.toString())
                                    putInt("colorCount", colorCount.toInt())
                                }
                                navController.navigate("GameView/${colorCount.toInt()}")
                            } catch (e: Exception) {
                                errorMessage = "An error occurred. Please try again."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Next")
            }
        }
    }
}


