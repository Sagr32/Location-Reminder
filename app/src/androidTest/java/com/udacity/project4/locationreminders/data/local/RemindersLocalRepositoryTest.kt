package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.DataBindingIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.annotations.NotNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository



    @Before
    fun setupDatabase() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        remindersLocalRepository = RemindersLocalRepository(database.reminderDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new task saved in the database.
        val reminderDto = ReminderDTO("title", "description", "location", 33.0, -33.0, "1")
        remindersLocalRepository.saveReminder(reminderDto)

        // WHEN  - Task retrieved by ID.
        val result = remindersLocalRepository.getReminder(reminderDto.id)

        // THEN - Same task is returned.
        assertThat(
            result is com.udacity.project4.locationreminders.data.dto.Result.Success,
            `is`(true)
        )
        result as Result.Success
        assertThat(result.data.title, `is`(reminderDto.title))
        assertThat(result.data.description, `is`(reminderDto.description))
        assertThat(result.data.location, `is`(reminderDto.location))
        assertThat(result.data.longitude, `is`(reminderDto.longitude))
        assertThat(result.data.latitude, `is`(reminderDto.latitude))
        assertThat(result.data.id, `is`(reminderDto.id))
    }


    @Test
    fun getReminderFromDbFailed() = runBlocking {
        val result = remindersLocalRepository.getReminder("0")
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

}