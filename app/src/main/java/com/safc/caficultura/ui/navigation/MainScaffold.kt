package com.safc.caficultura.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.safc.caficultura.ui.theme.BackgroundGreen
import com.safc.caficultura.ui.theme.MainGreen

sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : NavItem(Rutas.Menu, Icons.Default.Home, "Inicio")
    object Produccion : NavItem(Rutas.Produccion, Icons.Default.Inventory2, "Producción")
    object Empleados : NavItem(Rutas.Empleados, Icons.Default.People, "Empleados")
    object Pagos : NavItem(Rutas.Pagos, Icons.Default.Payments, "Pagos")
    object Reportes : NavItem(Rutas.Reportes, Icons.Default.Description, "Reportes")
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    showBottomBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val items = listOf(
                    NavItem.Home,
                    NavItem.Empleados,
                    NavItem.Produccion,
                    NavItem.Pagos,
                    NavItem.Reportes
                )

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    color = BackgroundGreen.copy(alpha = 0.98f),
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(85.dp)
                    ) {
                        items.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        navController.navigate(item.route) {
                                            popUpTo(Rutas.Menu) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MainGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                item.icon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            tint = Color(0xFF003324).copy(alpha = 0.6f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MainGreen else Color(0xFF003324).copy(alpha = 0.6f),
                                        maxLines = 1
                                    )
                                },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}
