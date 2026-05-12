package com.kaushalyakarnataka.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

/**
 * Hilt module providing the Supabase Client singleton.
 * Configured with the project URL and Anon API key.
 * Used exclusively for Storage (image uploads).
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = com.kaushalyakarnataka.app.BuildConfig.SUPABASE_URL,
            supabaseKey = com.kaushalyakarnataka.app.BuildConfig.SUPABASE_KEY
        ) {
            install(Storage)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient): Storage {
        return client.storage
    }
}
