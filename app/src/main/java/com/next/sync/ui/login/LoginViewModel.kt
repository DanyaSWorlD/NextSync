package com.next.sync.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.next.sync.core.db.data.AccountEntity
import com.next.sync.core.di.AccountModule
import com.next.sync.ui.events.LoginEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


//Can be renamed
data class LoginState(
    val isLoggedIn: Boolean = false,
    val serverAddress: String = "",
    val user: String = "",
    val password: String = ""
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountModule: AccountModule
) : ViewModel() {

    var loginState by mutableStateOf(LoginState())

    init {
        val id = getId()

        loginState = loginState.copy(
            isLoggedIn = id > -1
        )

        if (loginState.isLoggedIn) viewModelScope.launch { readCurrentAccount(id) }
    }

    private fun getId(): Long = runBlocking {
        return@runBlocking accountModule.getCurrentAccountId()
    }

    private fun setId(id: Long) = runBlocking {
        accountModule.setCurrentAccountId(id)
    }

    private fun readCurrentAccount(id: Long) {
        val account = accountModule.getAccountData(id) ?: return

        loginState = loginState.copy(
            serverAddress = account.server,
            user = account.user,
            password = account.password,
        )
    }

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

    fun logIn(state: LoginState) {
        loginState = state
        val id = accountModule.saveAccountData(
            AccountEntity(
                user = loginState.user,
                password = loginState.password,
                server = loginState.serverAddress
            )
        )
        setId(id)
    }
}