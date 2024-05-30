package com.next.sync.core.di

import androidx.datastore.preferences.core.longPreferencesKey
import com.next.sync.core.db.ObjectBox.store
import com.next.sync.core.db.data.AccountEntity
import com.next.sync.core.db.data.AccountEntity_
import io.objectbox.kotlin.and
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountService @Inject constructor(
    private var dataStore: PreferenceDataStoreHelper
) {
    private val currentAccountIdKey = longPreferencesKey("CURRENT_ACCOUNT_ID")

    suspend fun getCurrentAccountId(): Flow<Long> {
        return dataStore.getPreference(currentAccountIdKey, -1)
    }

    suspend fun setCurrentAccountId(preference: Long) {
        dataStore.putPreference(currentAccountIdKey, preference)
    }

    fun saveAccountData(account: AccountEntity): Long {
        val accountBox = store.boxFor(AccountEntity::class)

        val query = accountBox
            .query(AccountEntity_.user.equal(account.user) and AccountEntity_.user.equal(account.server))
            .build()

        val results = query.find()
        query.close()

        if (results.any())
            return results.first().id

        accountBox.put(account)

        return account.id
    }

    fun getAccountData(accountId: Long): AccountEntity? {
        val accountBox = store.boxFor(AccountEntity::class)

        val query = accountBox
            .query(AccountEntity_.id.equal(accountId))
            .build()

        val results = query.find()
        query.close()

        if(results.any())
            return results.first()

        return null
    }
}