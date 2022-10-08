package com.udacity.project4.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupRemindersListViewModel() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }


    @Test
    fun loadReminders_loading() {

        mainCoroutineRule.pauseDispatcher()
        // Load the reminder in the view model.
        viewModel.loadReminders()

        // Then progress indicator is shown.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        // Then progress indicator is hidden.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


    @Test
    fun loadRemindersWhenAvailable_ToDisplay() = runBlockingTest {
        val reminderDTO = ReminderDTO("title", "description", "location", 33.0, -33.0)
        fakeDataSource.saveReminder(reminderDTO)

        //
        mainCoroutineRule.pauseDispatcher()
        // Load the reminder in the view model.
        viewModel.loadReminders()

        // Then progress indicator is shown.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        // Then progress indicator is hidden.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.remindersList.getOrAwaitValue()).isNotEmpty()
    }

    @Test
    fun loadReminderWhenTasksAreUnavailable_callErrorToDisplay() = runBlockingTest {
        fakeDataSource.setShouldReturnError(true)
        //
        mainCoroutineRule.pauseDispatcher()


        // Load the reminder in the view model.
        viewModel.loadReminders()
//        result as Result.Error

        // Then progress indicator is shown.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        // Then progress indicator is hidden.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.showNoData.getOrAwaitValue()).isTrue()
        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isNotEmpty()
        MatcherAssert.assertThat(
            (fakeDataSource.getReminders() as Result.Error).message,
            `is`("Error")
        )

    }


}