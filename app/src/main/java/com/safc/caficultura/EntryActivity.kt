package com.safc.caficultura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safc.caficultura.ui.navigation.SafcApp
import com.safc.caficultura.ui.theme.MainGreen
import com.safc.caficultura.ui.theme.SAFCTheme

class EntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAFCTheme {
                var error by remember { mutableStateOf<String?>(null) }
                var deps by remember { mutableStateOf<AppDependencies?>(null) }

                if (deps == null && error == null) {
                    try {
                        deps = AppDependencies(applicationContext)
                    } catch (e: Throwable) {
                        error = (e::class.simpleName ?: "Error") + ": " + (e.message ?: "(sin mensaje)")
                    }
                }

                val d = deps
                val e = error

                if (d != null) {
                    SafcApp(deps = d)
                } else {
                    SplashScreen(error = e)
                }
            }
        }
    }
}

@Composable
fun SplashScreen(error: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo de la app en un recuadro blanco redondeado como en la imagen
            Surface(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp)),
                color = Color.White
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_icono_app),
                    contentDescription = "Logo SAFC",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SAFC",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (error != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Error al iniciar:\n$error",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
