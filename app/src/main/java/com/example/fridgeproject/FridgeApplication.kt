package com.example.fridgeproject

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.fridgeproject.data.AppContainer
import com.example.fridgeproject.data.DefaultAppContainer

class FridgeApplication : Application() {
    val container: AppContainer by lazy { DefaultAppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FridgeLab Push Notification",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Push Notification for user events"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "fridge_notifications"
    }
}