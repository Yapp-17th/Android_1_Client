package com.yapp.picon.presentation.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat

class LocationHelper(private val context: Context) {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation(): Location? {
        val isGPSEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled: Boolean =
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return if (checkPermission()) {
            when {
                isNetworkEnabled -> {
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                isGPSEnabled -> {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                else -> null
            }
        } else {
            null
        }
    }

    fun requestLocationPermissions(): Location? {
        getLocation()?.let {
            return it
        }
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), RequestCodeSet.LOCATION_REQUEST_CODE.code
        )
        return null
    }

}