package com.android.shareolc.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class BaseLocationHelper(private var mContext: Activity) : SensorEventListener {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var mListener: NewLocationListener? = null

    private val updateIntervalMillis: Long = TimeUnit.SECONDS.toMillis(4)
    private val fastestUpdateIntervalMillis = updateIntervalMillis / 2

    private var mGoogleApiAvailability: GoogleApiAvailability? = null
    private var mUsingGms = false

    private var mNetworkLocationListener: LocationListener? = null
    private var mGpsLocationListener: LocationListener? = null

    private var mCurrentBestLocation: Location? = null
    private var mBearing: Float = 0.0f
    private var mAxisX = 0
    private var mAxisY = 0

    private val tag = "BaseLocationHelper"
    private val fastestIntervalMillisO = 1000

    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    var isConnected = false


    private lateinit var mLocationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private lateinit var windowManager: WindowManager
    private lateinit var defaultDisplay: Display
    private lateinit var mSensor: Sensor
    private val minBearingOff = 2.0f


    fun initLocation() {
        mLocationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        defaultDisplay = windowManager.defaultDisplay
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL * 5)

        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        mGoogleApiAvailability = GoogleApiAvailability.getInstance()
        locationRequest = LocationRequest()
        determineIfUsingGms()

        locationRequest.interval = updateIntervalMillis
        locationRequest.fastestInterval = fastestUpdateIntervalMillis
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext)

        mNetworkLocationListener = createLocationListener()
        mGpsLocationListener = createLocationListener()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult?.lastLocation != null) {
                    mCurrentBestLocation = locationResult.lastLocation
                    mCurrentBestLocation?.bearing = mBearing
                    newLocationUpdated(mCurrentBestLocation, "from latest api", true)
                } else {
                    Log.e(tag, "Location missing in callback.")
                }
            }
        }
    }


    fun connectLocation() {
        if (isUsingGms() && isGPSEnabled) {
            //used latest api
            Log.e("connectLocation", "using latest api")
            try {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    if (it != null) {
                        Log.e("addOnSuccessListener", "done")
                    } else {
                        Log.e("addOnSuccessListener", "failed")
                        connectOldApi()
                    }
                }
            } catch (unlikely: SecurityException) {
                Log.e(tag, "Lost location permissions. Couldn't remove updates. $unlikely")
            }
        } else {
            //used older api
            Log.e("connectLocation", "using old api")
            connectOldApi()
        }
    }


    private fun newLocationUpdated(lastLocation: Location?, fromApi: String, available: Boolean) {
        //mCurrentBestLocation = lastLocation
        isConnected = true
        mListener?.onNewLocation(lastLocation, available)
    }


    @SuppressLint("MissingPermission")
    private fun connectOldApi() {
        val lastKnownGpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastKnownNetworkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        var bestLastKnownLocation = mCurrentBestLocation
        if (lastKnownGpsLocation != null && LocationUtil.isBetterLocation(lastKnownGpsLocation, bestLastKnownLocation)) {
            bestLastKnownLocation = lastKnownGpsLocation
        }
        if (lastKnownNetworkLocation != null && LocationUtil.isBetterLocation(lastKnownNetworkLocation, bestLastKnownLocation)) {
            bestLastKnownLocation = lastKnownNetworkLocation
        }

        mCurrentBestLocation = bestLastKnownLocation
        val gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        Log.e(tag, "gpsEnabled old API: $gpsEnabled")
        Log.e(tag, "networkEnabled old API: $networkEnabled")

        if (gpsEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, fastestIntervalMillisO.toLong(), 10f, mGpsLocationListener!!)
        }

        if (networkEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, fastestIntervalMillisO.toLong(), 10f, mNetworkLocationListener!!)
        }

        if (!gpsEnabled && !networkEnabled) {
            Log.e(tag, "Received last known location via old API: $mCurrentBestLocation")
            newLocationUpdated(mCurrentBestLocation, "from no api", false)
        }
    }


    fun disconnectLocation() {
        if (isUsingGms()) {
            //used latest api
            stopTrackingLocation()
        } else {
            //used older api
            mLocationManager.removeUpdates(mGpsLocationListener!!)
            mLocationManager.removeUpdates(mNetworkLocationListener!!)
        }
    }


    private fun createLocationListener(): LocationListener? {
        return object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.i(tag, "onLocationChanged via old API: $location")
                if (LocationUtil.isBetterLocation(location, mCurrentBestLocation)) {
                    mCurrentBestLocation = location
                    mCurrentBestLocation?.bearing = mBearing
                    newLocationUpdated(mCurrentBestLocation, "from older api", true)
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }


    private fun stopTrackingLocation() {
        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e(tag, "Location Callback removed.")
                } else {
                    Log.e(tag, "Failed to remove Location Callback.")
                }
            }
        } catch (unlikely: SecurityException) {
            Log.e(tag, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }


    private fun isUsingGms(): Boolean {
        return mUsingGms
    }

    private fun determineIfUsingGms() {
        val statusCode = mGoogleApiAvailability?.isGooglePlayServicesAvailable(mContext)
        if (statusCode == ConnectionResult.SUCCESS || statusCode == ConnectionResult.SERVICE_UPDATING) {
            mUsingGms = true
        }
    }

    interface NewLocationListener {
        fun onNewLocation(locationResult: Location?, available: Boolean)
    }

    fun setOnNewLocationListener(newLocationListener: NewLocationListener) {
        this.mListener = newLocationListener
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor!!.type == Sensor.TYPE_ROTATION_VECTOR) {
            Log.i(tag, "Rotation sensor accuracy changed to: $accuracy")
        }
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val rotationMatrix = FloatArray(16)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event!!.values)
        val orientationValues = FloatArray(3)
        readDisplayRotation()
        SensorManager.remapCoordinateSystem(rotationMatrix, mAxisX, mAxisY, rotationMatrix)
        SensorManager.getOrientation(rotationMatrix, orientationValues)
        val azimuth = Math.toDegrees(orientationValues[0].toDouble())
        val newBearing = azimuth.toFloat()
        if (abs(mBearing - newBearing) > minBearingOff) {
            mBearing = newBearing
        }
    }


    private fun readDisplayRotation() {
        mAxisX = SensorManager.AXIS_X
        mAxisY = SensorManager.AXIS_Y
        when (defaultDisplay.rotation) {
            Surface.ROTATION_0 -> {
            }
            Surface.ROTATION_90 -> {
                mAxisX = SensorManager.AXIS_Y
                mAxisY = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> mAxisY = SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> {
                mAxisX = SensorManager.AXIS_MINUS_Y
                mAxisY = SensorManager.AXIS_X
            }
            else -> {
            }
        }
    }
}