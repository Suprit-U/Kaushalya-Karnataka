package com.kaushalyakarnataka.app.di

import android.content.Context
import androidx.room.Room
import com.kaushalyakarnataka.app.data.local.BookingDao
import com.kaushalyakarnataka.app.data.local.KaushalyaDatabase
import com.kaushalyakarnataka.app.data.local.ReviewDao
import com.kaushalyakarnataka.app.data.local.WorkerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KaushalyaDatabase =
        Room.databaseBuilder(context, KaushalyaDatabase::class.java, "kaushalya.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideWorkerDao(db: KaushalyaDatabase): WorkerDao = db.workerDao()
    @Provides fun provideBookingDao(db: KaushalyaDatabase): BookingDao = db.bookingDao()
    @Provides fun provideReviewDao(db: KaushalyaDatabase): ReviewDao = db.reviewDao()
}
