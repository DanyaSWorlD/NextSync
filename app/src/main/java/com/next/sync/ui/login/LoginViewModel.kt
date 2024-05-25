package com.next.sync.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.next.sync.ui.events.LoginEvents


//Can be renamed
data class LoginState(
    val isLoggedIn: Boolean = false,
    val serverAddress: String = "",
    var loginFlow: String = "$serverAddress/index.php/login/flow"
)
class LoginViewModel : ViewModel() {
    var loginState by mutableStateOf(LoginState())

    fun onEvent(event: LoginEvents) {
        when (event) {
            is LoginEvents.OnAddressConfirmed -> {
                // Sample for login
                // var response = loginService.login(loginState.loginFlow)
                // if(response == "success"){
                //     loginState = loginState.copy(isLoggedIn = true)
                // }
            }

            is LoginEvents.UpdateServerAddress -> {
                loginState = loginState.copy(serverAddress = event.address)
            }
        }
    }
}