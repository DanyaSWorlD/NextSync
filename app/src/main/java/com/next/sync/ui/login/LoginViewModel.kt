package com.next.sync.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.next.sync.ui.events.LoginEvents


data class LoginState(
    val isLoggedIn: Boolean = false
)
class LoginViewModel: ViewModel() {

    var loginState by mutableStateOf(LoginState())

    fun onEvent(event: LoginEvents) {
        when(event) {
            is LoginEvents.OnAddressConfirmed -> {
                // Handle address confirmation
                // val response = loginRepository.login(event.address)
                loginState = loginState.copy(isLoggedIn = true)
            }
        }
    }
}