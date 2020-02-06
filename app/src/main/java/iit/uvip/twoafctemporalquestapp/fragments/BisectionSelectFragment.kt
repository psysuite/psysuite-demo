package iit.uvip.twoafctemporalquestapp.fragments

import android.os.Bundle
import androidx.navigation.Navigation
import iit.uvip.twoafctemporalquestapp.R
import iit.uvip.twoafctemporalquestapp.tests.Test
import kotlinx.android.synthetic.main.fragment_bisection_select.*

class BisectionSelectFragment : BaseFragment(
    layout = R.layout.fragment_bisection_select,
    landscape = false,
    hideAndroidControls = false
)
{

    override val LOG_TAG = BisectionSelectFragment::class.java.simpleName

    override fun onResume() {
        super.onResume()

        bt_bisection_audio.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("test_type", Test.TEST_BISECTION_AUDIO.toString())
            Navigation.findNavController(it).navigate(R.id.action_bisectionSelectFragment_to_testInfoDialogFragment, bundle)
        }

        bt_bisection_tactile.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("test_type", Test.TEST_BISECTION_TACTILE.toString())
            Navigation.findNavController(it).navigate(R.id.action_bisectionSelectFragment_to_testInfoDialogFragment, bundle)
        }
        
        bt_bisection_audio_tactile.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("test_type", Test.TEST_BISECTION_AUDIO_TACTILE.toString())
            Navigation.findNavController(it).navigate(R.id.action_bisectionSelectFragment_to_testInfoDialogFragment, bundle)
        }

        bt_bisection_audio_video.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("test_type", Test.TEST_BISECTION_AUDIO_VIDEO.toString())
            Navigation.findNavController(it).navigate(R.id.action_bisectionSelectFragment_to_testInfoDialogFragment, bundle)
        }
    }
}