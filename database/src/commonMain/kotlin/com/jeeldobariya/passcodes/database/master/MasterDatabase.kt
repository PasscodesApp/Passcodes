package com.jeeldobariya.passcodes.database.master

// import android.content.Context
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jeeldobariya.passcodes.database.master.migration.MIGRATION_1_2

@Database(
    entities = [PasswordEntity::class],
    version = 2,
    exportSchema = true
)
@ConstructedBy(MasterDatabaseConstructor::class)
abstract class MasterDatabase : RoomDatabase() {
    abstract val passwordsDao: PasswordsDao

    companion object {
        fun createNewDatabase(
            getDatabaseBuilder: () -> Builder<MasterDatabase>
        ): MasterDatabase {
            return getDatabaseBuilder()
                .setDriver(BundledSQLiteDriver())
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}

@Suppress("KotlinNoActualForExpect")
expect object MasterDatabaseConstructor : RoomDatabaseConstructor<MasterDatabase> {
    override fun initialize(): MasterDatabase
}
