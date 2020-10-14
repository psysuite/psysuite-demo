package iit.uvip.psysuite

import android.content.Intent
import android.os.Bundle
import androidx.navigation.Navigation
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TestBasic.Companion.TEST_WNOISE_CHOOSE_OFF
import iit.uvip.psysuite.core.common.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.temporalbinding.SubjectBindingsDialogFragment
import kotlinx.android.synthetic.main.fragment_bindings.*
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager

class BindingsFragment  : BaseFragment(
    layout = R.layout.fragment_bindings,
    landscape = false,
    hideAndroidControls = false
)
{
    override val LOG_TAG:String = BindingsFragment::class.java.simpleName
    private lateinit var subject: SubjectBasicParcel

    override fun onResume() {
        super.onResume()

        bt_start_atb_test.setOnClickListener {
            showATBSubjectDialog()
        }

        bt_start_atvb_test.setOnClickListener {
            showATVBSubjectDialog()
        }

        bt_start_tvb_test.setOnClickListener {
            showTVBSubjectDialog()
        }

        bt_start_avb_test.setOnClickListener {
            showAVBSubjectDialog()
        }
    }

    private fun showATBSubjectDialog(){

        subject                 = SubjectBasicParcel()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB")

        val bundle  = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment          = SubjectBindingsDialogFragment()
        editNameDialogFragment.setTargetFragment(this, MainFragment.TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showATVBSubjectDialog() {
        subject                 = SubjectBasicParcel()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB")

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBindingsDialogFragment()
        editNameDialogFragment.setTargetFragment(this, MainFragment.TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showTVBSubjectDialog() {
        subject                 = SubjectBasicParcel()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB")

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBindingsDialogFragment()
        editNameDialogFragment.setTargetFragment(this, MainFragment.TARGET_FRAGMENT_TVB_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showAVBSubjectDialog(){

        subject                 = SubjectBasicParcel()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB")
        subject.whitenoise      = TEST_WNOISE_CHOOSE_OFF

        val bundle  = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment          = SubjectBindingsDialogFragment()
        editNameDialogFragment.setTargetFragment(this, MainFragment.TARGET_FRAGMENT_AVB_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }
    //================================================================================================================
    // 2 - CALLBACK FROM DATA INSERTION DIALOG CLOSE
    //================================================================================================================
    // subject info !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {

        if (data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectBasicParcel? == null)
            return

        when(requestCode){
            MainFragment.TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE,
            MainFragment.TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE,
            MainFragment.TARGET_FRAGMENT_AVB_SUBJECT_REQUEST_CODE,
            MainFragment.TARGET_FRAGMENT_TVB_SUBJECT_REQUEST_CODE -> {
                subject         = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT)!!
                subject.device  = Device().setRam(requireContext())
                subject.vercode = UpdateManager.getVersionCodeLocal(requireContext()).first
                subject.writeJson(requireContext())
            }
        }
        startTest(subject)
    }


    private fun startTest(subj:SubjectBasicParcel){

        subj.stimuliDelays = MainApplication.delaysAligner   // these values were obtained with the oscilloscope and are device-dependent

        val bundle = Bundle()
        bundle.putParcelable(TestBasic.TESTINFO_BUNDLE_LABEL, subj)
        Navigation.findNavController(requireView()).navigate(R.id.action_bindingsFragment_to_testFragment, bundle)
    }
}