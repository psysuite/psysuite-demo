package iit.uvip.twoafctemporalquestapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.navigation.Navigation
import iit.uvip.twoafctemporalquestapp.R
import iit.uvip.twoafctemporalquestapp.showToast
import iit.uvip.twoafctemporalquestapp.tests.Test
import kotlinx.android.synthetic.main.fragment_test_info.*

class TestInfoFragment: BaseFragment(
    layout = R.layout.fragment_test_info,
    landscape = false,
    hideAndroidControls = false
){

    override val LOG_TAG                 = TestInfoFragment::class.java.simpleName
    private var mTestType: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mTestType = arguments?.getString("test_type").toString().toInt()
    }

    override fun onResume() {

        super.onResume()

        when(mTestType)
        {
            Test.TEST_BISECTION_AUDIO            -> txt_type.setText("audio")
            Test.TEST_BISECTION_TACTILE          -> txt_type.setText("tactile")
            Test.TEST_BISECTION_AUDIO_TACTILE    -> txt_type.setText("audio_tactile")
            Test.TEST_BISECTION_AUDIO_VIDEO      -> txt_type.setText("audio_video")
            Test.TEST_MUSICAL_METERS             -> txt_type.setText("mmeters")
        }

        bt_confirm.setOnClickListener{

            if (txt_subject_id.text.toString().isEmpty() || txt_type.text.toString().isEmpty())
            {
                val msg = "Devi selezionare sia il tipo di test che il nome del soggetto"
                Log.w(LOG_TAG, msg)
                showToast(msg, context!!)
            }
            else
            {
                val bundle = Bundle()

                // other chars are limited in the xml. the " " is substituted here
                val cleanTestName    = txt_type.text.toString().replace(" ", "_")
                val cleanTestSubj    = txt_subject_id.text.toString().replace(" ", "_")

                bundle.putString("test_type",   mTestType.toString())
                bundle.putString("test_name",   cleanTestName)
                bundle.putString("subject_id",  cleanTestSubj)

                Navigation.findNavController(it).navigate(R.id.action_testInfoDialogFragment_to_testFragment, bundle)
            }
        }

        bt_cancel.setOnClickListener{
            fragmentManager?.popBackStack()
        }
    }
}