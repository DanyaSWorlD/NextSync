package com.next.sync.core.di

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nextcloud.common.NextcloudClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NextcloudClientHelper @Inject constructor(
    @ApplicationContext val context: Context, private val accountService: AccountService
) {
    var client: NextcloudClient? by mutableStateOf(null)

    init {
        loadService()
    }

    fun loadService() = runBlocking {
        launch {
            val id = accountService.getCurrentAccountId()
            if (id.toInt() == -1) {
                return@launch
            }

            val account = accountService.getAccountData(id)

            val credentials: String = Credentials.basic(account!!.user, account.password)
            client = NextcloudClient(
                Uri.parse(account.server),
                account.user,
                credentials,
                context
            )
        }
    }
}