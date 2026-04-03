package com.argesurec.shared.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.viewmodel.koinViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.argesurec.shared.viewmodel.AuthViewModel
import com.argesurec.shared.ui.theme.ArgepColors
import com.argesurec.shared.util.isWeb
import org.jetbrains.compose.resources.painterResource
import argep.shared.generated.resources.Res
import argep.shared.generated.resources.login_hero

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<AuthViewModel>()
        val state by viewModel.state.collectAsState()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var rememberMe by remember { mutableStateOf(false) }

        if (isWeb) {
            WebAuthContent(
                email = email,
                password = password,
                rememberMe = rememberMe,
                isLoading = state.isLoading,
                error = state.error,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onRememberMeChange = { rememberMe = it },
                onLoginClick = { viewModel.signIn(email, password) },
                onRegisterClick = { navigator.push(RegisterScreen()) }
            )
        } else {
            // Existing Mobile Content (Simplified for brevity but kept functional)
            MobileAuthContent(
                email = email,
                password = password,
                isLoading = state.isLoading,
                error = state.error,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onLoginClick = { viewModel.signIn(email, password) },
                onRegisterClick = { navigator.push(RegisterScreen()) }
            )
        }
    }

    @Composable
    private fun WebAuthContent(
        email: String,
        password: String,
        rememberMe: Boolean,
        isLoading: Boolean,
        error: String?,
        onEmailChange: (String) -> Unit,
        onPasswordChange: (String) -> Unit,
        onRememberMeChange: (Boolean) -> Unit,
        onLoginClick: () -> Unit,
        onRegisterClick: () -> Unit
    ) {
        Row(modifier = Modifier.fillMaxSize().background(Color.White)) {
            // Left Side: Hero Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(ArgepColors.ExecutivePrimary),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo Image Placeholder (User will put login_hero.png)
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(140.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    try {
                        Image(
                            painter = painterResource(Res.drawable.login_hero),
                            contentDescription = "Logo",
                            modifier = Modifier.size(240.dp),
                            contentScale = ContentScale.Fit
                        )
                    } catch (e: Exception) {
                        // Fallback if image not found
                        Text("Logo", color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.headlineLarge)
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    "Ar-Ge Yönetim",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White
                )
                Text(
                    "Sistemi",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp
                    ),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Right Side: Form Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 120.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Hoş Geldiniz",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ArgepColors.ExecutivePrimary
                    )
                )
                Text(
                    "Lütfen hesap bilgilerinizle giriş yapın.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ArgepColors.Slate500
                )

                Spacer(modifier = Modifier.height(48.dp))

                ModernInputField(
                    label = "E-POSTA",
                    value = email,
                    placeholder = "ornek@argep.com",
                    onValueChange = onEmailChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    ModernInputField(
                        label = "ŞİFRE",
                        value = password,
                        placeholder = "••••••••",
                        isPassword = true,
                        onValueChange = onPasswordChange
                    )
                    TextButton(
                        onClick = { /* Forgot Password logic */ },
                        modifier = Modifier.align(Alignment.TopEnd).offset(y = (-4).dp)
                    ) {
                        Text("Şifremi Unuttum", color = ArgepColors.ExecutiveSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = onRememberMeChange,
                        colors = CheckboxDefaults.colors(checkedColor = ArgepColors.ExecutivePrimary)
                    )
                    Text("Beni hatırla", style = MaterialTheme.typography.bodyMedium, color = ArgepColors.Slate600)
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.ExecutivePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Giriş Yap", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }

                error?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
                
                Divider(color = ArgepColors.Slate200, thickness = 1.dp)
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hesabınız yok mu?", color = ArgepColors.Slate500)
                    TextButton(onClick = onRegisterClick) {
                        Text("Kaydolun", color = ArgepColors.ExecutiveSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    private fun MobileAuthContent(
        email: String,
        password: String,
        isLoading: Boolean,
        error: String?,
        onEmailChange: (String) -> Unit,
        onPasswordChange: (String) -> Unit,
        onLoginClick: () -> Unit,
        onRegisterClick: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Argep", style = MaterialTheme.typography.displaySmall, color = ArgepColors.ExecutivePrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-posta") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Şifre") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArgepColors.ExecutivePrimary)
                ) {
                    Text("Giriş Yap")
                }
            }

            error?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            TextButton(onClick = onRegisterClick, modifier = Modifier.padding(top = 16.dp)) {
                Text("Hesabınız yok mu? Kayıt olun")
            }
        }
    }
}

@Composable
fun ModernInputField(
    label: String,
    value: String,
    placeholder: String,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            ),
            color = ArgepColors.Slate700
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = ArgepColors.Slate400) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ArgepColors.ExecutiveSurfaceLow,
                unfocusedContainerColor = ArgepColors.ExecutiveSurfaceLow,
                disabledContainerColor = ArgepColors.ExecutiveSurfaceLow,
                cursorColor = ArgepColors.ExecutivePrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
        )
    }
}
