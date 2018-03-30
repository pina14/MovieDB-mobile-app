package pt.isel.pdm.yamda.activitys

import android.support.v4.content.CursorLoader
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.util.Log
import android.widget.Toast
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.content_movie_list.*
import pt.isel.pdm.yamda.App
import pt.isel.pdm.yamda.EndlessScrollerListener
import pt.isel.pdm.yamda.MovieListAdapter
import pt.isel.pdm.yamda.R
import pt.isel.pdm.yamda.model.ComingSoonList
import pt.isel.pdm.yamda.model.MoviesList
import pt.isel.pdm.yamda.persistentData.YAMDAContentProvider
import pt.isel.pdm.yamda.persistentData.toMovieList

class MovieListActivity : DrawerActivity(), NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>{

    private lateinit var path : String
    private lateinit var parametersMap : HashMap<String,String>
    private val typeListMap = HashMap<String, Uri>()
    init {
        typeListMap[App.nowPlaying] = YAMDAContentProvider.NOW_PLAYING_CONTENT_URI
        typeListMap[App.comingSoon] = YAMDAContentProvider.COMING_SOON_CONTENT_URI
    }

    private lateinit var listClass : Class<*>
    private var currMovieListAdapter : MovieListAdapter? = null
    private var inDB = true

    /**
     * Shows movie information with data provided by request response
     */
    fun requestCallback(movieList: MoviesList?) {

        val movieListView =  movies_list

        if(movieList!!.results.isEmpty()){
            movie_list_empty.text = getString(R.string.movie_list_empty)
            return
        }

        //create ArrayAdapter for ListView and set adapter
        if(currMovieListAdapter == null) {
            currMovieListAdapter = MovieListAdapter(this, R.layout.movie_list_item, movieList)
            movieListView.adapter = currMovieListAdapter
            movieListView.setOnItemClickListener { _, _, position, _ ->
                val intent = Intent(this, MovieActivity::class.java)
                intent.putExtra(App.extraMovieId, currMovieListAdapter!!.getItem(position).id)
                intent.putExtra(App.extraInDatabase, inDB)
                intent.putExtra(App.extraListType,when(path){
                    App.comingSoon -> YAMDAContentProvider.COMING_SOON_CONTENT_URI
                    App.nowPlaying -> YAMDAContentProvider.NOW_PLAYING_CONTENT_URI
                    else -> null
                })
                startActivity(intent)
            }
            if(movieList.total_pages > 1)
                movieListView.setOnScrollListener(object : EndlessScrollerListener(movieList.total_pages){

                    override fun onLoadMore(page: Int, totalItemsCount: Int): Boolean {
                        Log.i("MOVIE_LIST","IN $page PAGE")
                        parametersMap["page"] = (page).toString()
                        App.getProvider().provide(path, parametersMap, { requestCallback(it as MoviesList)},{ errorCallback(it)}, listClass)
                        return true
                    }
                })
        }
        else {
            currMovieListAdapter!!.movies?.results?.addAll(movieList.results)
            currMovieListAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * Shows error message when occurs an error in request
     */
    fun errorCallback(error: VolleyError?) {
        Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
        finish()
        val intent = Intent(this,SearchActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) { // 20 movies
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        super.createDrawer()

        //get info passed in intent
        val activityTitle = intent?.extras?.getString(App.activityName)
        path = intent?.extras?.getString(App.pathName)!!
        val movieName = (intent?.extras?.getString(App.extraMovieName)?: "")
        listClass = (intent?.extras?.get(App.listClassType) ?: "") as Class<*>

        if(activityTitle != null)
            supportActionBar!!.title = activityTitle

        //Init map with parameters
        parametersMap = HashMap()

        parametersMap.put("region", App.getRegion(resources))
        parametersMap.put("language", App.getLanguage(resources))
        if(movieName != "")
            parametersMap.put("query", movieName)
        parametersMap.put("page","1")

        if(!typeListMap.containsKey(path)) {
            Log.i("Request", "Doing network request")
            App.getProvider().provide(path, parametersMap, { requestCallback(it as MoviesList) }, { errorCallback(it) }, listClass)
        }else
            supportLoaderManager.initLoader(1, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val contentTypeURI = typeListMap[path]
        return CursorLoader(this, contentTypeURI, arrayOf("*"), null, null, null)
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        return
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        val movies = data?.toMovieList()!!
        if(movies.isEmpty()){
            inDB = false
            Log.i("Request", "Replica not made. Doing network request.")
            App.getProvider().provide(path, parametersMap, { requestCallback(it as MoviesList) }, { errorCallback(it) }, listClass)
            return
        }

        val movieList = if(listClass == ComingSoonList::class.java)
                            ComingSoonList(movies,-1,1)
                        else
                            MoviesList(movies,-1,1)

        requestCallback(movieList)
    }
}
