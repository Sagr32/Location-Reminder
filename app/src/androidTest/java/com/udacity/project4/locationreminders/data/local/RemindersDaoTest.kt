package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase


    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun insertDataAndCheckIt() = runBlockingTest {
        // GIVEN - Insert a task.
        val reminderDTO = ReminderDTO(
            "title", "description", "Location",
            33.0, -33.0
        )
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminders()[0]

        // THEN - The loaded data contains the expected values.
        Assert.assertNotNull(loaded as ReminderDTO)
        assertThat(loaded.title, `is`(reminderDTO.title))
        assertThat(loaded.description, `is`(reminderDTO.description))
        assertThat(loaded.location, `is`(reminderDTO.location))
        assertThat(loaded.latitude, `is`(reminderDTO.latitude))
        assertThat(loaded.longitude, `is`(reminderDTO.longitude))
    }



}