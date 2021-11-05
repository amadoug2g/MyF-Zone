package com.myfzone_sport.myf_zone.util

import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.data.RemoteDataSource
import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.usecases.user.GetUserStatusUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Amadou on 02/11/2021, 16:41
 */

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideString() = "Testing string"
//    GetUserStatusUseCase

    @Singleton
    fun remoteDataSource() = RemoteDataSourceImpl()

    @Singleton
    fun repository(remoteDataSource: RemoteDataSource) = RepositoryImpl(remoteDataSource)

    @Singleton
    @Provides
    fun getUserStatusUseCase(repository: Repository) = GetUserStatusUseCase(repository)
}