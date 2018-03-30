package pt.isel.pdm.yamda.services

import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.android.volley.VolleyError
import com.firebase.jobdispatcher.*
import needle.Needle
import pt.isel.pdm.yamda.App
import pt.isel.pdm.yamda.R
import pt.isel.pdm.yamda.model.ComingSoonList
import pt.isel.pdm.yamda.model.Movie
import pt.isel.pdm.yamda.model.MoviesList
import pt.isel.pdm.yamda.persistentData.YAMDAContentProvider
import pt.isel.pdm.yamda.persistentData.toContentValues
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class DataUpdateService : JobService(){

    private val map = HashMap<String, Type>()
    init{
        map.put(App.comingSoon,ComingSoonList::class.java)
        map.put(App.nowPlaying, MoviesList::class.java)
    }

    private val notificationsMap = HashMap<String, Int>()
    private val handler = Handler()
    init{
        notificationsMap.put(App.comingSoon,2)
        notificationsMap.put(App.nowPlaying,3)
    }

    private @Volatile var isStopped = false

    override fun onStartJob(job: JobParameters): Boolean {
        val movieListId = job.extras?.get(App.extraMovieListId) as String
        if((application as App).inPowerSaveMode()){
            (application as App).scheduleDataUpdateJob(movieListId, TimeUnit.HOURS.toSeconds(1).toInt())
            return false
        }
        else {
            Log.i("DataUpdateService", "Service started - " + movieListId)
            val listUri = if (movieListId == App.comingSoon) YAMDAContentProvider.COMING_SOON_CONTENT_URI
                          else YAMDAContentProvider.NOW_PLAYING_CONTENT_URI
            Needle.onBackgroundThread().execute {
                contentResolver.delete(listUri, null, null)
                updateReplica(job, 1, movieListId)
            }
        }
        return true
    }

    override fun onStopJob(job: JobParameters): Boolean {
        val movieListId = job.extras?.get(App.extraMovieListId) as String?
        val listUri = if (movieListId == App.comingSoon) YAMDAContentProvider.COMING_SOON_CONTENT_URI
                    else YAMDAContentProvider.NOW_PLAYING_CONTENT_URI
        isStopped = true
        Needle.onBackgroundThread().execute {
            contentResolver.delete(listUri, null, null)
        }
        return true
    }

    private fun updateReplica(job: JobParameters, page: Int, movieListId: String){
        Log.i("Replica", "Getting page for $movieListId (page $page)")

        val paramsMap = HashMap<String, String>()
        paramsMap.put("region", App.getRegion(resources))
        paramsMap.put("language", App.getLanguage(resources))
        paramsMap.put("page", page.toString())
        App.getProvider().provide(movieListId,
                paramsMap,
                { movieListResponseCallback(it as MoviesList, paramsMap, job, movieListId)},
                { handleError(it); jobFinished(job, true)},
                MoviesList::class.java
        )
    }

    private fun movieListResponseCallback(movieList: MoviesList, paramsMap: HashMap<String, String>, job: JobParameters, movieListId: String) {
        processMovieList(movieList,movieListId)
        paramsMap.remove("page")
        movieList.results.forEach {
            App.getProvider().provide("movie/${it.id}",
                    paramsMap,
                    {processMovie(it as Movie,movieListId)},
                    {handleError(it); jobFinished(job,true)},
                    Movie::class.java
            )
        }
        if(movieList.page > movieList.total_pages){
            Log.i("Replica", "No more pages for $movieListId")
            notifyUpdated(movieListId)
            jobFinished(job, false)
            (application as App).rescheduleDataUpdateJob(movieListId)
        }
        else{
            //schedule update of next page to 10 seconds from now, due to api request limits
            handler.postDelayed(
                    {
                        if(!isStopped)
                            updateReplica(job,movieList.page + 1,movieListId)
                    },
                    10000
            )
        }
    }

    private fun handleError(error : VolleyError) {
        Log.e("DataUpdateService","Error on updating data")
    }

    private fun processMovieList(movieList: MoviesList, movieListId: String){
        if(movieList.results.isEmpty())
            return
        val tableUri =
                if (movieListId == App.comingSoon) YAMDAContentProvider.COMING_SOON_CONTENT_URI
                else YAMDAContentProvider.NOW_PLAYING_CONTENT_URI
        if(!isStopped)
            contentResolver.bulkInsert(tableUri,movieList.toContentValues())
    }

    private fun processMovie(movie: Movie, movieListId: String) {
        val tableUri =
                if (movieListId == App.comingSoon) YAMDAContentProvider.COMING_SOON_CONTENT_URI
                else YAMDAContentProvider.NOW_PLAYING_CONTENT_URI
        if(!isStopped)
            contentResolver.update(tableUri, movie.toContentValues(),"id = ${movie.id}",null)
    }

    private fun notifyUpdated(movieListId: String) {
        Log.v("DataUpdateService", "Successfully updated $movieListId movie list")
        val notificationBuilder = NotificationCompat.Builder(applicationContext, (1000 + notificationsMap[movieListId]!!).toString())
        val notificationStyle = NotificationCompat.InboxStyle()
        notificationBuilder.setSmallIcon(R.drawable.yamda_logo)
                .setStyle(notificationStyle)
                .setContentTitle("Updated Data - {$movieListId}")
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setLights(Color.RED, 3000, 3000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(notificationsMap[movieListId]!!, notificationBuilder.build())
    }

}
