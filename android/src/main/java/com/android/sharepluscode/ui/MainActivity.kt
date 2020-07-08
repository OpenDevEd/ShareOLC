package com.android.sharepluscode.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.location.GpsStatus
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sharepluscode.R
import com.android.sharepluscode.adapters.MenuAdapter
import com.android.sharepluscode.base.BaseLocationHelper
import com.android.sharepluscode.code.OpenLocationCode
import com.android.sharepluscode.code.OpenLocationCodeUtil
import com.android.sharepluscode.localeHelper.LocaleHelper
import com.android.sharepluscode.model.SatelliteModel
import com.android.sharepluscode.timers.HandlerTimer
import com.android.sharepluscode.timers.SatelliteTimer
import com.android.sharepluscode.timers.StartSecondsTimer
import com.android.sharepluscode.timers.TimerSpentHandler
import com.android.sharepluscode.utils.DialogUtils
import com.android.sharepluscode.utils.JSConstant
import com.android.sharepluscode.utils.PrefUtil
import com.android.sharepluscode.utils.Utility
import com.google.firebase.FirebaseApp
import kotlinx.android.synthetic.main.btn_help.btnHelpHome
import kotlinx.android.synthetic.main.btn_menu.*
import kotlinx.android.synthetic.main.btn_share.*
import kotlinx.android.synthetic.main.layout_about_menu.*
import kotlinx.android.synthetic.main.layout_code.*
import kotlinx.android.synthetic.main.layout_drop_down_menu.*
import kotlinx.android.synthetic.main.layout_main_home.*
import kotlinx.android.synthetic.main.layout_permission_home.*
import java.util.*
import java.util.concurrent.TimeUnit


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

    //Time spent handler...
    private lateinit var timerSpentHandler: TimerSpentHandler
    private lateinit var spentHandler: Handler
    private lateinit var spentRunnable: Runnable

    private lateinit var satelliteModel: SatelliteModel
    private var mCurrentLocation: Location? = null
    private var lastFullCode: OpenLocationCode? = null
    private var fullCode: String = "-"
    private var sendAccuracy: String = "-"

    private var textToSpeech: TextToSpeech? = null
    private var speechMessage = ""
    private var lastSpeechMessage = ""
    private var altitudeHeight = 0.0
    private var sensorData = ""
    private var accuracyShareData = 0.0f
    private var waitMilliseconds = 500L
    private var accuracy = 0.0

    //private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var menuAdapter: MenuAdapter

    override fun attachBaseContext(newBase: Context?) {
        val onAttach = LocaleHelper.onAttach(newBase!!)
        super.attachBaseContext(onAttach)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        //window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mContext = this
        //LocaleHelper.onAttach(this)
        dialogUtils = DialogUtils(mContext)
        satelliteModel = SatelliteModel(0, 0)
        JSConstant.IS_READY_SHARE = false
        is10Timer = false

        initializeHandlers()
        //val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        //wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SharePlusCode:WakelockTag")

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

        timerSpentHandler = TimerSpentHandler()
        spentHandler = timerSpentHandler.spentHandler
        spentRunnable = timerSpentHandler.spentRunnable
        timerSpentHandler.resetData()
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
            //isSpeechButton = true
            //sendSpeechLoud()
            layoutMenuHome.visibility = View.GONE
            layoutAboutMenuHome.visibility = View.VISIBLE
            isShowingMenu = false
            isShowingAbout = true
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
        menuAdapter = MenuAdapter(mContext)
        recycleMenu.adapter = menuAdapter

        //Runtime permission...
        btnAllowPermissionHome.setOnClickListener {
            requestAppPermissions(arrayPermissions, arrayPermissionCode)
        }

        btnRestartHome.setOnClickListener {
            startStage(true)
        }

        btnOutsideHome.setOnClickListener {
            waitingViews()
        }

        val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName: String = pInfo.versionName
        txtVersionAbout.text = "Version: $versionName"


        //onLanguageChanged...
        menuAdapter.setOnLanguageListener(object : MenuAdapter.LanguageChangedListener {
            override fun onLanguageChanged(locale: Locale?) {
                try {
                    if (locale != null) {
                        //updateLocale(locale)
                        setLocale(mContext, locale)
                        menuAdapter.notifyDataSetChanged()
                        hideMenu()
                        restartActivity()
                    }
                } catch (e: Exception) {
                    DialogUtils.showExceptionAlert(mContext, "Exception: From adapter", e.message.toString() + " StackTrace: " + e.printStackTrace())
                }
            }
        })
    }


    private fun restartActivity() {
        finish()
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity)
    }


    override fun onResume() {
        super.onResume()
        //wakeLock.acquire(10 * 60 * 1000L)
        //CHECK PERMISSION...
        if (!hasPermissions(mContext)) {
            layoutPermissionHome.visibility = View.VISIBLE
            layoutMainHome.visibility = View.GONE
        } else {
            Log.e("onResume: ", "===> " + "ok")
            //initializeHandlers()
            layoutPermissionHome.visibility = View.GONE
            layoutMainHome.visibility = View.VISIBLE
            textToSpeech = TextToSpeech(this, mTextToSpeechListener)
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


    override fun onPermissionsGranted(requestCode: Int, isGranted: Boolean) {
        if (requestCode == arrayPermissionCode && isGranted) {
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


    //starting stage...
    private fun startStage(isRestarted: Boolean) {
        isDone = false
        if (isRestarted) {
            initializeHandlers()
            handlerTimer.stopHandler = false
            satelliteTimer.stopHandler = false
            sTimer.stopHandler = false
            startSecondsTimer.startStopHandler = false
            timerSpentHandler.spentStopHandler = false
        }

        //started time spend timer...
        timerSpentHandler.updateData(JSConstant.JSEVENT_STAGE1)
        if (!timerSpentHandler.isSpentRunning) {
            spentHandler.postDelayed(spentRunnable, 0)
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
                        moveStage1()
                    } else {
                        if (mCurrentLocation != null) {
                            startSecondsTimer.removeSecondsTimerCallbacks()
                            moveStage1()
                        }
                    }
                }
            })
        }
    }


    //move to stage1...
    private fun moveStage1() {
        if (mCurrentLocation == null) {
            moveStage2()
        } else {
            showLocationCode(mCurrentLocation!!)
            moveStage4()
        }
    }


    //move to stage2...
    private fun moveStage2() {
        timerSpentHandler.updateData(JSConstant.JSEVENT_STAGE2)
        isDone = false

        getSatellitesAvailable()
        if (satelliteModel.totalSatellites == 0) {
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
                            speechMessage = ""
                            if (satelliteModel.totalSatellites != 0) {
                                satelliteTimer.removeSatelliteTimerCallbacks()
                                moveStage3()
                            }
                        }
                    }
                })
            }
        } else {
            moveStage3()
        }
    }


    //move to stage3...
    private fun moveStage3() {
        timerSpentHandler.updateData(JSConstant.JSEVENT_STAGE3)
        isDone = false

        if (satelliteModel.useInSatellites >= JSConstant.minSatellite) {
            Log.e("moveStage3", "===> " + "satellites greater ok")
            moveStage4()
        } else if (satelliteModel.useInSatellites < JSConstant.minSatellite) {
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


    //move to stage4...
    private fun moveStage4() {
        timerSpentHandler.updateData(JSConstant.JSEVENT_STAGE4)
        isDone = false

        if (mCurrentLocation != null) {
            accuracy = mCurrentLocation?.accuracy!!.toDouble()
            accuracyShareData = accuracy.toFloat()
            Log.e("accuracy", "===> ok $accuracy")

            getSatellitesAvailable()
            if (handlerTimer.stopHandler) {
                moveStage4CheckSatellites(false)
            } else {
                if (!handlerTimer.isRunning) {
                    timeHandler.postDelayed(timeRunnable, 0)
                }
                handlerTimer.setOnTimeListener(object : HandlerTimer.TimerTickListener {
                    override fun onTickListener(milliSeconds: Long) {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliSeconds).toInt()
                        Log.e("moveStage4", "===> timer tick $minutes")
                        checkingStage4Data(minutes)
                    }
                })
            }
        }
    }


    private fun checkingStage4Data(minutes: Int) {
        if (minutes == 0) {
            handlerTimer.removeTimerCallbacks()
            moveStage4CheckSatellites(false)
        } else {
            if (accuracy == JSConstant.accuracyNo) {
                txtAccuracyHome.text = accuracyValue(getString(R.string.no_signal))
                sendAccuracy = "No signal"
                stayOutSideViews(minutes)
            } else if (accuracy >= JSConstant.accuracyHighStart && accuracy <= JSConstant.accuracyHighEnd) {
                txtAccuracyHome.text = accuracyValue(getString(R.string.high_accuracy))
                sendAccuracy = getString(R.string.high_accuracy)
                moveStage4CheckSatellites(true)
            } else if (accuracy >= JSConstant.accuracyMediumStart && accuracy <= JSConstant.accuracyMediumEnd) {
                txtAccuracyHome.text = accuracyValue(getString(R.string.medium_accuracy))
                sendAccuracy = getString(R.string.medium_accuracy)
                stayOutSideViews(minutes)
            } else if (accuracy >= JSConstant.accuracyLowStart /*&& accuracy <= JSConstant.accuracyLowEnd*/) {
                txtAccuracyHome.text = accuracyValue(getString(R.string.low_accuracy))
                sendAccuracy = getString(R.string.low_accuracy)
                stayOutSideViews(minutes)
            } else if (accuracy >= JSConstant.accuracyLowStart && minutes == 0) {
                Log.e("moveStage4", "===> " + "does not reached accuracy ok")
                moveStage4CheckSatellites(false)
            } else {
                stayOutSideViews(minutes)
            }
        }
    }


    private fun moveStage4CheckSatellites(highAccuracy: Boolean) {
        isDone = false
        if (satelliteModel.totalSatellites == 0) {
            Log.e("moveStage4", "===> totalSatellites 0")
            moveOutside()
        } else {
            if (satelliteModel.useInSatellites < JSConstant.minSatellite) {
                Log.e("moveStage4", "===> go open area ok")
                moveOpenArea()
            } else if (satelliteModel.useInSatellites >= JSConstant.minSatellite) {
                if (highAccuracy) {
                    handlerTimer.removeTimerCallbacks()
                    is10Timer = true
                    highAccuracyViews()
                } else {
                    is10Timer = false
                    cannotAccuracyViews()
                }

                Handler().postDelayed({
                    moveStage5()
                }, waitMilliseconds)
            }
        }
    }


    //move to stage5...
    private fun moveStage5() {
        timerSpentHandler.updateData(JSConstant.JSEVENT_STAGE5)
        isDone = true

        if (is10Timer) {
            timerSpentHandler.updateData(JSConstant.JSEVENT_STAGE_END)
            JSConstant.IS_READY_SHARE = true
            waitingViews()
            Handler().postDelayed({
                Log.e("isReadyToShare", "===> " + " reset")
                isReadyShareViews()
            }, JSConstant.endTimerMillisecondsDelayed)
        }

        btnDataShareHome.setOnClickListener {
            createShareData()
        }
    }


    //generate share data...
    private fun createShareData() {
        if (mCurrentLocation != null /*&& isDone*/) {
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

            //device information data..
            val deviceModel = Utility.getDeviceModel()
            val languageCode = PrefUtil.getStringPref(PrefUtil.PRF_LANGUAGE, mContext)
            val infoData = languageCode + "; " + deviceModel.deviceName + "; " + deviceModel.deviceOsVersion + "; " + timerSpentHandler.sendDataHome()
            val info = "[$infoData]"

            //location data...
            val height = "height:" + altitudeFormat + "m; "
            val satellites = "sat:" + satelliteModel.useInSatellites + "/" + satelliteModel.totalSatellites + "; "
            val accuracy = "acc:" + sendAccuracy + "," + accuracyFormat + "m" + "; "
            val sensor = "sensor:$sensorData"
            val dataValue = " ($height$satellites$accuracy$sensor), $info"

            //urls data...
            val shareUrl1 = "\nGoogle Maps: https://www.google.com/maps/place/$fullCode"
            val shareUrl2 = "\nOpenStreetMap: https://www.openstreetmap.org/#map=14/$latitude/$longitude"
            //val shareUrl3 = "\nMaps.Me: https://ge0.me/$fullCode"
            val shareUrl4 = "\nPlus Codes: https://plus.codes/$fullCode Get SharePlusCode at https://opendeved.net/SharePlusCode"
            val urlsData = shareUrl1 + shareUrl2 + shareUrl4

            val shareData = "SharePlusCode. Your Plus Code is $fullCode $dataValue$urlsData"
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


    //views visibility...
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
        btnTextDataShareHome.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.VISIBLE
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

    private fun stayOutSideViews(minutes: Int) {
        val stayOutsideMessage = String.format(mContext.resources.getString(R.string.stay_outside_minutes), minutes)
        speechMessage = stayOutsideMessage
        speechLoud()
        txtStateWaiting.text = stayOutsideMessage
        txtStateWaiting.visibility = View.VISIBLE
        btnDataShareHome.visibility = View.GONE
        btnRestartHome.visibility = View.GONE
        btnTextDataShareHome.visibility = View.GONE
        btnOutsideHome.visibility = View.GONE
    }


    private fun formatDecimal(value: Float): String {
        return String.format("%.2f", value)
    }


    //text to speech loud message...
    private fun speechLoud() {
        if (speechMessage.isNotEmpty()) {
            if (speechMessage != lastSpeechMessage) {
                sendSpeechLoud()
                lastSpeechMessage = speechMessage
            }
        }
    }

    private val mTextToSpeechListener = OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS) {
            val appLanguage = PrefUtil.getStringPref(PrefUtil.PRF_LANGUAGE, mContext)
            val result: Int = textToSpeech!!.setLanguage(Locale(appLanguage))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("sendSpeechLoud", "msg ===> failed missing")
            } else {
                sendSpeechLoud()
            }
        } else {
            Utility.toastLong(mContext, "Failed to voice initialization")
        }
    }


    private fun sendSpeechLoud() {
        if (speechMessage.isNotEmpty()) {
            Log.e("sendSpeechLoud", "msg ===> $speechMessage")
            if (textToSpeech != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech!!.speak(speechMessage, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    textToSpeech!!.speak(speechMessage, TextToSpeech.QUEUE_FLUSH, null)
                }
            }
        }
    }


    private fun accuracyValue(quality: String): String? {
        return String.format(mContext.resources.getString(R.string.accuracy_value), quality)
    }


    //get satellites data...
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


    private fun showLocationCode(location: Location?) {
        if (location != null) {
            val code = OpenLocationCodeUtil.createOpenLocationCode(location.latitude, location.longitude)
            lastFullCode = code
            fullCode = code.code

            val oneCode = fullCode.substring(0, 4)
            val twoCode = fullCode.substring(4, 8)
            val threeCode = fullCode.substring(8, 12)
            txtOneOLCHome.text = oneCode
            txtTwoOLCHome.text = twoCode
            txtThreeOLCHome.text = threeCode

            val accuracyFormat = formatDecimal(location.accuracy)
            txtDistanceHome.text = accuracyFormat + "m"
        }
    }


    /*override fun updateLocale(locale: Locale) {
        super.updateLocale(locale)
    }*/


    private fun setLocale(activity: Activity, newLocale: Locale) {
        try {
            LocaleHelper.setLocale(activity, newLocale)
            restartActivity()
        } catch (e: Exception) {
            DialogUtils.showExceptionAlert(activity, "Exception : From Locale Delegates", e.message.toString())
        }
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


    override fun onPause() {
        super.onPause()
        //wakeLock.release()
    }


    override fun onStop() {
        super.onStop()
        stoppedLocation()
    }


    private fun stoppedSpeech() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
    }


    private fun stoppedLocation() {
        try {
            Log.e("stoppedLocation ", "===> " + "ok")
            stoppedSpeech()
            baseLocationHelper.disconnectLocation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            handlerTimer.removeTimerCallbacks()
            satelliteTimer.removeSatelliteTimerCallbacks()
            sTimer.removeSatelliteTimerCallbacks()
            startSecondsTimer.removeSecondsTimerCallbacks()
            timerSpentHandler.removeSpentTimerCallbacks()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}