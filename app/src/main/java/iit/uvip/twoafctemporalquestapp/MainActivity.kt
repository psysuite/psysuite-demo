package iit.uvip.twoafctemporalquestapp

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import iit.uvip.twoafctemporalquestapp.fragments.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*

// NavController.OnNavigatedListener does not resolve with android.arch.navigation:navigation-ui-ktx:1.0.0-alpha09
// must use : android.arch.navigation:navigation-ui-ktx:1.0.0-alpha07

class MainActivity : AppCompatActivity(), NavController.OnNavigatedListener, DialogInterface.OnDismissListener  {

    private val TEST_PERMISSIONS_REQUEST_WRITE = 1
    private val TEST_PERMISSIONS_REQUEST_INTERNET = 2

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(findNavController(R.id.my_nav_host_fragment))
        findNavController(R.id.my_nav_host_fragment).addOnNavigatedListener(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),TEST_PERMISSIONS_REQUEST_WRITE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET),TEST_PERMISSIONS_REQUEST_INTERNET)
    }

    override fun onSupportNavigateUp() = findNavController(R.id.my_nav_host_fragment).navigateUp()

    override fun onNavigated(controller: NavController, destination: NavDestination){}

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            refreshNavigationVisibility()
    }

    fun refreshNavigationVisibility() {
        val currentFragment = my_nav_host_fragment.childFragmentManager.fragments.firstOrNull() as? BaseFragment

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
        val currentFragment = my_nav_host_fragment.childFragmentManager.fragments.firstOrNull() as? BaseFragment

        when(currentFragment?.LOG_TAG){
            "AnswerDialogFragment", "TestFragment"  -> {}
            "MainFragment"          -> {
                                AlertDialog.Builder(this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Chiudi ?")
                                    .setMessage("Vuoi uscire dall'applicazione ??")
                                    .setCancelable(false)
                                    .setPositiveButton("SI", { dialog, i -> finish() })
                                    .setNegativeButton("NO", null)
                                    .show()
            }
            else                    -> super.onBackPressed()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {}

    override fun onDestroy() {

        // release TTS
        val application = applicationContext as MainApplication
        application.tts?.shutdown()

        dialog?.dismiss()
        findNavController(R.id.my_nav_host_fragment).removeOnNavigatedListener(this)
        super.onDestroy()
    }
}
