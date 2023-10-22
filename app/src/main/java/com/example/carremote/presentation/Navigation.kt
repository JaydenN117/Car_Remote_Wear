package com.example.carremote.presentation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exanple.carremote.com.example.carremote.presentation.LockUnlockScreen

@Composable
fun Navigation(
    onBluetoothStateChanged:()-> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.StartScreen.route) {
        composable(route = Screen.StartScreen.route) {
//            StartScreen(navController = navController)
            LockUnlockScreen(onBluetoothStateChange = onBluetoothStateChanged)
        }
        composable(route = Screen.LockUnlockScreen.route) {
            LockUnlockScreen(onBluetoothStateChange = onBluetoothStateChanged)

        }
    }
}

sealed class Screen(val route: String) {
    object StartScreen : Screen("start_screen")
    object LockUnlockScreen : Screen("lock_unlock_buttons")
}