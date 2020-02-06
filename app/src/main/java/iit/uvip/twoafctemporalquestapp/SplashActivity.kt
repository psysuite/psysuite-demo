package iit.uvip.twoafctemporalquestapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var mStimuliHandler: Handler = Handler()
        mStimuliHandler.postDelayed({
            // Start main activity
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            // close splash activity
            finish()
        }, 2000)

    }
}