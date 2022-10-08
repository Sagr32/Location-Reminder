package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent

class MyCustomReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("CustomReceiver", "onReceive")

        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)

        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
//                val errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.errorCode)
                Log.i("CustomReceiver", "Error")

                // display error
            } else {
                geofencingEvent.triggeringGeofences?.forEach {
                    val geofence = it.requestId
                    // display notification
                    Log.e("CustomReceiver", "Id")

                }
            }
        }
    }
}