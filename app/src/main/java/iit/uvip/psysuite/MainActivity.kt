package iit.uvip.psysuite

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.intentfilter.androidpermissions.PermissionManager
import com.intentfilter.androidpermissions.models.DeniedPermissions
import iit.uvip.psysuite.settings.SettingsActivity
import iit.uvip.psysuite.device.DeviceIdentificationManager
import iit.uvip.psysuite.device.DeviceIdBackupManager
import iit.uvip.psysuite.device.DeviceRegistrationDialog
import android.widget.Toast
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.pdf.PdfViewActivity

import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.sample.SubjectSampleDialogFragment
import iit.uvip.psysuite.core.tests.sample.SubjectSampleParcel
import iit.uvip.psysuite.core.ui.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.utility.filesystem.FileSystemManager
import iit.uvip.psysuite.view.MainFragment
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert
import java.util.*


class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener{

    var haveAudioRecordPermission: Boolean = false

    private val TEST_PERMISSIONS_REQUEST_WRITE = 1
    private val TEST_PERMISSIONS_REQUEST_INTERNET = 2

    private var dialog: AlertDialog? = null

    private lateinit var deviceManager: DeviceIdentificationManager
    private lateinit var resultsManager: ResultsManager
    private lateinit var fileSystemManager: FileSystemManager

    var isSubjectDFopening: Boolean = false

    // This will be called whenever an Intent with an action named "NAVIGATION_UPDATE" is broadcasted.
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if(intent.action == "NAVIGATION_UPDATE") refreshNavigationVisibility()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(findNavController(R.id.my_nav_host_fragment))
        findNavController(R.id.my_nav_host_fragment).addOnDestinationChangedListener(this)

