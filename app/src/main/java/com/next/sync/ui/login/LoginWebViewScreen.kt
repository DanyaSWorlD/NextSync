package com.next.sync.ui.login

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.next.sync.ui.components.bottom_bar.BottomBarScreen

@Composable
fun LoginWebViewScreen(
    loginState: LoginState,
    navigate: (String) -> Unit,
    loginVm: LoginViewModel
) {
    // Adding a WebView inside AndroidView
    // with layout as full screen
    AndroidView(factory = {
        WebView(it).apply {
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.settings.javaScriptEnabled = true
            this.settings.userAgentString = "NextSync"
            this.webViewClient = CustomWebViewClient(loginState) { state ->
                loginVm.logIn(state)
                navigate.invoke(BottomBarScreen.Home.route)
            }
        }
    }, update = {
        it.loadUrl(
            "https://${loginState.serverAddress}/index.php/login/flow",
            mapOf("OCS-APIREQUEST" to "true")
        )
    })
}

class CustomWebViewClient(
    private var loginState: LoginState,
    private val loggedInCallback: (LoginState) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if (url == null) return false

        if (url.startsWith(loginState.serverAddress)) {
            view?.loadUrl(url)
            return true
        }

        val prefix = "nc://login/"
        if (url.startsWith(prefix)) {
            val data = url.removePrefix(prefix)

            for (pair in data.split('&')) {
                val field = pair.split(':')

                if (field[0] == "server")
                    loginState = loginState.copy(serverAddress = field[1] + ":" + field[2])
                if (field[0] == "user")
                    loginState = loginState.copy(user = field[1])
                if (field[0] == "password")
                    loginState = loginState.copy(password = field[1])
            }

            loginState = loginState.copy(isLoggedIn = true)
            loggedInCallback.invoke(loginState)
            return true
        }

        return false
    }
}