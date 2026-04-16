package com.example.luontopeli.di

import android.content.Context
import androidx.room.Room
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.dao.WalkSessionDao
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.remote.firebase.StorageManager
import com.example.luontopeli.data.repository.NatureSpotRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "luontopeli_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNatureSpotDao(
        database: AppDatabase
    ): NatureSpotDao {
        return database.natureSpotDao()
    }

    @Provides
    @Singleton
    fun provideWalkSessionDao(
        database: AppDatabase
    ): WalkSessionDao {
        return database.walkSessionDao()
    }

    @Provides
    @Singleton
    fun provideFirestoreManager(): FirestoreManager {
        return FirestoreManager()
    }

    @Provides
    @Singleton
    fun provideAuthManager(): AuthManager {
        return AuthManager()
    }

    @Provides
    @Singleton
    fun provideStorageManager(): StorageManager {
        return StorageManager()
    }

    @Provides
    @Singleton
    fun provideNatureSpotRepository(
        dao: NatureSpotDao,
        firestoreManager: FirestoreManager,
        authManager: AuthManager,
        storageManager: StorageManager
    ): NatureSpotRepository {
        return NatureSpotRepository(
            dao = dao,
            firestoreManager = firestoreManager,
            authManager = authManager,
            storageManager = storageManager
        )
    }
}