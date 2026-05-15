package com.madhusiri.app.di

import com.madhusiri.app.data.repository.HiveRepositoryImpl
import com.madhusiri.app.domain.repository.HiveRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindHiveRepository(
        hiveRepositoryImpl: HiveRepositoryImpl
    ): HiveRepository
}
