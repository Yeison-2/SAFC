package com.safc.caficultura.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.R
import com.safc.caficultura.ui.navigation.Rutas
import com.safc.caficultura.ui.theme.BackgroundGreen
import com.safc.caficultura.ui.theme.DarkGreenText
import com.safc.caficultura.ui.theme.LightGreenButton
import com.safc.caficultura.ui.theme.MainGreen
import com.safc.caficultura.ui.viewmodel.LoginViewModel
import com.safc.caficultura.ui.viewmodel.SafcViewModelFactory

@Composable
fun LoginRoute(
    navController: NavHostController,
    deps: AppDependencies,
    factory: SafcViewModelFactory
) {
    val vm: LoginViewModel = viewModel(factory = factory)
    val ui by vm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(ui.navegarAlMenu) {
        if (ui.navegarAlMenu) {
            navController.navigate(Rutas.Menu) {
                popUpTo(Rutas.Login) { inclusive = true }
            }
            vm.consumirNavegacion()
        }
    }

    LoginScreen(
        cargando = ui.cargando,
        mensajeError = ui.mensajeError,
        onIngresar = { usuario, clave -> vm.iniciarSesion(usuario, clave) }
    )
}

@Composable
private fun LoginScreen(
    cargando: Boolean,
    mensajeError: String?,
    onIngresar: (usuario: String, contrasena: String) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarLogin by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGreen)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo en recuadro blanco como la imagen
        Surface(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(40.dp)),
            color = Color.White
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_principal),
                contentDescription = "Logo SAFC",
                modifier = Modifier.fillMaxSize().padding(10.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SAFC",
            color = MainGreen,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Sistema de Administración de Fincas Cafeteras",
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (!mostrarLogin) {
            // Botones principales (Imagen 2)
            Button(
                onClick = { mostrarLogin = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainGreen),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Iniciar Sesión", color = Color(0xFF003324), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Navegar a registro */ },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGreenButton),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Registrarse", color = Color(0xFF003324), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /* Olvidé contraseña */ }) {
                Text("¿Olvidaste tu contraseña?", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            // Formulario de login
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuario") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainGreen,
                    unfocusedBorderColor = MainGreen.copy(alpha = 0.5f)
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainGreen,
                    unfocusedBorderColor = MainGreen.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            mensajeError?.let {
                Spacer(Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onIngresar(usuario, contrasena) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !cargando,
                colors = ButtonDefaults.buttonColors(containerColor = MainGreen)
            ) {
                if (cargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Entrar", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = { mostrarLogin = false }) {
                Text("Volver", color = DarkGreenText)
            }
        }
    }
}
