package pt.isel.pdm.yamda.activitys

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import kotlinx.android.synthetic.main.content_search.*
import pt.isel.pdm.yamda.App
import pt.isel.pdm.yamda.R
import pt.isel.pdm.yamda.model.MoviesList

class SearchActivity : DrawerActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val errorText = "Search Box must not be empty!!!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        super.createDrawer()

        searchButton.setOnClickListener{
            val movieName = name_text_view.text.toString()
            if(movieName.isEmpty()){
                name_text_view.error = errorText
            }else {
                val intent = Intent(this@SearchActivity, MovieListActivity::class.java)
                intent.putExtra(App.extraMovieName, movieName)
                intent.putExtra(App.pathName, App.searchMovie)
                intent.putExtra(App.listClassType, MoviesList::class.java)
                startActivity(intent)
            }
        }
    }
}
