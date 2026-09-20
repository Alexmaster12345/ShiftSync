package com.example.shiftsync

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

// ShiftEntry itself is the Room entity — it was already the single source of truth for a shift
// record, so there's no separate "domain model vs. persistence model" split to maintain here.
@Entity(tableName = "shift_entries")
data class ShiftEntry(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val startedAtMillis: Long,
    val shiftType: ShiftType,
    val durationMinutes: Long,
    val unpaidBreakMinutes: Int,
    val hourlyRate: Double,
    val estimatedPay: Double,
    val notes: String = ""
)

class ShiftTypeConverters {
    @TypeConverter
    fun fromShiftType(type: ShiftType): String = type.name

    @TypeConverter
    fun toShiftType(name: String): ShiftType =
        runCatching { ShiftType.valueOf(name) }.getOrDefault(ShiftType.REGULAR)
}

@Dao
interface ShiftEntryDao {
    @Query("SELECT * FROM shift_entries ORDER BY startedAtMillis DESC")
    fun observeAll(): Flow<List<ShiftEntry>>

    @Query("SELECT * FROM shift_entries ORDER BY startedAtMillis DESC")
    suspend fun getAllOnce(): List<ShiftEntry>

    @Query("SELECT COUNT(*) FROM shift_entries")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ShiftEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ShiftEntry>)

    @Delete
    suspend fun delete(entry: ShiftEntry)

    @Query("DELETE FROM shift_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM shift_entries")
    suspend fun clearAll()
}

@Database(entities = [ShiftEntry::class], version = 1, exportSchema = false)
@TypeConverters(ShiftTypeConverters::class)
abstract class ShiftDatabase : RoomDatabase() {
    abstract fun shiftEntryDao(): ShiftEntryDao

    companion object {
        @Volatile private var INSTANCE: ShiftDatabase? = null

        fun getInstance(context: Context): ShiftDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ShiftDatabase::class.java,
                    "shift_sync.db"
                ).build().also { INSTANCE = it }
            }
    }
}

/**
 * Single access point for shift entry persistence. Wraps the Room DAO and, on first use, imports
 * any entries left over from the old delimited-string-in-SharedPreferences storage — this app's
 * only prior source of shift data — so upgrading users don't lose history.
 */
object ShiftRepository {
    private lateinit var dao: ShiftEntryDao
    private var migrationAttempted = false

    fun init(context: Context) {
        if (!::dao.isInitialized) {
            dao = ShiftDatabase.getInstance(context).shiftEntryDao()
        }
    }

    suspend fun migrateLegacyEntriesIfNeeded(prefs: SharedPreferences) {
        if (migrationAttempted) return
        migrationAttempted = true
        if (dao.count() > 0) return
        val legacy = parseLegacyEntries(prefs.getString(KEY_ENTRIES, null))
        if (legacy.isNotEmpty()) {
            dao.insertAll(legacy)
        }
        prefs.edit().remove(KEY_ENTRIES).apply()
    }

    fun observeAll(): Flow<List<ShiftEntry>> = dao.observeAll()
    suspend fun getAllOnce(): List<ShiftEntry> = dao.getAllOnce()
    suspend fun add(entry: ShiftEntry) = dao.insert(entry)
    suspend fun update(entry: ShiftEntry) = dao.insert(entry)
    suspend fun delete(id: String) = dao.deleteById(id)
    suspend fun clearAll() = dao.clearAll()

    /** Replaces the entire table — used by full-backup import and by the overtime-rules
     * "apply to all shifts" re-split, both of which compute a brand new entry list. */
    suspend fun replaceAll(entries: List<ShiftEntry>) {
        dao.clearAll()
        dao.insertAll(entries)
    }
}
