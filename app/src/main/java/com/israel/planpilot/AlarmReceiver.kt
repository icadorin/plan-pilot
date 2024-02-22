package com.israel.planpilot

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.UUID

class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val activityId = UUID.fromString(intent.getStringExtra("activity_id"))
        val activityRepository = ActivityRepository(context)
        val activity = activityRepository.readActivity(activityId)

        val name = "Canal"
        val descriptionText = "Canal de notificação"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel("canal", name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val builder = NotificationCompat.Builder(context, "canal")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Alarme")
            .setContentText("O alarme para a atividade '${activity?.name}' disparou!")
            .setSound(alarmUri)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }
    }
}
