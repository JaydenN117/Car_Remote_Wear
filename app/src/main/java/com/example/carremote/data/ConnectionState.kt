package com.exanple.carremote.com.example.carremote.data

sealed interface ConnectionState{
    object Connected:ConnectionState
    object Disconnected:ConnectionState
    object Uninitialized:ConnectionState
    object CurrentlyIntializing:ConnectionState
}