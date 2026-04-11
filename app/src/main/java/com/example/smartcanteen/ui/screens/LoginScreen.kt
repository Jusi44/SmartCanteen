package com.example.smartcanteen.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.ui.theme.CanteenOrangeLight

@Composable
fun LoginScreen(onLogin: (String, String, (Boolean) -> Unit) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        val transition = rememberInfiniteTransition(label = "login_bg")
        val scale by transition.animateFloat(
            initialValue = 1f, targetValue = 1.02f,
            animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "scale"
        )

        Card(
            modifier = Modifier.fillMaxWidth(0.88f).padding(16.dp).scale(scale),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = CanteenOrangeLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(45.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("SmartCanteen", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("Management System", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(40.dp))
                
                OutlinedTextField(
                    value = username, onValueChange = { username = it; loginError = null },
                    label = { Text("Username") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), isError = loginError != null,
                    singleLine = true, enabled = !isLoggingIn
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it; loginError = null },
                    label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp), isError = loginError != null,
                    singleLine = true, enabled = !isLoggingIn
                )
                
                if (loginError != null) {
                    Text(text = loginError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { 
                        isLoggingIn = true
                        onLogin(username, password) { success ->
                            if (!success) {
                                isLoggingIn = false
                                loginError = "Invalid credentials. Please try again."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoggingIn
                ) {
                    if (isLoggingIn) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SIGN IN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
