package com.android.sharepluscode.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.GpsStatus
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sharepluscode.R
import com.android.sharepluscode.adapters.MenuAdapter
import com.android.sharepluscode.base.BaseLocationHelper
import com.android.sharepluscode.code.OpenLocationCode
import com.android.sharepluscode.code.OpenLocationCodeUtil
import com.android.sharepluscode.model.SatelliteModel
import com.android.sharepluscode.timers.HandlerTimer
import com.android.sharepluscode.timers.SatelliteTimer
import com.android.sharepluscode.timers.StartSecondsTimer
import com.android.sharepluscode.utils.DialogUtils
import com.android.sharepluscode.utils.JSConstant
import com.android.sharepluscode.utils.PrefUtil
import com.android.sharepluscode.utils.Utility
import kotlinx.android.synthetic.main.btn_help.btnHelpHome
import kotlinx.android.synthetic.main.btn_menu.*
import kotlinx.android.synthetic.main.btn_share.*
import kotlinx.android.synthetic.main.layout_about_menu.*
import kotlinx.android.synthetic.main.layout_code.*
import kotlinx.android.synthetic.main.layout_drop_down_menu.*
import kotlinx.android.synthetic.main.layout_main_home.*
import kotlinx.android.synthetic.main.layout_permission_home.*
import java.lang.Exception
import java.util.*


class MainActivity : RuntimePermissionActivity(), BaseLocationHelper.NewLocationListener {

    private lateinit var mContext: Activity
    private var isShowingMenu = false
    private var isShowingAbout = false

    private lateinit var dialogUtils: DialogUtils
    private lateinit var locationManager: LocationManager
    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    private var isDone = false
    private var isSpeechButton = false
    private var is10Timer = false
    private lateinit var baseLocationHelper: BaseLocationHelper

    //timers...
    private lateinit var handlerTimer: HandlerTimer
    private lateinit var timeHandler: Handler
    private lateinit var timeRunnable: Runnable

    private lateinit var satelliteTimer: SatelliteTimer
    private lateinit var satelliteTimeHandler: Handler
    private lateinit var satelliteTimeRunnable: Runnable

    private lateinit var sTimer: SatelliteTimer
    private lateinit var sTimeHandler: Handler
    private lateinit var sTimeRunnable: Runnable

    private lateinit var startSecondsTimer: StartSecondsTimer
    private lateinit var secondsHandler: Handler
    private lateinit var secondsRunnable: Runnable


    private lateinit var satelliteModel: SatelliteModel
    private var mCurrentLocation: Location? = null
    private var lastFullCode: OpenLocationCode? = null
    private var fullCode: String = "-"
    private var sendAccuracy: String = "-"

    private var minSatellite = 5
    private var accuracyNo = 0.0
    private var accuracyHighStart = 1.0
    private var accuracyHighEnd = 5.0
    private var accuracyMediumStart = 6.0
    private var accuracyMediumEnd = 100.0
    lateinit var tts: TextToSpeech
    private var speechMessage = ""
    private var lastSpeechMessage = ""
    private var altitudeHeight = 0.0
    private var sensorData = ""
    private var accuracyShareData = 0.0f

