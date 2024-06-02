package com.next.sync.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.next.sync.R
import com.next.sync.ui.Routes
import com.next.sync.ui.events.LoginEvents
import com.next.sync.ui.theme.AppTheme

@Composable
fun LoginScreen(
    loginEvents: (LoginEvents) -> Unit,
    loginState: LoginState,
    navigate: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }


    Box(Modifier.fillMaxSize())
    {
        Image(
            painter = painterResource(id = R.drawable.kamil_porembinski_clouds),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                Text(
                    text = "LOGO",
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Light
                )
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 40.dp)
            ) {
                OutlinedTextField(
                    value = loginState.serverAddress,
                    label = { Text(text = "Server address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    onValueChange = { loginEvents(LoginEvents.UpdateServerAddress(it)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedLabelColor = Color.White
                    ),

                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )

                IconButton(
                    onClick = { navigate.invoke(Routes.LoginWebViewScreen.name) },//loginEvents(LoginEvents.OnAddressConfirmed(loginState.serverAddress)) },
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(top = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_forward_24),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    AppTheme(false) {
        LoginScreen(loginEvents = {}, loginState = LoginState(), navigate = {})
    }
}