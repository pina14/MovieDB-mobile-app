package pt.isel.pdm.yamda.activitys

import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_movie.*
import kotlinx.android.synthetic.main.app_bar_movie.*
import pt.isel.pdm.yamda.App
import pt.isel.pdm.yamda.R
import pt.isel.pdm.yamda.R.string.*
import pt.isel.pdm.yamda.model.ComingSoonList
import pt.isel.pdm.yamda.model.MoviesList

open class DrawerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    fun createDrawer(){
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout!!.addDrawerListener(toggle)
        toggle.syncState()

        nav_view!!.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout!!.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Inflate the menu
     * Adds items to the action bar if it is present.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.drawer, menu)
        return true
    }

    /**
     * Define action for credits option
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        when(id){
            R.id.menu_credits -> {
                val intent = Intent(this, CreditsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_preferences -> {
                val intent = Intent(this, PreferencesActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     *  Define action for each menu option
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        val intent:Intent

        when (id) {
            R.id.nav_search_movie -> {
                intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_now_playing -> {
                intent = Intent(this, MovieListActivity::class.java)
                intent.putExtra(App.pathName, App.nowPlaying)
                intent.putExtra(App.activityName, getString(now_playing))
                intent.putExtra(App.listClassType, MoviesList::class.java)
                startActivity(intent)
            }
            R.id.nav_coming_soon -> {
                intent = Intent(this, MovieListActivity::class.java)
                intent.putExtra(App.pathName, App.comingSoon)
                intent.putExtra(App.activityName, getString(coming_soon))
                intent.putExtra(App.listClassType, ComingSoonList::class.java)
                startActivity(intent)
            }
            R.id.nav_popular_movies -> {
                intent = Intent(this, MovieListActivity::class.java)
                intent.putExtra(App.pathName, App.popularMovies)
                intent.putExtra(App.activityName, getString(popular_movies))
                intent.putExtra(App.listClassType, MoviesList::class.java)
                startActivity(intent)
            }
        }

        drawer_layout!!.closeDrawer(GravityCompat.START)
        return true
    }
}
