package com.kaushalyakarnataka.app.di

import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.AuthRepositoryImpl
import com.kaushalyakarnataka.app.data.repository.BookingRepository
import com.kaushalyakarnataka.app.data.repository.BookingRepositoryImpl
import com.kaushalyakarnataka.app.data.repository.NotificationRepository
import com.kaushalyakarnataka.app.data.repository.NotificationRepositoryImpl
import com.kaushalyakarnataka.app.data.repository.ReviewRepository
import com.kaushalyakarnataka.app.data.repository.ReviewRepositoryImpl
import com.kaushalyakarnataka.app.data.repository.ServiceRepository
import com.kaushalyakarnataka.app.data.repository.ServiceRepositoryImpl
import com.kaushalyakarnataka.app.data.repository.StorageRepository
import com.kaushalyakarnataka.app.data.repository.StorageRepositoryImpl
import com.kaushalyakarnataka.app.data.repository.AiSummaryRepository
import com.kaushalyakarnataka.app.data.repository.AiSummaryRepositoryImpl
import com.kaushalyakarnataka.app.data.repository.WorkerRepository
import com.kaushalyakarnataka.app.data.repository.WorkerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindWorkerRepository(impl: WorkerRepositoryImpl): WorkerRepository

    @Binds @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds @Singleton
    abstract fun bindServiceRepository(impl: ServiceRepositoryImpl): ServiceRepository

    @Binds @Singleton
    abstract fun bindStorageRepository(impl: StorageRepositoryImpl): StorageRepository

    @Binds @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds @Singleton
    abstract fun bindAiSummaryRepository(impl: AiSummaryRepositoryImpl): AiSummaryRepository
}
