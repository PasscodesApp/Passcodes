package com.jeeldobariya.passcodes.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordsDao {

    @Insert
    suspend fun insertPassword(password: Password): Long

    @Query("SELECT * FROM passwords ORDER BY id DESC")
    fun getAllPasswords(): Flow<List<Password>>

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Int): Password?

    @Query("SELECT * FROM passwords WHERE username = :username AND domain = :domain")
    suspend fun getPasswordByUsernameAndDomain(username: String, domain: String): Password?

    @Update
    suspend fun updatePassword(password: Password): Int

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: Int): Int

    @Delete
    suspend fun deletePassword(password: Password): Int

    @Query("DELETE FROM passwords")
    suspend fun clearAllPasswordData(): Int
}
