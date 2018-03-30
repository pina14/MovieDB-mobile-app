package pt.isel.pdm.yamda

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference

class PreferencesFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //add xml
        addPreferencesFromResource(R.xml.preferences)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val pref = findPreference(key)
        val modifiedPref = sharedPreferences!!.all[key]
        if(pref is ListPreference)
            changeListPreference(key,modifiedPref as String)
        else if(pref is SwitchPreference)
            changeSwitchPreference(key,modifiedPref as Boolean)
    }

    private fun changeSwitchPreference(key: String?, modifiedPref: Boolean){
        App.getSharedPrefs().edit().putBoolean(key, modifiedPref).apply()
    }

    private fun changeListPreference(key: String?, modifiedPref: String){
        App.getSharedPrefs().edit().putString(key, modifiedPref).apply()
    }

}