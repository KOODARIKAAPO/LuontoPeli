package com.example.luontopeli.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow

@Dao
interface NatureSpotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spot: NatureSpot): Long

    @Query("SELECT * FROM nature_spots ORDER BY timestamp DESC")
    fun getAllSpots(): Flow<List<NatureSpot>>

    @Query("SELECT * FROM nature_spots WHERE latitude != 0.0 AND longitude != 0.0 ORDER BY timestamp DESC")
    fun getSpotsWithLocation(): Flow<List<NatureSpot>>

    @Query("SELECT * FROM nature_spots WHERE synced = 0")
    suspend fun getUnsyncedSpots(): List<NatureSpot>

    @Query("UPDATE nature_spots SET synced = 1, imageFirebaseUrl = :url WHERE id = :id")
    suspend fun markSynced(id: String, url: String)

    @Delete
    suspend fun delete(spot: NatureSpot)
}