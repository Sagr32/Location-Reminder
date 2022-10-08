package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


//import androidx.test.core.app.ApplicationProvider.getApplicationContext
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.BuildConfig
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_INDEX = 0
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    private val TAG = "SelectLocationFragment"
    private lateinit var mMap: GoogleMap
    val viewModel: SaveReminderViewModel by inject()

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var pointOfInterest: PointOfInterest

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("requestCode", "Location_Code")

        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            Log.e("requestCode", "Location_On")
            checkDeviceLocationSettings(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.confirmButton.setOnClickListener {
            onLocationSelected()
        }
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        setupMap()


        return binding.root
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun onLocationSelected() {
        if (this::pointOfInterest.isInitialized) {
            viewModel.latitude.value = pointOfInterest.latLng.latitude
            viewModel.longitude.value = pointOfInterest.latLng.longitude
            viewModel.selectedPOI.value = pointOfInterest
            viewModel.reminderSelectedLocationStr.value = pointOfInterest.name
            findNavController().popBackStack()

        }


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        setPoiClick(mMap)
        setMapStyle(mMap)
        setMapLongClick(mMap)
        enableMyLocation()
        mMap.setOnMyLocationButtonClickListener {
            Toast.makeText(requireContext(), "ClickedLo", Toast.LENGTH_SHORT).show()
//            checkLastLocation()
//            checkDeviceLocationSettings()
            checkDeviceLocationSettings()


            return@setOnMyLocationButtonClickListener false
        }
    }


    private fun checkPermissions() {
        if (foregroundLocationPermissionApproved()) {
//            checkDeviceLocationSettings()
//            enableMyLocation()
        } else {
            requestForegroundLocationPermissions()
        }
    }


    private fun foregroundLocationPermissionApproved(): Boolean {
        return (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
    }


    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
//        isLocationServicesThere()
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())

        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
//                    exception.startResolutionForResult(
//                        requireActivity(),
//                        REQUEST_TURN_DEVICE_LOCATION_ON
//                    )
                    startIntentSenderForResult(
                        exception.getResolution().getIntentSender(),
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.constraintLayout,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful
//                && foregroundLocationPermissionApproved()
            ) {
                Log.e("requestCode", "isSuccessful")
                checkLastLocation()
//                addGeofenceForClue()
            }
        }
    }


    private fun requestForegroundLocationPermissions() {
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE


        Log.d(TAG, "Request foreground only location permission")
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (
//            grantResults.isEmpty() ||
            grantResults.isNotEmpty() &&
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED

        ) {
            // Permission denied.
            Snackbar.make(
                binding.constraintLayout,
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts(
                            "package",
                            "com.udacity.project4",
                            null
                        )
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkLastLocation()
//            checkDeviceLocationSettings()
        }
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        //TODO: I have enabled my location layer even if the device location is off
        // Just requires the foreground location permission as requested
        //TODO: I have removed the 2 layers of if checks , as requested

        if (isPermissionGranted()) {
            mMap.setMyLocationEnabled(true)
        } else {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            pointOfInterest = poi
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun checkLastLocation() {
//        val client = LocationServices
//            .getFusedLocationProviderClient(
//                requireActivity()
//            )
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
//            && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Log.e("requestCode", "first")
        //TODO: I have used current current location to avoid location being null as requested
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })


            .addOnCompleteListener { getLocation ->
                if (getLocation.isSuccessful) {
                    Log.e("requestCode", "getLocation.isSuccessful")
                    getLocation.result?.let {
                        Log.e("requestCode", "getLocation. not null")

                        val myLocation =
                            LatLng(getLocation.result.latitude, getLocation.result.longitude)
                        enableMyLocation()
                        mMap.clear()
                        mMap.addMarker(
                            MarkerOptions()
                                .position(myLocation)
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
                    }

                }
            }

    }

    //TODO: set on map long click
    // so that The user should be allowed to select a random location as requested
    private fun setMapLongClick(map: GoogleMap) {


        map.setOnMapLongClickListener { latLng ->
            map.clear()
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            pointOfInterest = PointOfInterest(latLng, snippet, snippet)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)


            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

}
