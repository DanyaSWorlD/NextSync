package com.next.sync.ui.login

import androidx.lifecycle.ViewModel
import com.next.sync.ui.events.LoginEvents

class LoginViewModel: ViewModel() {

    fun onEvent(event: LoginEvents) {
        when(event) {
            is LoginEvents.OnAddressConfirmed -> {
                // Handle address confirmation
            }
        }
    }
}