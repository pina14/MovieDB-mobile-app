package pt.isel.pdm.yamda.activitys

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.content_movie.*
import pt.isel.pdm.yamda.App
import pt.isel.pdm.yamda.model.Movie
import pt.isel.pdm.yamda.DownloadImageTask
import pt.isel.pdm.yamda.R
import pt.isel.pdm.yamda.persistentData.AccessDB
import pt.isel.pdm.yamda.persistentData.YAMDAContentProvider

class MovieActivity : DrawerActivity(), NavigationView.OnNavigationItemSelectedListener{

    /**
     * Shows movie information with data provided by request response
     */
    fun requestCallback(movie: Movie?) {
        if (movie != null) {
            movieNameTextView.text = movie.original_title
            DownloadImageTask.execute(null, movie.poster_path, movieImageView, this)
            ratingTextView.append(movie.vote_average.toString())

            val arrayAdapter = ArrayAdapter<Movie.Genre>(this,R.layout.genre_list_item,movie.genres)
            genresListView.adapter = arrayAdapter

            durationTextView.append(Integer.toString(movie.runtime)+" m")
            releaseDateTextView.append(movie.release_date)
            descriptionTextView.append(movie.overview)
            descriptionTextView.movementMethod = ScrollingMovementMethod()
            if(this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
                descriptionTextView.maxLines = 3
            }
        }
    }

    /**
     * Shows error message when occurs an error in request
     */
    fun errorCallback(error: VolleyError) {
        Log.e("MovieActivity", error.message)
        Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
        finish()
        val intent = Intent(this,SearchActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)
        super.createDrawer()

        val movieId = intent?.extras?.get(App.extraMovieId) as String
        val inDB = intent?.extras?.get(App.extraInDatabase) as Boolean
        val listType = intent?.extras?.get(App.extraListType) as Uri?

        //Init map with parameters
        val map = HashMap<String, String>()
        map.put("region", App.getRegion(resources))
        map.put("language", App.getLanguage(resources))

        if(!inDB || listType == null)
            App.getProvider().provide("movie/$movieId", map, { requestCallback(it as Movie)},{ errorCallback(it)}, Movie::class.java)
        else{
            AccessDB.getMovie(contentResolver, listType, arrayOf("*"), "id=$movieId", null, null){
                requestCallback(it)
            }
        }
    }
}