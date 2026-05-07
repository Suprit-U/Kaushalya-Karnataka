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

    // Replace these with the actual keys from your Supabase dashboard or build config
    private const val SUPABASE_URL = "https://ytuosxjwpvsxwtnoppjm.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl0dW9zeGp3cHZzeHd0bm9wcGptIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgwNTUxNDEsImV4cCI6MjA5MzYzMTE0MX0.VCOBQytyvMCWentMlmPE3ie2cA-0E7R5h9ILTKFSqEU"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
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
