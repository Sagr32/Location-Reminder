package com.udacity.project4.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//TODO: I have modified FakeDataSource to mimics the real data source as requested
//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            if (shouldReturnError) {
                throw Exception("Error")
            }
            Result.Success(ArrayList(reminders))
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        return try {
            val reminder: ReminderDTO? = reminders?.find { it.id == id }
            if (shouldReturnError) {
                throw Exception("Error")
            } else if (reminder == null) {
                throw Exception("Reminder not found!")

            } else {
                Result.Success(reminder)
            }
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }

    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}
