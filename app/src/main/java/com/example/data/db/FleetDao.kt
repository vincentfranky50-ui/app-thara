package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {
    @Query("SELECT * FROM vehicles ORDER BY name ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVehicles(vehicles: List<VehicleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicle(id: String)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("UPDATE alerts SET acknowledged = 1 WHERE id = :alertId")
    suspend fun acknowledgeAlert(alertId: String)

    @Query("UPDATE alerts SET acknowledged = 1")
    suspend fun acknowledgeAllAlerts()

    @Query("DELETE FROM alerts")
    suspend fun deleteAllAlerts()

    @Query("SELECT * FROM geofences ORDER BY name ASC")
    fun getAllGeofences(): Flow<List<GeofenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofences(zones: List<GeofenceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofence(zone: GeofenceEntity)

    @Query("DELETE FROM geofences WHERE id = :id")
    suspend fun deleteGeofence(id: String)
}