    private var endMilliseconds = 10000L //10 seconds
    private var waitMilliseconds = 500L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mContext = this
        dialogUtils = DialogUtils(mContext)
        satelliteModel = SatelliteModel(0, 0)
        JSConstant.IS_READY_SHARE = false
        is10Timer = false
        //initialize handler...
        initializeHandlers()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        baseLocationHelper = BaseLocationHelper(mContext)
        baseLocationHelper.initLocation()
        baseLocationHelper.setOnNewLocationListener(this)
        initViews()
    }


    private fun initializeHandlers() {
        handlerTimer = HandlerTimer()
        timeHandler = handlerTimer.timeHandler
        timeRunnable = handlerTimer.timeRunnable

        satelliteTimer = SatelliteTimer()
        satelliteTimeHandler = satelliteTimer.satelliteTimeHandler
        satelliteTimeRunnable = satelliteTimer.satelliteTimeRunnable

        sTimer = SatelliteTimer()
        sTimeHandler = sTimer.satelliteTimeHandler
        sTimeRunnable = sTimer.satelliteTimeRunnable

        startSecondsTimer = StartSecondsTimer()
        secondsHandler = startSecondsTimer.secondsTimeHandler
        secondsRunnable = startSecondsTimer.secondsTimeRunnable
    }


    private fun initViews() {
        btnMenuHome.setOnClickListener {
            layoutMenuHome.visibility = View.VISIBLE
            isShowingMenu = true
        }

        btnCloseMenuHome.setOnClickListener {
            layoutMenuHome.visibility = View.GONE
            isShowingMenu = false
        }

        btnHelpHome.setOnClickListener {
            isSpeechButton = true
            sendSpeechLoud()
        }

        btnShareHome.setOnClickListener {
            createShareData()
        }

        btnAboutMenu.setOnClickListener {
            layoutMenuHome.visibility = View.GONE
            layoutAboutMenuHome.visibility = View.VISIBLE
            isShowingMenu = false
            isShowingAbout = true
        }

        btnCloseAboutMenu.setOnClickListener {
            layoutAboutMenuHome.visibility = View.GONE
            isShowingAbout = false
        }

        recycleMenu.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        recycleMenu.adapter = MenuAdapter(mContext)

        btnAllowPermissionHome.setOnClickListener {
            requestAppPermissions(ARRAY_PERMISSIONS, R.string.app_name, ARRAY_PERMISSION_CODE)
        }


        btnRestartHome.setOnClickListener {
            startStage(true)
        }

        btnOutsideHome.setOnClickListener {
            waitingViews()
        }
    }


    override fun onResume() {
        super.onResume()
        //CHECK PERMISSION...
        if (!hasPermissions(mContext)) {
            layoutPermissionHome.visibility = View.VISIBLE
            layoutMainHome.visibility = View.GONE
        } else {
            layoutPermissionHome.visibility = View.GONE
            layoutMainHome.visibility = View.VISIBLE
            setupGPSData()
        }
    }


    private fun setupGPSData() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGPSEnabled) {
            dialogUtils.showGPSSettingsAlert()
        } else {
            dialogUtils.isDismissGPSAlert()
        }
        baseLocationHelper.connectLocation()
    }


    override fun onPause() {
        super.onPause()
        baseLocationHelper.disconnectLocation()
    }


    override fun onPermissionsGranted(requestCode: Int, isGranted: Boolean) {
        if (requestCode == ARRAY_PERMISSION_CODE && isGranted) {
            layoutPermissionHome.visibility = View.GONE
            layoutMainHome.visibility = View.VISIBLE
        } else {
            layoutPermissionHome.visibility = View.VISIBLE
            layoutMainHome.visibility = View.GONE
        }
    }


    //new location updated...
    override fun onNewLocation(locationResult: Location?, available: Boolean) {
        this.mCurrentLocation = locationResult
        Log.e("onNewLocation", "===> $available")
        if (!JSConstant.IS_READY_SHARE) {
            isDone = false
            if (available) {
                startStage(false)
            } else {
                speechMessage = getString(R.string.no_connection_available)
                speechLoud()
                noConnectionAvailable()
            }
        }
    }


    private fun startStage(isRestarted: Boolean) {
        isDone = false
        if (isRestarted) {
            initializeHandlers()
            handlerTimer.stopHandler = false
            satelliteTimer.stopHandler = false
            sTimer.stopHandler = false
            startSecondsTimer.startStopHandler = false
        }


        if (startSecondsTimer.startStopHandler) {
            moveStage1()
        } else {
            waitingViews()
            if (!startSecondsTimer.isSecondsRunning) {
                secondsHandler.postDelayed(secondsRunnable, 0)
            }
            startSecondsTimer.setOnTimeListener(object : StartSecondsTimer.SecondTimerTickListener {
                override fun onTickListener(seconds: Int) {
                    if (seconds == 1) {
                        startSecondsTimer.removeSecondsTimerCallbacks()
                        Log.e("moveStage1", "===> " + "10 seconds done")
                        moveStage1()
                    }
                }
            })
        }
    }


    private fun moveStage1() {
        if (mCurrentLocation == null) {
            moveStage2()
        } else {
            showDistanceCode(mCurrentLocation!!)
            moveStage4()
        }
    }


    private fun moveStage2() {
        isDone = false
        getSatellitesAvailable()
        if (satelliteModel.totalSatellites == 0) {
            Log.e("moveStage2", "===> " + "move outside ok")
            moveOutside()

            if (satelliteTimer.stopHandler) {
                unableDetectLocation()
            } else {
                if (!satelliteTimer.isSatelliteRunning) {
                    satelliteTimeHandler.postDelayed(satelliteTimeRunnable, 0)
                }
                satelliteTimer.setOnTimeListener(object : SatelliteTimer.SatelliteTimerTickListener {
                    override fun onTickListener(minutes: Int) {
                        if (minutes == 1) {
                            satelliteTimer.removeSatelliteTimerCallbacks()
                            unableDetectLocation()
                        } else {
                            Log.e("moveStage2 counter 3", "===> " + "else ok")
                            speechMessage = ""
                            if (satelliteModel.totalSatellites != 0) {
                                moveStage3()
                            }
                        }
                    }
                })
            }
        } else {
            Log.e("moveStage2", "===> " + "else ok")
            moveStage3()
        }
    }


    private fun moveStage3() {
        isDone = false
        if (satelliteModel.useInSatellites >= minSatellite) {
            Log.e("moveStage3", "===> " + "satellites greater ok")
            if (handlerTimer.stopHandler) {
                moveStage4()
            } else {
                if (!handlerTimer.isRunning) {
                    timeHandler.postDelayed(timeRunnable, 0)
                }
                handlerTimer.setOnTimeListener(object : HandlerTimer.TimerTickListener {
                    override fun onTickListener(minutes: Int) {
                        if (minutes == 1) {
                            handlerTimer.removeTimerCallbacks()
                            moveStage4()
                        } else {
                            Log.e("moveStage3", "===> " + "stay outside ok")
                            val stayOutsideMessage = String.format(mContext.resources.getString(R.string.stay_outside_minutes), minutes)
                            speechMessage = stayOutsideMessage
                            speechLoud()
                            txtStateWaiting.text = stayOutsideMessage
                            stayOutSideViews()
                        }
                    }
                })
            }
        } else if (satelliteModel.useInSatellites < minSatellite) {
            Log.e("moveStage3", "===> " + "satellites less ok")
            moveOpenArea()
            if (sTimer.stopHandler) {
                unableDetectLocation()
            } else {
                if (!sTimer.isSatelliteRunning) {
                    sTimeHandler.postDelayed(sTimeRunnable, 0)
                }
                sTimer.setOnTimeListener(object : SatelliteTimer.SatelliteTimerTickListener {
                    override fun onTickListener(minutes: Int) {
                        if (minutes == 1) {
                            Log.e("moveStage3", "===> " + "else unable detect ok")
                            sTimer.removeSatelliteTimerCallbacks()
                            unableDetectLocation()
                        } else {
                            speechMessage = ""
                        }
                    }
                })
            }
        }
    }


    private fun moveStage4() {
        isDone = false
        if (mCurrentLocation != null) {
            val accuracy = mCurrentLocation?.accuracy!!.toDouble()
            accuracyShareData = accuracy.toFloat()
            Log.e("accuracy", "===> $accuracy")

            getSatellitesAvailable()
            Handler().postDelayed({
                when (accuracy) {
                    accuracyNo -> {
                        Log.e("moveStage4", "===> " + "no signal ok")
                        txtAccuracyHome.text = accuracyValue(getString(R.string.no_signal))
                        sendAccuracy = "No signal"
                        waitingViews()
                    }
                    in accuracyHighStart..accuracyHighEnd -> {
                        Log.e("moveStage4", "===> " + "high ok")
                        txtAccuracyHome.text = accuracyValue(getString(R.string.high_accuracy))
                        sendAccuracy = getString(R.string.high_accuracy)
                        moveStage5(true)
                    }
                    in accuracyMediumStart..accuracyMediumEnd -> {
                        Log.e("moveStage4", "===> " + "medium ok")
                        txtAccuracyHome.text = accuracyValue(getString(R.string.medium_accuracy))
                        sendAccuracy = getString(R.string.medium_accuracy)
                        waitingViews()
                    }
                    else -> {
                        Log.e("moveStage4", "===> " + "does not reached accuracy ok")
                        txtAccuracyHome.text = accuracyValue(getString(R.string.medium_accuracy))
                        sendAccuracy = getString(R.string.medium_accuracy)
                        moveStage5(false)
                    }
                }
            }, 1000L)
        }
    }


    private fun moveStage5(highAccuracy: Boolean) {
        isDone = false
        if (satelliteModel.totalSatellites == 0) {
            Log.e("moveStage4", "===> totalSatellites 0")
            moveOutside()
        } else {
            if (satelliteModel.useInSatellites < minSatellite) {
                Log.e("moveStage4", "===> go open area ok")
                moveOpenArea()
            } else if (satelliteModel.useInSatellites >= minSatellite) {
                if (highAccuracy) {
                    is10Timer = true
                    highAccuracyViews()
                } else {
                    Log.e("moveStage5", "===> " + "cannot get high accuracy share ok")
                    is10Timer = false
                    cannotAccuracyViews()
                }

                Handler().postDelayed({
                    Log.e("moveStage6", "===> " + "going")
                    moveStage6()
                }, waitMilliseconds)
            }
        }
    }


    private fun moveStage6() {
        //end seconds timer...
        if (is10Timer) {
            JSConstant.IS_READY_SHARE = true
            waitingViews()
            Handler().postDelayed({
                Log.e("isReadyToShare", "===> " + " reset")
                isDone = true
                isReadyShareViews()
            }, endMilliseconds)
        }

        btnDataShareHome.setOnClickListener {
            createShareData()
        }
    }


    private fun highAccuracyViews() {
        speechMessage = ""
        txtStateWaiting.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.GONE
        btnDataShareHome.visibility = View.GONE
    }


    private fun cannotAccuracyViews() {
        speechMessage = getString(R.string.cannot_get_high_accuracy_share_anyway)
        speechLoud()
        txtStateWaiting.text = getString(R.string.cannot_get_high_accuracy_share_anyway)
        txtStateWaiting.visibility = View.VISIBLE
        btnRestartHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.GONE
        btnDataShareHome.visibility = View.GONE
    }


    private fun isReadyShareViews() {
        speechMessage = getString(R.string.ready_to_share)
        speechLoud()
        txtStateWaiting.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.VISIBLE
    }


    private fun moveOpenArea() {
        speechMessage = getString(R.string.go_to_an_open_area)
        speechLoud()
        txtStateWaiting.text = getString(R.string.go_to_an_open_area)
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
    }

    private fun unableDetectLocation() {
        speechMessage = getString(R.string.unable_to_detect_location_restart)
        speechLoud()
        txtStateWaiting.text = getString(R.string.unable_to_detect_location_restart)
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.VISIBLE
        btnTextDataShareHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
    }

    private fun moveOutside() {
        speechMessage = getString(R.string.please_go_outside)
        speechLoud()
        txtStateWaiting.text = getString(R.string.please_go_outside)
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.GONE
        btnOutsideHome.visibility = View.VISIBLE
    }


    private fun waitingViews() {
        speechMessage = getString(R.string.please_wait)
        speechLoud()
        txtStateWaiting.text = getString(R.string.please_wait)
        txtStateWaiting.visibility = View.VISIBLE
        btnRestartHome.visibility = View.GONE
        btnDataShareHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
    }

    private fun noConnectionAvailable() {
        txtStateWaiting.text = getString(R.string.no_connection_available)
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
    }

    private fun stayOutSideViews() {
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
    }


    private fun createShareData() {
        if (mCurrentLocation != null && isDone) {
            altitudeHeight = mCurrentLocation?.altitude!!
            val accuracyFormat = formatDecimal(accuracyShareData)
            val latitude = mCurrentLocation?.latitude!!
            val longitude = mCurrentLocation?.longitude!!
            val altitudeFormat = formatDecimal(altitudeHeight.toFloat())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //horizontal accuracy, vertical accuracy
                val verticalAccuracy = mCurrentLocation?.verticalAccuracyMeters
                sensorData = accuracyFormat + "," + formatDecimal(verticalAccuracy!!)
            } else {
                sensorData = accuracyFormat
            }

            //location data...
            val height = "height:" + altitudeFormat + "m; "
            val satellites = "sat:" + satelliteModel.useInSatellites + "/" + satelliteModel.totalSatellites + "; "
            val accuracy = "acc:" + sendAccuracy + "," + accuracyFormat + "m" + "; "
            val sensor = "sensor:$sensorData"
            val dataValue = " ($height$satellites$accuracy$sensor)."

            //urls data...
            val getSharePlusCodeData = String.format(mContext.resources.getString(R.string.share_getpluscode), fullCode)
            val shareUrl1 = "\nGoogle Maps: https://www.google.com/maps/place/$fullCode"
            val shareUrl2 = "\nOpenStreetMap: https://www.openstreetmap.org/#map=14/$latitude/$longitude"
            val shareUrl3 = "\nMaps.Me: https://ge0.me/$fullCode"
            val shareUrl4 = "\nPlus Codes: https://plus.codes/$getSharePlusCodeData"
            val urlsData = shareUrl1 + shareUrl2 + shareUrl3 + shareUrl4

            val startShareData = String.format(mContext.resources.getString(R.string.share_sharepluscode), fullCode)
            val shareData = startShareData + dataValue + urlsData
            Log.e("shareData", "===> $shareData")
            shareIntentData(shareData)
        } else {
            Utility.toastLong(mContext, getString(R.string.sharing_data_not_available))
        }
    }


    private fun formatDecimal(value: Float): String {
        return String.format("%.2f", value)
    }

    private fun shareIntentData(shareData: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareData)
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }


    private fun speechLoud() {
        if (speechMessage.isNotEmpty()) {
            if (speechMessage != lastSpeechMessage) {
                sendSpeechLoud()
                lastSpeechMessage = speechMessage
            }
        }
    }


    private fun sendSpeechLoud() {
        if (speechMessage.isNotEmpty()) {
            Log.e("sendSpeechLoud", "msg ===> $speechMessage")
            tts = TextToSpeech(this, TextToSpeech.OnInitListener {
                if (it == TextToSpeech.SUCCESS) {
                    val appLanguage = PrefUtil.getStringPref(PrefUtil.PRF_LANGUAGE, mContext)
                    val result: Int = tts.setLanguage(Locale(appLanguage))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Utility.toastLong(mContext, "Current Language is not supported for speech")
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tts.speak(speechMessage, TextToSpeech.QUEUE_FLUSH, null, null)
                        } else {
                            tts.speak(speechMessage, TextToSpeech.QUEUE_FLUSH, null)
                        }
                    }
                } else {
                    Utility.toastLong(mContext, "Failed to voice initialization")
                }
            })
        }
    }


    private fun accuracyValue(quality: String): String? {
        return String.format(mContext.resources.getString(R.string.accuracy_value), quality)
    }


    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun getSatellitesAvailable() {
        if (mCurrentLocation != null) {
            locationManager.addGpsStatusListener { event ->
                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    if (!JSConstant.IS_READY_SHARE) {
                        val gpsStatus = locationManager.getGpsStatus(null)
                        if (gpsStatus != null) {
                            val satellitesList = gpsStatus.satellites
                            if (satellitesList != null) {
                                var totalSatellites = 0
                                var useSatellites = 0
                                for (gpsSatellite in satellitesList) {
                                    totalSatellites++
                                    if (gpsSatellite.usedInFix()) {
                                        useSatellites++
                                    }
                                }
                                txtSateliteHome.text = String.format(mContext.resources.getString(R.string.satellites_value), useSatellites, totalSatellites)
                                satelliteModel = SatelliteModel(totalSatellites, useSatellites)
                            }
                        }
                    }
                }
            }
        }
    }


    //SHOW open location code...
    private fun showDistanceCode(location: Location?) {
        if (location != null) {

            val code = OpenLocationCodeUtil.createOpenLocationCode(location.latitude, location.longitude)
            lastFullCode = code
            fullCode = code.code

            //String fullCode = "5GRF 2F3V +8M";
            val oneCode = fullCode.substring(0, 4)
            val twoCode = fullCode.substring(4, 8)
            val threeCode = fullCode.substring(8, 12)
            //Log.e("fullCode: ", "===> $fullCode")
            //Log.e("threeCode: ", "===> $threeCode")

            txtOneOLCHome.text = oneCode
            txtTwoOLCHome.text = twoCode
            txtThreeOLCHome.text = threeCode

            val accuracyFormat = formatDecimal(location.accuracy)
            txtDistanceHome.text = accuracyFormat + "m"

            //if (location!!.hasBearing() && getCurrentOpenLocationCode() != null) {
            /*if (getCurrentOpenLocationCode() != null) {
                val direction = DirectionUtil.getDirection(location, getCurrentOpenLocationCode(), mCurrentLocation)
                showDistance(direction.distance)
                Log.e("distance: ", "===> " + direction.distance)
                //Log.e("accuracy: ", "===> "+location.accuracy+"m")
            }*/
        }
    }


    private fun showDistance(distanceInMeters: Int) {
        when {
            distanceInMeters < 1000 -> {
                txtDistanceHome.text = String.format(mContext.resources.getString(R.string.distance_meters), distanceInMeters)
            }
            distanceInMeters < 3000 -> {
                val distanceInKm = distanceInMeters / 1000.0
                txtDistanceHome.text = String.format(mContext.resources.getString(R.string.distance_few_kilometers), distanceInKm)
            }
            else -> {
                val distanceInKm = distanceInMeters / 1000.0
                txtDistanceHome.text = String.format(mContext.resources.getString(R.string.distance_many_kilometers), distanceInKm)
            }
        }
    }

    private fun getCurrentOpenLocationCode(): OpenLocationCode? {
        return lastFullCode
    }

    override fun onBackPressed() {
        when {
            isShowingMenu -> {
                layoutMenuHome.visibility = View.GONE
                isShowingMenu = false
            }
            isShowingAbout -> {
                layoutAboutMenuHome.visibility = View.GONE
                isShowingAbout = false
            }
            else -> {
                super.onBackPressed()
            }
        }
    }


    fun hideMenu() {
        when {
            isShowingMenu -> {
                layoutMenuHome.visibility = View.GONE
                isShowingMenu = false
            }
            isShowingAbout -> {
                layoutAboutMenuHome.visibility = View.GONE
                isShowingAbout = false
            }
        }
    }

    override fun updateLocale(locale: Locale) {
        super.updateLocale(locale)
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            handlerTimer.removeTimerCallbacks()
            satelliteTimer.removeSatelliteTimerCallbacks()
            sTimer.removeSatelliteTimerCallbacks()
            startSecondsTimer.removeSecondsTimerCallbacks()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}