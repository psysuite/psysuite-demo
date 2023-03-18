package iit.uvip.psysuite

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
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
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.pdf.PdfViewActivity
import java.util.*

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener{

    var haveAudioRecordPermission: Boolean = false

    private val TEST_PERMISSIONS_REQUEST_WRITE = 1
    private val TEST_PERMISSIONS_REQUEST_INTERNET = 2

    private var dialog: AlertDialog? = null

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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //TODO hide menu_action_results when already in resultsFragment
        return when(item.itemId) {
//            R.id.menu_action_settings -> {
//                val intent = Intent(this, SettingsActivity::class.java)
//                startActivity(intent)
//                true
//            }
//            R.id.menu_action_results -> {
//                findNavController(R.id.my_nav_host_fragment).navigate(R.id.action_mainFragment_to_resultsFragment)
//                true
//            }
            R.id.menu_action_manual ->{
                val intent = Intent(this, PdfViewActivity::class.java)
                intent.putExtra("pdfAssetName", "PsySuite_manual.pdf")
                intent.putExtra("title", "Manuale")
                intent.putExtra("error_message", resources.getString(R.string.show_manual_error))
                startActivity(intent)
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

    override fun onSupportNavigateUp() = findNavController(R.id.my_nav_host_fragment).navigateUp()

    override fun onDestinationChanged(controller: NavController, destination: NavDestination,arguments: Bundle?) {}

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            refreshNavigationVisibility()
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

    override fun onBackPressed() {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment)?.childFragmentManager?.fragments?.firstOrNull()  as? BaseFragment

        when(currentFragment?.LOG_TAG){
            "AnswerDialogFragment", "TestFragment"  -> {}
            "MainFragment"      -> {
                                AlertDialog.Builder(this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Chiudi ?")
                                .setMessage("Vuoi uscire dall'applicazione ??")
                                .setCancelable(false)
                                .setPositiveButton("SI"){ _, _ -> finish() }
                                .setNegativeButton("NO", null)
                                .show()
            }
            else                -> super.onBackPressed()
        }
    }


    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        dialog?.dismiss()
        findNavController(R.id.my_nav_host_fragment).removeOnDestinationChangedListener(this)
        super.onDestroy()
    }
}
