package iit.uvip.psysuite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.albaspazio.core.ui.show1MethodDialog
import org.albaspazio.core.updater.Constants
import org.albaspazio.core.updater.UpdateManager


class SplashActivity : AppCompatActivity() {

    @SuppressLint("StringFormatInvalid")  // ISSUE: resources.getString(R.string.update_error_message, it) gives error...don't know why
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = "https://api.allspeak.eu/stableupdate.xml"
//        val url = "https://drive.google.com/file/d/1FQly9VIoYjiKkbx9axH0ZIJqKV49vFne/view?usp=sharing"
//        val url = "https://drive.google.com/uc?id=1FQly9VIoYjiKkbx9axH0ZIJqKV49vFne&export=download"

        UpdateManager(this, url,
            {
                // onSuccess
                when(it){
                    Constants.VERSION_UP_TO_UPDATE, Constants.UPDATE_CANCELLED -> go2main()
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