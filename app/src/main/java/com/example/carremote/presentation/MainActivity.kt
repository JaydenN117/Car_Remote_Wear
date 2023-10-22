package com.example.carremote.presentation

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint  // Dagger Hilt annotation to enable dependency injection for this activity.
class MainActivity : ComponentActivity() {

    // Dependency injection for the BluetoothAdapter.
    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content of this activity to the Navigation composable.
        setContent {
            Navigation(onBluetoothStateChanged = { showBluetoothDialog() })
        }
    }

    override fun onStart() {
        super.onStart()
        // Check and potentially prompt the user to enable Bluetooth when the activity starts.
        showBluetoothDialog()
    }

    // Variable to keep track of whether the Bluetooth dialog has been shown to avoid repeated prompts.
    private var isBluetoothDialogAlreadyShown = false

    // Function to prompt the user to enable Bluetooth if it's not already enabled.
    private fun showBluetoothDialog() {
        if (!isBluetoothDialogAlreadyShown) {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothIntentForResult.launch(enableBtIntent)
                isBluetoothDialogAlreadyShown = true
            }
        }
    }

    // Callback for the Bluetooth enable intent. It checks the user's response.
    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isBluetoothDialogAlreadyShown = false
            if (result.resultCode != Activity.RESULT_OK) {
                // If the user declined to enable Bluetooth, prompt again.
                showBluetoothDialog()
            }
        }
}
