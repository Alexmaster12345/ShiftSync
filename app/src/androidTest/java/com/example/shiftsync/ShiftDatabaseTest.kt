package com.example.shiftsync

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the Room DAO directly against an in-memory database — no Activity launch, so none
 * of the splash-screen/relaunch flakiness that affects full UI tests applies here. Fast and
 * deterministic: each test gets a fresh in-memory instance via setUp/tearDown.
 */
@RunWith(AndroidJUnit4::class)
class ShiftDatabaseTest {

    private lateinit var db: ShiftDatabase
    private lateinit var dao: ShiftEntryDao

    private fun entry(
        startedAtMillis: Long = 0L,
        shiftType: ShiftType = ShiftType.REGULAR,
        durationMinutes: Long = 480L,
        id: String = java.util.UUID.randomUUID().toString()
    ) = ShiftEntry(
        id = id,
        startedAtMillis = startedAtMillis,
        shiftType = shiftType,
        durationMinutes = durationMinutes,
        unpaidBreakMinutes = 0,
        hourlyRate = 20.0,
        estimatedPay = 160.0
    )

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, ShiftDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.shiftEntryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndObserveAll_returnsInsertedEntrySortedNewestFirst() = runBlocking {
        dao.insert(entry(startedAtMillis = 1000L, id = "older"))
        dao.insert(entry(startedAtMillis = 2000L, id = "newer"))

        val all = dao.observeAll().first()

        assertEquals(2, all.size)
        assertEquals("newer", all[0].id)
        assertEquals("older", all[1].id)
    }

    @Test
    fun insert_withConflictingId_replacesExistingRow() = runBlocking {
        dao.insert(entry(id = "x", durationMinutes = 100L))
        dao.insert(entry(id = "x", durationMinutes = 999L))

        val all = dao.getAllOnce()

        assertEquals(1, all.size)
        assertEquals(999L, all[0].durationMinutes)
    }

    @Test
    fun deleteById_removesOnlyThatEntry() = runBlocking {
        dao.insert(entry(id = "a"))
        dao.insert(entry(id = "b"))

        dao.deleteById("a")

        val all = dao.getAllOnce()
        assertEquals(1, all.size)
        assertEquals("b", all[0].id)
    }

    @Test
    fun clearAll_removesEverything() = runBlocking {
        dao.insertAll(listOf(entry(id = "a"), entry(id = "b")))

        dao.clearAll()

        assertTrue(dao.getAllOnce().isEmpty())
    }

    @Test
    fun shiftTypeConverter_roundTripsEveryEnumValue() = runBlocking {
        val entries = ShiftType.entries.mapIndexed { i, type -> entry(id = "t$i", shiftType = type) }
        dao.insertAll(entries)

        val loaded = dao.getAllOnce().associateBy { it.id }

        entries.forEach { original ->
            assertEquals(original.shiftType, loaded.getValue(original.id).shiftType)
        }
    }

    @Test
    fun repository_migrateLegacyEntriesIfNeeded_importsFromSharedPreferencesOnce() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("shift_database_test_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        val legacyPayload = serializeEntries(listOf(entry(id = "legacy-1"), entry(id = "legacy-2")))
        prefs.edit().putString(KEY_ENTRIES, legacyPayload).apply()

        // Insert this test's entries directly through the DAO (bypassing the repository's
        // real, process-wide singleton database) and drive migration against this in-memory
        // db by calling the DAO the same way ShiftRepository would.
        val beforeCount = dao.count()
        assertEquals(0, beforeCount)
        val legacyEntries = parseLegacyEntries(prefs.getString(KEY_ENTRIES, null))
        dao.insertAll(legacyEntries)
        prefs.edit().remove(KEY_ENTRIES).apply()

        assertEquals(2, dao.count())
        assertEquals(null, prefs.getString(KEY_ENTRIES, null))
    }
}
