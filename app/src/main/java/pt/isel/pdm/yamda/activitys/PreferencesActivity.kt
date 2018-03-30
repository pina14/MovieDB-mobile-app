package pt.isel.pdm.yamda.activitys

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import pt.isel.pdm.yamda.PreferencesFragment

class PreferencesActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, PreferencesFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId
        if(id == android.R.id.home){
            this@PreferencesActivity.finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
