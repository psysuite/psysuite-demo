package iit.uvip.psysuite.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import iit.uvip.psysuite.R

//class SettingsActivity  : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback  {
class SettingsActivity  : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_layout, SettingsFragment())
            .commit()
    }

    // https://eng-nohasamirsaad.medium.com/setting-preference-summary-ebc4aab4ccfa
//    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
//        // Instantiate the new Fragment
//        val args = pref.extras
//        val fragment = supportFragmentManager.fragmentFactory.instantiate(
//            classLoader,
//            pref.fragment)
//        fragment.arguments = args
//        fragment.setTargetFragment(caller, 0)
//        // Replace the existing Fragment with the new Fragment
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.settings_layout, fragment)
//            .addToBackStack(null)
//            .commit()
//        return true
//    }
}
