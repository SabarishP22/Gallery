package com.example.videoplayer.domain.model

import android.content.IntentSender

sealed class TrashDeleteRequest {
    data class SystemConfirmation(val intentSender: IntentSender) : TrashDeleteRequest()
    data object Completed : TrashDeleteRequest()
}