        checkPermissions(Manifest.permission.RECORD_AUDIO)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),TEST_PERMISSIONS_REQUEST_WRITE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET),TEST_PERMISSIONS_REQUEST_INTERNET)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Log.d("BuildConfig", "API_URL: ${BuildConfig.API_URL}")
        Log.d("BuildConfig", "API_KEY: ${BuildConfig.API_KEY}")

        try {
            // Initialize managers
            deviceManager = DeviceIdentificationManager.getInstance(this)
            fileSystemManager = FileSystemManager.getInstance()
            resultsManager = ResultsManager.getInstance(this)
            resultsManager.updateContext(this)

            // Start the initialization flow
            startInitializationFlow()
        }
        catch (e: Exception) {
            showAlert(this, resources.getString(iit.uvip.psysuite.core.R.string.critical_error), e.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //TODO hide menu_action_results when already in resultsFragment
        return when(item.itemId) {

            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_send_results -> {
                resultsManager.openResultsManager()
                true
            }
            R.id.menu_action_manual ->{
                val intent = Intent(this, PdfViewActivity::class.java)
                intent.putExtra("pdfAssetName", "PsySuite_manual.pdf")
                intent.putExtra("title", "Manuale")
                intent.putExtra("error_message", resources.getString(R.string.show_manual_error))
                startActivity(intent)
                true
            }
            R.id.menu_sample_test -> {
                handleSampleTestFromMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter("NAVIGATION_UPDATE"))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        dialog?.dismiss()
        findNavController(R.id.my_nav_host_fragment).removeOnDestinationChangedListener(this)
        super.onDestroy()
    }

    // region NAVIGATION
    override fun onSupportNavigateUp() = findNavController(R.id.my_nav_host_fragment).navigateUp()

    override fun onDestinationChanged(controller: NavController, destination: NavDestination,arguments: Bundle?) {}

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            refreshNavigationVisibility()
    }

    fun refreshNavigationVisibility() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment)?.childFragmentManager?.fragments?.firstOrNull()  as? BaseFragment

        if (currentFragment?.hideAndroidControls == true) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_FULLSCREEN)

            actionBar?.hide()
            supportActionBar?.hide()
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            actionBar?.show()
            supportActionBar?.show()
        }
    }
    // endregion

    // region SAMPLE TEST HANDLING
    private fun handleSampleTestFromMenu() {
        if (!isSubjectDFopening) {
            isSubjectDFopening = true
            val navigationAction = NavigationActionManager.resolveNavigationAction(this, NavigationActionManager.NavigationDestination.TEST_FRAGMENT) ?: R.id.action_mainFragment_to_testFragment
            
            // Create a temporary fragment to act as the target for the dialog
            val tempFragment = SampleTestDialogFragment.newInstance(navigationAction)
            
            // Add the temporary fragment
            supportFragmentManager.beginTransaction()
                .add(tempFragment, "temp_sample_test_fragment")
                .commit()
            
            // Create subject parcel and show dialog using MainFragment's method
            val subjectParcel = SubjectSampleParcel()
            val dialogFragment = SubjectSampleDialogFragment()
            
            MainFragment.showDialog(
                subjectParcel,
                dialogFragment,
                MainFragment.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE,
                tempFragment,
                supportFragmentManager
            )
        }
    }
    // endregion

    // region INITIALIZATION FLOW
    private fun startInitializationFlow() {
        // Step 1: Check device registration
        checkDeviceRegistration()
    }

    private fun checkDeviceRegistration() {
        when {
            deviceManager.isDeviceRegistered    ->  checkPendingResults()
            deviceManager.isRegistrationSkipped ->  checkPendingResults()
            deviceManager.isFirstLaunch         -> {
                                                    // First launch and not registered, try to restore from backup
                                                    val backupManager = DeviceIdBackupManager(this)
                                                    val restoredId = backupManager.restoreDeviceId()

                                                    if (restoredId != null) {
                                                        deviceManager.setDeviceId(restoredId)
                                                        checkPendingResults()
                                                    } else {
                                                        // No backup found, show registration dialog
                                                        showRegistrationDialog()
                                                    }
                                                }
            
            else -> {
                // Not first launch but not registered and not skipped, show registration dialog
                showRegistrationDialog()
            }
        }
    }

    private fun checkPendingResults() {
        // Step 2: Check for valid files and upload capability
        if (resultsManager.canUpload && resultsManager.existResultsToSend) {
            show2ChoisesDialog(this, resources.getString(R.string.warning), "There are pending results to send. do you want to send them?", resources.getString(R.string.yes), resources.getString(R.string.no),
                { /* pressed YES */ resultsManager.openResultsManager() },{})
        }
        // If no results to send or can't upload or don't want to upload, do nothing (stay on main screen)
    }

    private fun showRegistrationDialog() {
        val dialog = DeviceRegistrationDialog.newInstance(isFirstLaunch = deviceManager.isFirstLaunch, allowSkip = true)
        dialog.setOnDeviceRegisteredListener(object : DeviceRegistrationDialog.OnDeviceRegisteredListener {

            override fun onDeviceRegistered(deviceId: String) {
                DeviceIdBackupManager(this@MainActivity).backupDeviceId(deviceId)
                deviceManager.setDeviceId(deviceId)
                Toast.makeText(this@MainActivity, resources.getString(R.string.device_registered, deviceId), Toast.LENGTH_LONG).show()
                
                // After registration, proceed to step 2
                checkPendingResults()
            }
            
            override fun onRegistrationSkipped() {
                Toast.makeText(this@MainActivity, resources.getString(R.string.registration_skipped), Toast.LENGTH_LONG).show()
                deviceManager.skipRegistration()
                
                // After skipping, proceed to step 2
                checkPendingResults()
            }
            
            override fun onRegistrationCancelled() {
                Toast.makeText(this@MainActivity, resources.getString(R.string.registration_cancelled), Toast.LENGTH_LONG).show()
                // On cancel, still proceed to step 2 (don't block the user)
                checkPendingResults()
            }
        })
        dialog.show(supportFragmentManager, "device_registration")
    }
    // endregion

    override fun onBackPressed() {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment)?.childFragmentManager?.fragments?.firstOrNull()  as? BaseFragment

        when(currentFragment?.LOG_TAG){
            "AnswerDialogFragment", "TestFragment"  -> {}
            "MainFragment"      -> {
                AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(resources.getString(R.string.warning))
                    .setMessage(resources.getString(R.string.want_to_quit_app))
                    .setCancelable(false)
                    .setPositiveButton(resources.getString(R.string.yes)){ _, _ -> finish() }
                    .setNegativeButton(resources.getString(R.string.no), null)
                    .show()
            }
            else                -> super.onBackPressed()
        }
    }

    private fun checkPermissions(perm: String) {
        val permissionManager: PermissionManager = PermissionManager.getInstance(applicationContext)
        permissionManager.checkPermissions(
            Collections.singleton(perm),
            object : PermissionManager.PermissionRequestListener {
                override fun onPermissionGranted() {
                    haveAudioRecordPermission = true
                }

                override fun onPermissionDenied(deniedPermissions: DeniedPermissions) {
                    haveAudioRecordPermission = false
                }
            })
    }

}

/**
 * Temporary fragment to handle sample test dialog results from MainActivity
 */
class SampleTestDialogFragment : androidx.fragment.app.Fragment() {

    companion object {
        private const val ARG_NAVIGATION_ACTION = "navigation_action"

        fun newInstance(navigationAction: Int): SampleTestDialogFragment {
            val fragment = SampleTestDialogFragment()
            val args = Bundle()
            args.putInt(ARG_NAVIGATION_ACTION, navigationAction)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navigationAction = arguments?.getInt(ARG_NAVIGATION_ACTION) ?: R.id.action_mainFragment_to_testFragment

        // Set up the result listener
        parentFragmentManager.setFragmentResultListener(
            MainFragment.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE.toString(),
            this
        ) { _, result ->
            val activity = requireActivity() as MainActivity
            activity.isSubjectDFopening = false

            val subj = result.getParcelable<SubjectBasicParcel>(SubjectBasicDialogFragment.EVENT_SUBJECT)
            if (subj != null) {
                // Use MainFragment's static method to start the test with the appropriate navigation action
                MainFragment.startTest(subj, requireActivity().findViewById(R.id.my_nav_host_fragment), navigationAction)
            }
            // Remove this temporary fragment
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }
}
