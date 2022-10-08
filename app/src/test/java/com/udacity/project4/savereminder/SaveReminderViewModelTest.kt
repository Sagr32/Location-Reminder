package com.udacity.project4.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel


    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setupRemindersListViewModel() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }
    //validateAndSaveReminder
    //saveReminder
    //validateEnteredData

    @Test
    fun saveReminders_showingLoading() {
        val reminderDataItem = ReminderDataItem("title", "description", "location", 33.0, -33.0)

        mainCoroutineRule.pauseDispatcher()
        // Load the reminder in the view model.
        viewModel.saveReminder(reminderDataItem)

        // Then progress indicator is shown.
        Assert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()
        // Then progress indicator is hidden.
        Assert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
    }

    @Test
    fun saveReminderSuccessfully() {
        val reminderDataItem = ReminderDataItem("title", "description", "location", 33.0, -33.0)

        mainCoroutineRule.pauseDispatcher()
        // Load the reminder in the view model.
        viewModel.saveReminder(reminderDataItem)

        // Then progress indicator is shown.
        Assert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()
        // Then progress indicator is hidden.
        Assert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        Assert.assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))

    }


    @Test
    fun saveReminder_withoutLocation() {
        val reminderDataItem = ReminderDataItem("title", "description", "", 33.0, -33.0)
        val valid = viewModel.validateEnteredData(reminderDataItem)
        Assert.assertThat(valid, `is`(false))
        Assert.assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

    @Test
    fun saveReminder_withoutTitle() {
        val reminderDataItem = ReminderDataItem("", "description", "location", 33.0, -33.0)


        val valid = viewModel.validateEnteredData(reminderDataItem)
        Assert.assertThat(valid, `is`(false))
        Assert.assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

}
