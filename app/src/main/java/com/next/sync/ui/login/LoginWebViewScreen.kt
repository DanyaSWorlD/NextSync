package com.next.sync.ui.login

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun loginWebViewScreen(loginViewModel: LoginViewModel = viewModel())
{
    // Adding a WebView inside AndroidView
    // with layout as full screen
    AndroidView(factory = {
        WebView(it).apply {
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }, update = {
        it.loadUrl(loginViewModel.serverAddress, mapOf("OCS-APIREQUEST" to "true"))
    })
}