package com.android.shareolc.ui

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
import com.android.shareolc.R
import com.android.shareolc.adapters.MenuAdapter
import com.android.shareolc.base.BaseLocationHelper
import com.android.shareolc.code.OpenLocationCode
import com.android.shareolc.code.OpenLocationCodeUtil
import com.android.shareolc.direction.DirectionUtil
import com.android.shareolc.model.SatelliteModel
import com.android.shareolc.timers.HandlerTimer
import com.android.shareolc.timers.SatelliteTimer
import com.android.shareolc.utils.DialogUtils
import com.android.shareolc.utils.PrefUtil
import com.android.shareolc.utils.Utility
import kotlinx.android.synthetic.main.btn_help.btnHelpHome
import kotlinx.android.synthetic.main.btn_menu.*
import kotlinx.android.synthetic.main.btn_share.*
import kotlinx.android.synthetic.main.layout_about_menu.*
import kotlinx.android.synthetic.main.layout_code.*
import kotlinx.android.synthetic.main.layout_drop_down_menu.*
import kotlinx.android.synthetic.main.layout_main_home.*
import kotlinx.android.synthetic.main.layout_permission_home.*
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
    private lateinit var baseLocationHelper: BaseLocationHelper

    private lateinit var handlerTimer: HandlerTimer
    private lateinit var timeHandler: Handler
    private lateinit var timeRunnable: Runnable

    private lateinit var satelliteTimer: SatelliteTimer
    private lateinit var satelliteTimeHandler: Handler
    private lateinit var satelliteTimeRunnable: Runnable

    private lateinit var sTimer: SatelliteTimer
    private lateinit var sTimeHandler: Handler
    private lateinit var sTimeRunnable: Runnable

    private lateinit var satelliteModel: SatelliteModel
    private var mCurrentLocation: Location? = null
    private var lastFullCode: OpenLocationCode? = null
    private var fullCode: String = "-"
    private var sendAccuracy: String = "-"

    private var minSatellite = 5
    private var accuracyNo = 0.0
    private var accuracyHighStart = 1.0
    private var accuracyHighEnd = 25.0
    private var accuracyMediumStart = 26.0
    private var accuracyMediumEnd = 100.0
    lateinit var tts: TextToSpeech
    var speechMessage = ""
    var lastSpeechMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mContext = this
        dialogUtils = DialogUtils(mContext)
        satelliteModel = SatelliteModel(0, 0)

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
            moveStage1(true)
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


    //New location updated...
    override fun onNewLocation(locationResult: Location?, available: Boolean) {
        this.mCurrentLocation = locationResult
        Log.e("onNewLocation", "===> $available")

        isDone = false

        if (available) {
            moveStage1(false)
        } else {
            speechMessage = getString(R.string.no_connection_available)
            speechLoud()
            txtStateWaiting.text = getString(R.string.no_connection_available)
            txtStateWaiting.visibility = View.VISIBLE
            btnDataShareHome.visibility = View.GONE
            btnRestartHome.visibility = View.GONE
        }
    }


    private fun moveStage1(isRestarted: Boolean) {
        isDone = false
        if (isRestarted) {
            initializeHandlers()
            handlerTimer.stopHandler = false
            satelliteTimer.stopHandler = false
            sTimer.stopHandler = false
        }

        if (mCurrentLocation == null) {
            moveStage2()
        } else {
            showLocationCode(mCurrentLocation!!)
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
                            txtStateWaiting.visibility = View.VISIBLE
                            btnDataShareHome.visibility = View.GONE
                            btnRestartHome.visibility = View.GONE
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
            getSatellitesAvailable()
            when (accuracy) {
                accuracyNo -> {
                    Log.e("moveStage4", "===> " + "no signal ok")
                    txtAccuracyHome.text = accuracyValue(getString(R.string.no_signal))
                    sendAccuracy = "No signal"
                    hideViews()
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
                    hideViews()
                }
                else -> {
                    Log.e("moveStage4", "===> " + "does not reached accuracy ok")
                    moveStage5(false)
                }
            }
        }
    }


    private fun moveStage5(highAccuracy: Boolean) {
        isDone = false
        if (satelliteModel.useInSatellites < minSatellite) {
            Log.e("moveStage5", "===> go open area ok")
            moveOpenArea()
        } else if (satelliteModel.useInSatellites >= minSatellite) {
            txtStateWaiting.text = getString(R.string.cannot_get_high_accuracy_share_anyway)
            if (highAccuracy) {
                speechMessage = ""
                txtStateWaiting.visibility = View.GONE
            } else {
                Log.e("moveStage5", "===> " + "cannot get high accuracy share ok")
                speechMessage = getString(R.string.cannot_get_high_accuracy_share_anyway)
                speechLoud()
                txtStateWaiting.visibility = View.VISIBLE
            }
            btnDataShareHome.visibility = View.VISIBLE
            btnRestartHome.visibility = View.GONE
            moveStage6()
        }
    }


    private fun moveStage6() {
        Log.e("moveStage6", "===> " + "finally done")
        isDone = true
        speechMessage = getString(R.string.ready_to_share)
        speechLoud()

        btnDataShareHome.setOnClickListener {
            createShareData()
        }
    }


    private fun moveOpenArea() {
        speechMessage = getString(R.string.go_to_an_open_area)
        speechLoud()
        txtStateWaiting.text = getString(R.string.go_to_an_open_area)
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
    }

    private fun unableDetectLocation() {
        speechMessage = getString(R.string.unable_to_detect_location_restart)
        speechLoud()
        txtStateWaiting.text = getString(R.string.unable_to_detect_location_restart)
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.VISIBLE
    }

    private fun moveOutside() {
        speechMessage = getString(R.string.please_go_outside)
        speechLoud()
        txtStateWaiting.text = getString(R.string.please_go_outside)
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
    }

    private fun hideViews() {
        speechMessage = ""
        txtStateWaiting.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
        btnDataShareHome.visibility = View.GONE
    }


    private fun createShareData() {
        if (mCurrentLocation != null && isDone) {

            val height = "height:null; "
            val satellites = "sat:" + satelliteModel.useInSatellites + "/" + satelliteModel.totalSatellites + "; "
            val accuracy = "acc:" + sendAccuracy + "," + mCurrentLocation?.accuracy + "m" + "; "
            val sensor = "sensor:null"
            val shareUrl1 = "Google Maps: https://www.google.com/maps/place/$fullCode"
            val shareUrl2 = "OpenStreetMap: https://www.openstreetmap.org/#map=12/" + mCurrentLocation?.latitude + "/" + mCurrentLocation?.longitude
            val shareUrl3 = "Maps.Me: https://ge0.me/$fullCode"
            val shareUrl4 = "Plus Codes: https://plus.codes/$fullCode Get SharePlusCode at URL."

            val shareData = "SharePlusCode." + " Your Plus Code is " + fullCode + " (" + height + satellites + accuracy + sensor + ")." + "\n" +
                    shareUrl1 + "\n" + shareUrl2 + "\n" + shareUrl3 + "\n" + shareUrl4

            Log.e("shareData", "===> $shareData")
            shareIntentData(shareData)
        } else {
            Utility.toastLong(mContext, getString(R.string.sharing_data_not_available))
        }
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
                Log.e("speechLoud", "diff msg ===> $speechMessage")
                sendSpeechLoud()
                lastSpeechMessage = speechMessage
            } else {
                Log.e("speechLoud", "same msg ===> $speechMessage")
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
                            tts.speak(speechMessage, TextToSpeech.QUEUE_FLUSH, null, null);
                        } else {
                            tts.speak(speechMessage, TextToSpeech.QUEUE_FLUSH, null);
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


    //SHOW open location code...
    private fun showLocationCode(location: Location?) {
        if (mCurrentLocation != null) {
            if (location!!.hasBearing() && getCurrentOpenLocationCode() != null) {
                val direction = DirectionUtil.getDirection(location, getCurrentOpenLocationCode(), mCurrentLocation)
                showDistance(direction.distance)
            }
            val code = OpenLocationCodeUtil.createOpenLocationCode(location.latitude, location.longitude)
            lastFullCode = code
            fullCode = code.code
            //String fullCode = "5GRF 2F3V +8M";
            val oneCode = fullCode.substring(0, 4)
            val twoCode = fullCode.substring(4, 8)
            val threeCode = fullCode.substring(8, 11)
            Log.e("fullCode: ", "===> $fullCode")

            txtOneOLCHome.text = oneCode
            txtTwoOLCHome.text = twoCode
            txtThreeOLCHome.text = threeCode
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
        handlerTimer.removeTimerCallbacks()
        satelliteTimer.removeSatelliteTimerCallbacks()
        sTimer.removeSatelliteTimerCallbacks()
    }
}