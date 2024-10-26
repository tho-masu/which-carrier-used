package com.example.whichcarrierused.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.whichcarrierused.R

class CarrierDisplayService : Service() {
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var telephonyManager: TelephonyManager
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "carrier_info"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        displayDefaultDataCarrier()
    }

    private fun displayDefaultDataCarrier() {
        if (ActivityCompat.checkSelfPermission(this, 
            android.Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            
            val defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
            val activeSubscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(defaultDataSubId)
            
            // キャリア名を取得（Android 13以降の方法）
            val carrierName = activeSubscriptionInfo?.carrierName ?: telephonyManager.simOperatorName
            
            showCarrierNotification(carrierName?.toString() ?: "不明")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_description)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun showCarrierNotification(carrierName: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_network)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(carrierName)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}