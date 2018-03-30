package pt.isel.pdm.yamda.services

import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import needle.Needle
import pt.isel.pdm.yamda.App
import pt.isel.pdm.yamda.R
import pt.isel.pdm.yamda.persistentData.AccessDB
import pt.isel.pdm.yamda.model.Movie
import pt.isel.pdm.yamda.persistentData.YAMDAContentProvider
import java.text.SimpleDateFormat
import java.util.*

class MovieNotifyJobService : JobService() {

    private val format = SimpleDateFormat("yyyy-MM-dd")
    private var notificationBuilder : NotificationCompat.Builder? = null
    private var notificationStyle : NotificationCompat.InboxStyle? = null
    private val notificationID = 0

    private @Volatile var isStopped = false

    override fun onStartJob(job: JobParameters): Boolean {
        val notificationsEnabled = App.getSharedPrefs().getBoolean("receive_notification_switch",false)
        if(!notificationsEnabled){
            return false
        }
        Needle.onBackgroundThread().execute{
            notificationBuilder = NotificationCompat.Builder(applicationContext, "1001")
            notificationStyle = NotificationCompat.InboxStyle()
            val notifyMovies = arrayListOf<Movie>()

            AccessDB.getMovies(contentResolver, YAMDAContentProvider.FOLLOWING_CONTENT_URI, arrayOf("*"),
                               null, null, null, null) {
                run loop@{
                    it.results.forEach {
                        val movieDate = format.parse(it.release_date)
                        if (movieDate.before(Calendar.getInstance().time)) {
                            notificationStyle!!.addLine(it.original_title)
                            if (isStopped) {
                                return@loop
                            }
                            AccessDB.deleteMovie(contentResolver, YAMDAContentProvider.FOLLOWING_CONTENT_URI,
                                    "${YAMDAContentProvider.COLUMN_ID}=${it.id}", null)
                            notifyMovies.add(it)
                        }
                    }
                }
                if(notifyMovies.count() > 0) {
                    notificationBuilder!!.setSmallIcon(R.drawable.yamda_logo)
                            .setStyle(notificationStyle)
                            .setContentTitle("Movies Released")
                            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                            .setLights(Color.RED, 3000, 3000)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

                    val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    mNotifyMgr.notify(notificationID, notificationBuilder!!.build())
                }
                jobFinished(job, false)
            }
        }
        return true
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        isStopped = true
        return true
    }

}
