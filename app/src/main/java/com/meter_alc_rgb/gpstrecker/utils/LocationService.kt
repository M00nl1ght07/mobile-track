package com.meter_alc_rgb.gpstrecker.utils

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.meter_alc_rgb.gpstrecker.R
import com.meter_alc_rgb.gpstrecker.main.MainActivity
import org.osmdroid.util.GeoPoint

class LocationService : Service() {
    private var distance = 0.0f
    private var lastLocation: Location? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var pLine: ArrayList<GeoPoint> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        initData()
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation: Location = locationResult.lastLocation!!

            if (lastLocation != null) {
                pLine.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))
                val d: Float
                if (currentLocation.speed > 0.2) {
                    d = lastLocation?.distanceTo(currentLocation) ?: 0.0f
                    if(d != 0.0f) distance += d
                }

                val dataModel = LocationModel(
                    currentLocation.speed,
                    distance,
                    currentLocation.accuracy,
                    pLine
                )

                sendLocationData(dataModel)
                Log.d("MyLog", "Location received")
            }
            lastLocation = currentLocation
        }
    }

    private fun sendLocationData(dataModel: LocationModel){
        val i = Intent(GPS_LOCATION_INTENT)
        i.putExtra(LOCATION_INTENT, dataModel)
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(i)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        prepareForegroundNotification()
        startLocationUpdates()
        startTime = intent?.getLongExtra("start_time", 0) ?: 0
        Log.d("MyLog","Start Time after open service: $startTime")
        isRunning = true
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Looper.myLooper()?.let {
            mFusedLocationClient?.requestLocationUpdates(
                locationRequest!!,
                locationCallback, it
            )
        }
    }

    private fun prepareForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                AppConstants.CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            AppConstants.SERVICE_LOCATION_REQUEST_CODE,
            notificationIntent, 0
        )
        val notification: Notification = NotificationCompat.Builder(
            this, AppConstants.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracker")
            .setContentTitle("Location on background")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(AppConstants.LOCATION_SERVICE_NOTIF_ID, notification)
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationClient?.removeLocationUpdates(locationCallback)
        stopForeground(true)
        isRunning = false
        startTime = 0L
    }

    private fun initData() {
        val interval = PreferenceManager.getDefaultSharedPreferences(
            this
        ).getString("update_time_key", "3000"
        )?.toLong() ?: 3000
        locationRequest = LocationRequest.create()
        Log.d("MyLog","Update interval: $interval")
        locationRequest?.interval = interval
        locationRequest?.priority = PRIORITY_HIGH_ACCURACY
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(baseContext)
    }

    companion object {
        var isRunning = false
        var startTime = 0L
        //region data
        const val LOCATION_INTENT = "location_intent"
        const val GPS_LOCATION_INTENT = "gps_location_intent"
    }
}