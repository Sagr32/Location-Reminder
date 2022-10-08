package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.app.PendingIntent.getActivity
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.R
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDataSource: ReminderDataSource
    private var appContext: Application = ApplicationProvider.getApplicationContext()
    private lateinit var viewModel: SaveReminderViewModel


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
        viewModel = GlobalContext.get().koin.get()
        runBlocking {
            reminderDataSource.deleteAllReminders()

        }
    }


    @Test
    fun saveFailed_NoTitle() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
//        R.string.err_enter_title
        //


        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        onView(withText(R.string.err_enter_title)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(appContext)?.getWindow()?.getDecorView()
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )
        Thread.sleep(3000L)
        //

    }

    @Test
    fun saveSuccess_withToast() {
        viewModel.latitude.postValue(33.0)
        viewModel.longitude.postValue(-33.0)
        viewModel.selectedPOI.postValue(PointOfInterest(LatLng(33.0, -33.0), "1", "New Place"))

        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
//        R.string.err_enter_title
        //
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("Title"))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Description"))
        Espresso.closeSoftKeyboard()



        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))

//        onView(withText(R.string.reminder_saved)).inRoot(
//            withDecorView(
//                not(
//                    `is`(
//                        getActivity(appContext)?.window?.decorView
//                    )
//                )
//            )
//        ).check(matches(isDisplayed()))

    }


}