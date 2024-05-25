package com.next.sync.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.next.sync.ui.events.LoginEvents


class LoginViewModel : ViewModel() {
    var serverAddress by mutableStateOf("")
    val loginFlow : String
        get() = serverAddress + "/index.php/login/flow"

    fun changeServerAddress(value: String) {
        serverAddress = value
    }



    fun onEvent(event: LoginEvents) {
        when (event) {
            is LoginEvents.OnAddressConfirmed -> {
                // Handle address confirmation
            }
        }
    }
}