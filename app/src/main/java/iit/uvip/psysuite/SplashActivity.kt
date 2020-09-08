package iit.uvip.psysuite

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.albaspazio.core.ui.show1MethodDialog
import org.albaspazio.core.updater.Constants
import org.albaspazio.core.updater.UpdateManager


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = "https://api.allspeak.eu/psysuitestableupdate.xml"
//        val url = "http://192.168.1.14:8095/psysuitestableupdate.xml"     // localhost @home

        UpdateManager(this, url,
            {
                // onSuccess
                when(it){
                    Constants.VERSION_UP_TO_UPDATE,
                    Constants.UPDATE_CANCELLED,
                    Constants.NETWORK_ABSENT          -> go2main()
                }
            },
            {
                // onError
                show1MethodDialog(this, resources.getString(R.string.error), resources.getString(R.string.update_error_message, it), "OK"){
                    go2main()
                }

            }
        ).checkUpdate()
    }

    private fun go2main(){
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()    // close splash activity
    }
}






















/*

//        var mStimuliHandler = Handler()
//        mStimuliHandler.postDelayed({
//            // Start main activity
//            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//            // close splash activity
//            finish()
//        }, 2000)
 */