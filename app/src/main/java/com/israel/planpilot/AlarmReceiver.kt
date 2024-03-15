package com.israel.planpilot

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id")
        val activityName = intent.getStringExtra("activity_name")
        val alarmTime = intent.getStringExtra("alarm_time")
        val alarmTone = intent.getStringExtra("alarm_tone")

        val channel = NotificationChannel(
            "alarm_channel",
            "Canal notificação de alarme",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Canal notificação de alarme"
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        if (intent.action == "stop_alarm") {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            notificationManager.cancel(alarmId.hashCode())
        } else {

            val alarmUri: Uri = if (alarmTone.isNullOrEmpty()) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } else {
                Uri.parse(alarmTone)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
            }

            val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "stop_alarm"
                putExtra("alarm_id", alarmId)
            }

            val stopPendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Alarme")
                .setContentText("$activityName, $alarmTime")
                .setSound(alarmUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDeleteIntent(stopPendingIntent)

            builder.addAction(R.drawable.ic_notification, "Cancelar", stopPendingIntent)

            with(NotificationManagerCompat.from(context)) {
                notify(alarmId.hashCode(), builder.build())
            }
        }
    }
}
