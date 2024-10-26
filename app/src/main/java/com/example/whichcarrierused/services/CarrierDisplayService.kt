package com.example.whichcarrierused.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
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
        try {
            if (ActivityCompat.checkSelfPermission(this, 
                android.Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                
                val defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
                val activeSubscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(defaultDataSubId)
                
                val carrierName = activeSubscriptionInfo?.carrierName ?: 
                                 telephonyManager.simOperatorName ?: 
                                 getString(R.string.unknown_carrier)
                
                showCarrierNotification(carrierName.toString())
            }
        } catch (e: Exception) {
            Log.e("CarrierDisplayService", "Error displaying carrier info", e)
            showCarrierNotification(getString(R.string.error_getting_carrier))
        }
    }

    private fun getCarrierIcon(carrierName: String): Int {
        val firstChar = carrierName.firstOrNull()?.uppercaseChar() ?: return R.drawable.ic_network
        
        return when (firstChar) {
            'A' -> R.drawable.ic_carrier_a  // au, ASUS等
            'D' -> R.drawable.ic_carrier_d  // docomo等
            'I' -> R.drawable.ic_carrier_i  // IIJmobile等
            'K' -> R.drawable.ic_carrier_k  // KDDI等
            'L' -> R.drawable.ic_carrier_l  // Linemo等
            'P' -> R.drawable.ic_carrier_p  // povo等
            'R' -> R.drawable.ic_carrier_r  // Rakuten等
            'S' -> R.drawable.ic_carrier_s  // SoftBank等
            'T' -> R.drawable.ic_carrier_t  // T-Mobile等
            'U' -> R.drawable.ic_carrier_u  // UQ mobile等
            'V' -> R.drawable.ic_carrier_v  // Vodafone等
            'Y' -> R.drawable.ic_carrier_y  // Y!mobile等
            else -> R.drawable.ic_network   // その他
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
        try {
            val iconResId = getCarrierIcon(carrierName)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(iconResId)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(carrierName)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("CarrierDisplayService", "Error showing notification", e)
            // エラー時はデフォルトアイコンを使用
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_network)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(carrierName)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}