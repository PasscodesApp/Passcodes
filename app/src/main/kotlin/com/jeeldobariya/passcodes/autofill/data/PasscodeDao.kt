package com.jeeldobariya.passcodes.autofill.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasscodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(passcode: Passcode)

    @Update
    suspend fun update(passcode: Passcode)

    @Delete
    suspend fun delete(passcode: Passcode)

    @Query("SELECT * FROM passcodes ORDER BY name ASC")
    fun getAllPasscodes(): Flow<List<Passcode>>

    @Query("SELECT * FROM passcodes WHERE id = :id")
    fun getPasscode(id: Int): Flow<Passcode>
}
