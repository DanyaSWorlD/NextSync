package com.next.sync.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ServiceModule {
    @Provides
    @ViewModelScoped
    fun providesAccountService(): AccountService {
        return AccountService()
    }

}
