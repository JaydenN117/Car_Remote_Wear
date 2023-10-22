package com.exanple.carremote.com.example.carremote.data

data class FeedbackResult(
//    val lockState: LockState,
    val connectionState: ConnectionState
)

enum class LockState {
    Locked,
    Unlocked,
    Unknown
}