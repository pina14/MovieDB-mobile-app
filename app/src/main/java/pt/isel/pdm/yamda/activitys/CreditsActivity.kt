package pt.isel.pdm.yamda.activitys

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_credits.*
import pt.isel.pdm.yamda.R

/**
 * Activity used to show the credits of the application to the user
 */
class CreditsActivity : AppCompatActivity() {

    private val tmdbURL = "https://www.themoviedb.org/"
    private val iconsURL = "https://icons8.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tmdb_image_view.setOnClickListener{
            callBrowser(tmdbURL)
        }

        icons_image_view.setOnClickListener{
            callBrowser(iconsURL)
        }

        icons_text_view.setOnClickListener{
            callBrowser(iconsURL)
        }
    }

    private fun callBrowser(url: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId
        if(id == android.R.id.home){
            this@CreditsActivity.finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
