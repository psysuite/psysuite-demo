package iit.uvip.twoafctemporalquestapp.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import iit.uvip.twoafctemporalquestapp.R
import iit.uvip.twoafctemporalquestapp.tests.Test
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : BaseFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
)
{

    override val LOG_TAG = MainFragment::class.java.simpleName

    override fun onResume() {
        super.onResume()

        activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        bt_bisection.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_bisectionSelectFragment)
        }

        bt_musicalmeters.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("test_type", Test.TEST_MUSICAL_METERS.toString())
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_testInfoDialogFragment, bundle)
        }
    }
}