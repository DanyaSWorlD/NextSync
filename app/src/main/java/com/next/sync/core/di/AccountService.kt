package com.next.sync.core.di

import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountService @Inject constructor(
    //private var dataStore: PreferenceDataStoreHelper
) {
    private val currentAccountIdKey = intPreferencesKey("CURRENT_ACCOUNT_ID")

    suspend fun getCurrentAccountId(): Flow<Int> {
        //return dataStore.getPreference(currentAccountIdKey, -1)
        return flow { emit(3) }
    }


}