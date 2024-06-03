package com.next.sync.core.di

import android.content.Context
import android.net.Uri
import com.nextcloud.common.NextcloudClient
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.OwnCloudClientFactory
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory
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
    var client: NextcloudClient? = null
    var ownCloudClient: OwnCloudClient? = null

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
                Uri.parse(account.server), account.user, credentials, context
            )

            val serverUri = Uri.parse(account.server)
            val occ = OwnCloudClientFactory.createOwnCloudClient(serverUri, context, true)
            occ.credentials = OwnCloudCredentialsFactory.newBasicCredentials(
                account.user, account.password
            )
            occ.userId = account.user
            ownCloudClient = occ
        }
    }
}