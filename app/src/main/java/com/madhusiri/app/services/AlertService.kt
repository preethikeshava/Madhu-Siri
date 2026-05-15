package com.madhusiri.app.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AlertService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val database = FirebaseDatabase.getInstance().getReference("beekeepers")
    
    companion object {
        const val RADIUS_KM = 2.0
        const val CHANNEL_ID = "alert_channel"
        const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlertService", "Service started, checking for beekeepers...")
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Madhu-Siri Active")
            .setContentText("Monitoring location for spraying alerts.")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
            
        startForeground(102, notification)
        
        checkLocationAndTriggerAlerts()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun checkLocationAndTriggerAlerts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AlertService", "Location permission not granted")
            stopSelf()
            return
        }

        // Optimize Battery Usage: Use Balanced Power Accuracy with a 5-minute interval
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            5 * 60 * 1000L // 5 minutes
        ).apply {
            setMinUpdateIntervalMillis(2 * 60 * 1000L) // 2 minutes fastest
            setMaxUpdateDelayMillis(10 * 60 * 1000L)
        }.build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let { location ->
                    fetchBeekeepersAndCompare(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            android.os.Looper.getMainLooper()
        )
    }

    private fun fetchBeekeepersAndCompare(farmerLocation: Location) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var alertTriggered = false
                
                for (beekeeperSnapshot in snapshot.children) {
                    val lat = beekeeperSnapshot.child("latitude").getValue(Double::class.java)
                    val lng = beekeeperSnapshot.child("longitude").getValue(Double::class.java)
                    
                    if (lat != null && lng != null) {
                        val distance = calculateHaversineDistance(
                            farmerLocation.latitude, farmerLocation.longitude,
                            lat, lng
                        )
                        
                        if (distance <= RADIUS_KM) {
                            alertTriggered = true
                            break
                        }
                    }
                }
                
                if (alertTriggered) {
                    sendHighPriorityNotification()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AlertService", "Database error: ${error.message}")
            }
        })
    }

    /**
     * Calculates the distance between two points in kilometers using the Haversine formula.
     */
    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of the Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun sendHighPriorityNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Spraying Alert Sent")
            .setContentText("Beekeepers within 2km have been notified!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Spraying Alerts"
            val descriptionText = "Notifications for nearby spraying activity"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
