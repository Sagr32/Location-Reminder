package com.udacity.project4.locationreminders.reminderslist


import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var reminderDataSource: ReminderDataSource
    private var appContext: Application = getApplicationContext()


    @Before
    fun setupFragmentDep() {
        stopKoin()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
        reminderDataSource = GlobalContext.get().koin.get()

        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }


    @Test
    fun clickFab_NavigateToSaveReminder() {

        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the first list item
        onView(withId(R.id.addReminderFAB))
            .perform(
                click()
            )


        // THEN - Verify that we navigate to the first detail screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun noReminders_showNoDataText() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN
        onView(withText("No Data")).check(matches(isDisplayed()))
    }

    @Test
    fun reminders_showReminderText() {
        // GIVEN - On the home screen
        val reminderDto = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 33.0,
            longitude = -33.0
        )
        runBlocking {
            reminderDataSource.saveReminder(reminderDto)
        }
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }


        //

        //
        onView(withText(reminderDto.title)).check(matches(isDisplayed()))
        onView(withText(reminderDto.description)).check(matches(isDisplayed()))
        onView(withText(reminderDto.location)).check(matches(isDisplayed()))
    }


}