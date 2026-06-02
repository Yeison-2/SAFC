package com.safc.caficultura.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.ui.screens.empleados.EmpleadosRoute
import com.safc.caficultura.ui.screens.login.LoginRoute
import com.safc.caficultura.ui.screens.menu.MenuRoute
import com.safc.caficultura.ui.screens.pagos.PagosRoute
import com.safc.caficultura.ui.screens.produccion.ProduccionRoute
import com.safc.caficultura.ui.screens.reportes.ReportesRoute
import com.safc.caficultura.ui.theme.SAFCTheme
import com.safc.caficultura.ui.viewmodel.SafcViewModelFactory

@Composable
fun SafcApp(deps: AppDependencies) {
    SAFCTheme {
        val navController = rememberNavController()
        val factory = remember(deps) { SafcViewModelFactory(deps) }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val mostrarScaffold = currentRoute != null && currentRoute != Rutas.Login

        MainScaffold(
            navController = navController,
            showBottomBar = mostrarScaffold
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Rutas.Login,
                modifier = Modifier.padding(
                    bottom = if (mostrarScaffold) innerPadding.calculateBottomPadding() else 0.dp
                )
            ) {
                composable(Rutas.Login) {
                    LoginRoute(navController = navController, deps = deps, factory = factory)
                }
                composable(Rutas.Menu) {
                    MenuRoute(navController = navController, deps = deps, factory = factory)
                }
                composable(Rutas.Empleados) {
                    EmpleadosRoute(navController = navController, deps = deps, factory = factory)
                }
                composable(Rutas.Produccion) {
                    ProduccionRoute(navController = navController, deps = deps, factory = factory)
                }
                composable(Rutas.Pagos) {
                    PagosRoute(navController = navController, deps = deps, factory = factory)
                }
                composable(Rutas.Reportes) {
                    ReportesRoute(navController = navController, deps = deps, factory = factory)
                }
            }
        }
    }
}
