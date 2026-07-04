package com.example.fridgeproject.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fridgeproject.R
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.net.toUri
import com.example.fridgeproject.FridgeApplication

class FridgeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = SessionManagerFacade.currentUserId ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("fcmToken", token)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val type = data["type"] ?: return
        val notificationId = data["notificationId"] ?: return
        val userId = data["userId"] ?: return

        if (!isAppInForeground()){
        showSystemNotification(type, notificationId, userId)
        }
    }
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        return appProcesses.any {
            it.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && it.processName == packageName
        }
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showSystemNotification(type: String, notificationId: String, userId: String) {
        val deepLinkUri = buildDeepLinkUri(userId)

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, FridgeApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setContentTitle(titleForType(type))
            .setContentText(bodyForType(type))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this)
            .notify(notificationId.hashCode(), notification)
    }

    private fun buildDeepLinkUri(userId: String): Uri {
        return "fridgelab://notifications/$userId".toUri()
    }

    private fun titleForType(type: String): String = when (type) {
        "LIKE" -> "NEW LIKE"
        "REVIEW" -> "NEW REVIEW!"
        "RECIPE_DUPLICATE" -> "REMIXED RECIPE"
        "NEW_RECIPE" -> "NEW RECIPE"
        "TIP" -> "NEW TIP"
        else -> "NEW NOTIFICATION"
    }

    private fun bodyForType(type: String): String = when (type) {
        "LIKE" -> "Someone liked one of your recipes!"
        "REVIEW" -> "Someone added a new review for one of your recipes!"
        "RECIPE_DUPLICATE" -> "Someone remixed one of your recipes"
        "NEW_RECIPE" -> "Someone posted a recipe you might like!"
        "TIP" -> "Someone added a new tip for one of your recipes!"
        else -> "You have a new notification!"
    }

}