package com.jeeldobariya.passcodes.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordsDao {

    @Insert
    suspend fun insertPassword(password: PasswordEntity): Long

    @Query("SELECT * FROM passwords ORDER BY id DESC")
    fun getAllPasswords(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Int): PasswordEntity?

    @Query("SELECT * FROM passwords WHERE username = :username AND domain = :domain")
    suspend fun getPasswordByUsernameAndDomain(username: String, domain: String): PasswordEntity?

    @Update
    suspend fun updatePassword(password: PasswordEntity): Int

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: Int): Int

    @Delete
    suspend fun deletePassword(password: PasswordEntity): Int

    @Query("DELETE FROM passwords")
    suspend fun clearAllPasswordData(): Int
}
