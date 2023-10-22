package com.exanple.carremote.com.example.carremote.data

import com.example.carremote.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface FeedbackReceiveManager {

    /**
     * A shared flow that emits [FeedbackResult] wrapped in a [Resource].
     * [Resource] is a utility class that represents data states like success, error, and loading.
     */
    val data: MutableSharedFlow<Resource<FeedbackResult>>

    /**
     * Reconnects to the remote device.
     *
     * Implement this method to handle the logic when a reconnection is required.
     */
    fun reconnect()

    /**
     * Disconnects from the remote device.
     *
     * Implement this method to handle the disconnection process.
     */
    fun disconnect()

    /**
     * Initiates the process of receiving feedback data.
     *
     * Implement this method to start listening for feedback from the remote device.
     */
    fun startReceiving()

    /**
     * Closes the current connection to the remote device.
     *
     * Implement this method to gracefully close the connection and free up any resources.
     */
    fun closeConnection()

    fun writeLockUnlockValue(value: ByteArray)

}
