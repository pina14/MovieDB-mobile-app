package pt.isel.pdm.yamda

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.BatteryManager
import android.preference.PreferenceManager

import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.firebase.jobdispatcher.*
import pt.isel.pdm.yamda.services.DataUpdateService
import pt.isel.pdm.yamda.services.MovieNotifyJobService
import pt.isel.pdm.yamda.providers.MovieProvider
import pt.isel.pdm.yamda.providers.RealProvider
import java.util.concurrent.TimeUnit
import android.os.Bundle


/**
 * Context of the application. All the information used on the application is stored in this class
 */
class App : Application() {

    companion object {

        fun getProvider() : MovieProvider = provider

        fun getQueue() = requestQueue

        fun getSharedPrefs() = sharedPreferences

        fun getJobDispatcher() = firebaseJobDispatcher

        /**
         * Gets the system region to be used on the request
         */
        fun getRegion(res: Resources): String = res.configuration.locale.country

        /**
         * Gets the system language to be used on the request
         */
        fun getLanguage(res: Resources): String = res.configuration.locale.language

        fun getDefaultImageURL() = "http://www.free-icons-download.net/images/movie-icon-72062.png"

        private @Volatile lateinit var requestQueue : RequestQueue
        private @Volatile var provider = RealProvider()
        private lateinit var firebaseJobDispatcher : FirebaseJobDispatcher
        private lateinit var sharedPreferences: SharedPreferences
        val listClassType = "class-type"
        val extraMovieName = "movieName"
        val extraMovieId = "id"
        val extraInDatabase = "inDB"
        val extraListType = "listType"
        val pathName = "path"
        val searchMovie = "search/movie"
        val nowPlaying = "movie/now_playing"
        val comingSoon = "movie/upcoming"
        val popularMovies = "movie/popular"
        val activityName = "activityName"
        val preferencesFile = "preferences.xml"
        val updateDataJobTag = "data-update-job-"
        val notificationJobTag = "notification-job"
        val extraMovieListId = "listId"
        val extraIsFirstUpdate = "isFirstUpdate"
    }

    /**
     * Initializes the Application
     */
    override fun onCreate() {
        super.onCreate()
        requestQueue = Volley.newRequestQueue(this@App)

        sharedPreferences = getSharedPreferences(preferencesFile,0)
		PreferenceManager.setDefaultValues(this@App, preferencesFile,0,R.xml.preferences,false)

        firebaseJobDispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        val myJob = firebaseJobDispatcher.newJobBuilder()
                .setService(MovieNotifyJobService::class.java)
                .setTag(notificationJobTag)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(0, TimeUnit.DAYS.toSeconds(1).toInt()))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build()
        firebaseJobDispatcher.mustSchedule(myJob)
        scheduleDataUpdateJob(nowPlaying, 0)
        scheduleDataUpdateJob(comingSoon, 0)
    }

    fun scheduleDataUpdateJob(listId : String, timeToSchedule: Int) {
        val extras = Bundle()
        extras.putString(extraMovieListId, listId)
        val toleranceInterval = TimeUnit.MINUTES.toSeconds(30).toInt()
        val connectivityPref = App.getSharedPrefs().getString("update_connectivity_list","0").toInt()
        val updateDataJob = firebaseJobDispatcher.newJobBuilder()
                .setService(DataUpdateService::class.java)
                .setTag(updateDataJobTag+listId)
                .setRecurring(false)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(timeToSchedule, timeToSchedule + toleranceInterval))
                .setReplaceCurrent(false)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(
                    connectivityPref
                )
                .setExtras(extras)
                .build()
        firebaseJobDispatcher.mustSchedule(updateDataJob)
    }

    fun rescheduleDataUpdateJob(listId : String) {
        val extras = Bundle()
        extras.putString(extraMovieListId, listId)
        extras.putBoolean(extraIsFirstUpdate,false)
        val toleranceInterval = TimeUnit.MINUTES.toSeconds(30).toInt()
        val connectivityPref = App.getSharedPrefs().getString("update_connectivity_list","0").toInt()
        val frequencyPref = TimeUnit.DAYS.toSeconds(App.getSharedPrefs().getString("update_frequency_list","0").toLong()).toInt()
        val updateDataJob = App.getJobDispatcher().newJobBuilder()
                .setService(DataUpdateService::class.java)
                .setTag(App.updateDataJobTag+listId)
                .setRecurring(false)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(frequencyPref,frequencyPref+toleranceInterval))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(
                        connectivityPref
                )
                .setExtras(extras)
                .build()
        App.getJobDispatcher().mustSchedule(updateDataJob)
    }

    fun inPowerSaveMode(): Boolean {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, iFilter)
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        return level < App.getSharedPrefs().getString("power_saving","0").toInt()
    }
}