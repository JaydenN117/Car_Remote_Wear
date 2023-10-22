package com.exanple.carremote.com.example.carremote.presentation

import android.provider.Contacts.SettingsColumns.KEY
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carremote.util.Resource
import com.exanple.carremote.com.example.carremote.data.ConnectionState
import com.exanple.carremote.com.example.carremote.data.FeedbackReceiveManager
import com.exanple.carremote.com.example.carremote.data.LockState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing lock and unlock functionalities for a car.
 * This ViewModel interacts with the [FeedbackReceiveManager] to obtain connection and lock state updates.
 */
@HiltViewModel
class LockUnlockViewModel @Inject constructor(
    // Dependency injection of FeedbackReceiveManager
    private val feedbackReceiveManager: FeedbackReceiveManager
) : ViewModel() {

    // Mutable state for displaying an initializing message
    var initializingMessage by mutableStateOf<String?>(null)
        private set

    // Mutable state for displaying error messages
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Mutable state for representing the current lock state of the car
    var lock by mutableStateOf(LockState.Unknown)
        private set

    // Mutable state for representing the current connection state with the car
    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)
        private set


    private fun subscribeToChanges() {
        viewModelScope.launch {
            feedbackReceiveManager.data.collect { result ->
                when (result) {
                    // Handle successful state updates
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
//                        lock = result.data.lockState
                    }

                    // Handle loading state updates
                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyIntializing
                    }

                    // Handle error state updates
                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }

    /**
     * Disconnects from the car.
     */
    fun disconnect() {
        feedbackReceiveManager.disconnect()
    }

    /**
     * Attempts to reconnect to the car.
     */
    fun reconnect() {
        feedbackReceiveManager.reconnect()
    }

    /**
     * Initializes the connection to the car and subscribes to state updates.
     */
    fun initializeConnection() {
        errorMessage = null
        subscribeToChanges()
        feedbackReceiveManager.startReceiving()
        connectionState = ConnectionState.CurrentlyIntializing //gpt
    }

    /**
     * Cleanup logic when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        feedbackReceiveManager.closeConnection()
    }
    fun lockCar() {
        feedbackReceiveManager.writeLockUnlockValue((KEY+"1").toByteArray()  )
    }

    /**
     * Writes the unlock value to the car's BLE characteristic.
     */
    fun unlockCar() {
        feedbackReceiveManager.writeLockUnlockValue((KEY+"2").toByteArray())
    }

}
