package iit.uvip.psysuite.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import iit.uvip.psysuite.R

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

//    var pref_delay_a1: EditTextPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

//        val preferenceManager = preferenceManager
//        preferenceManager.preferenceDataStore = dataStore



//        pref_delay_a1 = findPreference("pref_delay_a1")
//        pref_delay_a1?.setOnPreferenceChangeListener { preference, newValue ->
//            if(newValue.toString().toInt() < 0) {
//                Toast.makeText(context, "Audio delay cannot be less than zero. $newValue", Toast.LENGTH_LONG).show()
//                false
//            }
//            else true
//        }
//
//        pref_delay_a1?.setOnBindEditTextListener { editText ->
//            editText.inputType = InputType.TYPE_CLASS_NUMBER
//        }
//
//        pref_delay_a1?.summaryProvider =
//            Preference.SummaryProvider<EditTextPreference> { preference ->
//                val text = preference.text
//                if (TextUtils.isEmpty(text)) {
//                    "Please set it"
//                } else {
//                    text
//                }
//            }
    }

    override fun onPreferenceChange(preference: Preference?, value: Any?): Boolean {
        val stringValue = value.toString()

        return true
    }
}
