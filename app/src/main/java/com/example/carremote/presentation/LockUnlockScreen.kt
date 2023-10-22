package com.exanple.carremote.com.example.carremote.presentation

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.carremote.LockUnlockButtons
import com.exanple.carremote.com.example.carremote.data.ConnectionState
import com.exanple.carremote.com.example.carremote.presentation.permissions.PermissionUtils
import com.exanple.carremote.com.example.carremote.presentation.permissions.SystemBroadcastReceiver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

// ... [other imports]

/**
 * The main screen for locking and unlocking functionality.
 *
 * @param onBluetoothStateChange A lambda function that gets called when the Bluetooth state changes.
 * @param viewModel The ViewModel responsible for managing the state and business logic.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LockUnlockScreen(
    onBluetoothStateChange: () -> Unit,
    viewModel: LockUnlockViewModel = hiltViewModel()
) {
    // BroadcastReceiver to listen to Bluetooth state changes.
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChange()
        }
    }

    // Remember the permissions required for the app to function.
    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
    val lifecycleOwner = LocalLifecycleOwner.current
    val bleConnectionState = viewModel.connectionState

    // Lifecycle observer to request permissions when the screen starts.
    // Also handles the connection state when the screen is stopped.
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                // When the screen starts, request permissions.
                permissionState.launchMultiplePermissionRequest()

                // If permissions are granted and BLE is disconnected, try reconnecting.
                if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {
                    viewModel.reconnect()
                }
            }
            if (event == Lifecycle.Event.ON_STOP) {
                // If the screen stops and BLE is connected, disconnect.
                if (bleConnectionState == ConnectionState.Connected) {
                    viewModel.disconnect()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // If permissions are granted, initialize the BLE connection.
    LaunchedEffect(key1 = permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.initializeConnection()
        }
    }

    // UI Layout for the LockUnlockScreen.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column (
            modifier = Modifier
                .fillMaxSize()
        ){
            if (bleConnectionState == ConnectionState.CurrentlyIntializing){
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    if (viewModel.initializingMessage != null) {
                        Text(
                            text = viewModel.initializingMessage!!,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (!permissionState.allPermissionsGranted) {
                        Text(
                            text = "Go to the app settings and enable the permissions",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(10.dp)
                        )
                    } else if (viewModel.errorMessage != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = viewModel.errorMessage!!)

                            Button(
                                onClick = {
                                    if (permissionState.allPermissionsGranted) {
                                        viewModel.initializeConnection()

                                    }
                                }
                            ) {
                                Text(text = "Retry")
                            }
                        }
                    }
                }
                    }else if (bleConnectionState == ConnectionState.Connected){
                        Log.d("BLE", "Connected")
                        LockUnlockButtons(lockOnClick = {viewModel.lockCar()}, unlockOnClick = {viewModel.unlockCar()})

                    }else if (bleConnectionState == ConnectionState.Disconnected){
                        Button(
                            onClick = {
                                viewModel.initializeConnection()
                            }
                        ){
                            Text(text = "Restart Connection")
                        }

                    }
            }
        }
    }

/**
 * Preview function to visualize the `LockUnlockButtons` Composable in the Android Studio preview pane.
 */
@Composable
@Preview
fun LockUnlockScreenPreview() {
    LockUnlockButtons(lockOnClick = {}, unlockOnClick = {})
}
