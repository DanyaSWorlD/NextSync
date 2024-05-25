package com.next.sync.ui.events

sealed class LoginEvents {
    data class OnAddressConfirmed(val address: String) : LoginEvents()
}