package com.example.skillbridge.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.skillbridge.data.SkillBridgeRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * WorkerLocationForegroundService:
 * Transmits live GPS telemetry exclusively when a worker is Online or on an active job assignment.
 * Adapts interval dynamically (5s en-route vs. 15s idle). Terminates automatically on Offline toggle or job end.
 */
class WorkerLocationForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var repository: SkillBridgeRepository? = null

    private var isActiveJobMode: Boolean = false

    companion object {
        const val CHANNEL_ID = "skillbridge_worker_dispatch_channel"
        const val NOTIFICATION_ID = 2026
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        const val EXTRA_ACTIVE_JOB = "EXTRA_ACTIVE_JOB"
        const val EXTRA_WORKER_ID = "EXTRA_WORKER_ID"

        fun startService(context: Context, workerId: String, hasActiveJob: Boolean) {
            val intent = Intent(context, WorkerLocationForegroundService::class.java).apply {
                action = ACTION_START_TRACKING
                putExtra(EXTRA_WORKER_ID, workerId)
                putExtra(EXTRA_ACTIVE_JOB, hasActiveJob)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WorkerLocationForegroundService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = SkillBridgeRepository.getInstance(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                onNewLocation(location)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                isActiveJobMode = intent.getBooleanExtra(EXTRA_ACTIVE_JOB, false)
                val workerId = intent.getStringExtra(EXTRA_WORKER_ID) ?: ""
                startForeground(NOTIFICATION_ID, buildForegroundNotification(isActiveJobMode))
                requestLocationUpdates(isActiveJobMode)
                Log.i("WorkerLocationService", "Started location tracking for worker $workerId (activeJob=$isActiveJobMode)")
            }
            ACTION_STOP_TRACKING -> {
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                Log.i("WorkerLocationService", "Stopped worker location service")
            }
        }
        return START_NOT_STICKY
    }

    private fun requestLocationUpdates(activeJob: Boolean) {
        val intervalMs = if (activeJob) 5000L else 15000L
        val minUpdateDistanceMeters = if (activeJob) 5f else 15f

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMinUpdateDistanceMeters(minUpdateDistanceMeters)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("WorkerLocationService", "Location permission missing: ${e.message}")
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.w("WorkerLocationService", "Error removing updates: ${e.message}")
        }
    }

    private fun onNewLocation(location: Location) {
        serviceScope.launch {
            try {
                repository?.updateWorkerLiveCoordinates(location.latitude, location.longitude)
            } catch (e: Exception) {
                Log.e("WorkerLocationService", "Failed to transmit telemetry: ${e.message}")
            }
        }
    }

    private fun buildForegroundNotification(activeJob: Boolean): Notification {
        val contentText = if (activeJob) {
            "Active Job Dispatch: Sharing live route location with Task-Poster"
        } else {
            "Online: Sharing availability location with nearby Task-Posters"
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SkillBridge Worker Dispatch Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SkillBridge Worker Location Dispatch",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used while worker is online to receive nearby service requests and live tracking"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
