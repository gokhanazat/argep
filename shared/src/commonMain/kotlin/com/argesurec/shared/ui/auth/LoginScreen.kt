package com.argesurec.shared.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
// import org.jetbrains.compose.resources.painterResource
// import argep.shared.generated.resources.Res
// import argep.shared.generated.resources.login_hero

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ArgepColors.ExecutiveBackground),
                contentAlignment = Alignment.Center
            ) {
                WebAuthCard(
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
            }
        } else {
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
    private fun WebAuthCard(
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
        Surface(
            modifier = Modifier
                .width(1000.dp)
                .height(600.dp)
                .shadow(24.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Side: Hero Section
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .background(ArgepColors.ExecutivePrimary)
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(Color.White, RoundedCornerShape(110.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Derleme hatasini engellemek icin gecici olarak metin kullanildi. 
                        // Proje basariyla derlendikten sonra gorsel referansi baglanabilir.
                        Text(
                            "LOGO", 
                            color = ArgepColors.ExecutivePrimary, 
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        /* 
                        Image(
                            painter = painterResource(Res.drawable.login_hero),
                            contentDescription = "Logo",
                            modifier = Modifier.size(180.dp),
                            contentScale = ContentScale.Fit
                        )
                        */
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        "Ar-Ge Yönetim",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        "Sistemi",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 32.sp,
                            letterSpacing = 4.sp
                        )
                    )
                }

                // Right Side: Form Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(60.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Hoş Geldiniz",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ArgepColors.ExecutivePrimary
                        )
                    )
                    Text(
                        "Lütfen hesap bilgilerinizle giriş yapın.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArgepColors.Slate500
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    ModernInputField(
                        label = "E-POSTA",
                        value = email,
                        placeholder = "ornek@argep.com",
                        onValueChange = onEmailChange
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ModernInputField(
                        label = "ŞİFRE",
                        value = password,
                        placeholder = "••••••••",
                        isPassword = true,
                        onValueChange = onPasswordChange
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = onRememberMeChange,
                            colors = CheckboxDefaults.colors(checkedColor = ArgepColors.ExecutivePrimary)
                        )
                        Text("Beni hatırla", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate600)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
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
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Divider(color = ArgepColors.Slate100, thickness = 1.dp)
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hesabınız yok mu?", style = MaterialTheme.typography.bodySmall, color = ArgepColors.Slate500)
                        TextButton(onClick = navigator.push(RegisterScreen())) { // Updated navigator call for consistency
                            Text("Kaydolun", color = ArgepColors.ExecutiveSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
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
                fontSize = 11.sp,
                letterSpacing = 1.sp
            ),
            color = ArgepColors.Slate700
        )
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = ArgepColors.Slate400, fontSize = 14.sp) },
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
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            singleLine = true
        )
    }
}
