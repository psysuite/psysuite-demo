package iit.uvip.psysuite

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TestResult
import iit.uvip.psysuite.core.common.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.sample.SubjectSampleDialogFragment
import iit.uvip.psysuite.core.tests.sample.SubjectSampleParcel
import iit.uvip.psysuite.core.tests.tid.SubjectTIDDialogFragment
import iit.uvip.psysuite.core.tests.tid.SubjectTIDParcel
import kotlinx.android.synthetic.main.fragment_main.*
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager


class MainFragment : BaseFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
)
{
    private lateinit var subject: SubjectBasicParcel
    override val LOG_TAG:String = MainFragment::class.java.simpleName

    private lateinit var resultsManager:ResultsManager

    companion object {
        @JvmStatic val TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE: Int    = 1
        @JvmStatic val TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE:Int    = 2
        @JvmStatic val TARGET_FRAGMENT_TVB_SUBJECT_REQUEST_CODE:Int     = 3
        @JvmStatic val TARGET_FRAGMENT_AVB_SUBJECT_REQUEST_CODE:Int     = 4
        @JvmStatic val TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE: Int    = 5
        @JvmStatic val TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE: Int    = 6
        @JvmStatic val TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE: Int    = 7
        @JvmStatic val TARGET_FRAGMENT_SAMPLE_SUBJECT_REQUEST_CODE: Int = 8
        @JvmStatic val TARGET_FRAGMENT_TFI_SUBJECT_REQUEST_CODE: Int    = 9

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultsManager = ResultsManager(resources, requireActivity())

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)?.observe(viewLifecycleOwner) { result ->

            // if the list contains a results file => append res_files with subject file
            if(result.res_files.isNotEmpty())
                result.res_files.add(subject.getAbsoluteSubjectFilePath())
            resultsManager.onTestFinished(result)
        }
    }

    override fun onResume(){
        super.onResume()

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        labVersion.text = "ver. ${BuildConfig.VERSION_NAME}"

        bt_start_tid_test.setOnClickListener{
            showTIDSubjectDialog()
        }

        bt_start_bindings_test.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_bindingsFragment)
        }

        bt_start_bis.setOnClickListener {
            showBISSubjectDialog()
        }

        bt_start_musicalmeter.setOnClickListener {
            showMMDSubjectDialog()
        }

        bt_start_tfi_test.setOnClickListener {
//            debugStart()
//            return@setOnClickListener
            showTFISubjectDialog()
        }

        bt_start_sample_test.setOnClickListener {
            showSampleSubjectDialog()
        }
    }

    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================

    private fun showTIDSubjectDialog(){

        subject                     = SubjectTIDParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes           = listOf("iit.uvip.psysuite.core.tests.tid.TestTID")

        (subject as SubjectTIDParcel).spinner_data_resource = R.array.tid_sessions_array

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment          = SubjectTIDDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showBISSubjectDialog(){

        subject                     = SubjectBasicParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes           = listOf("iit.uvip.psysuite.core.tests.bis.TestBIS")

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showMMDSubjectDialog() {

        subject                     = SubjectBasicParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes           = listOf("iit.uvip.psysuite.core.tests.mmd.TestMMD")

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showTFISubjectDialog() {

        subject                     = SubjectBasicParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes             = listOf("iit.uvip.psysuite.core.tests.tfi.TestTFI",
                                             "iit.uvip.psysuite.core.tests.tfi.AnswerDialogFragmentTFI")

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_TFI_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showSampleSubjectDialog(){

        subject                     = SubjectSampleParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes             = listOf("iit.uvip.psysuite.core.tests.sample.TestSample")

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment          = SubjectSampleDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_SAMPLE_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments    = bundle
        editNameDialogFragment.isCancelable = false
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }
    //================================================================================================================
    // 2 - CALLBACK FROM DATA INSERTION DIALOG CLOSE
    //================================================================================================================
    // subject info !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {

        if (data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectBasicParcel? == null)
            return

        when(requestCode){
            TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE,
            TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE,
            TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE,
            TARGET_FRAGMENT_TFI_SUBJECT_REQUEST_CODE,
            TARGET_FRAGMENT_SAMPLE_SUBJECT_REQUEST_CODE -> {
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
        Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }

    // =====================================================================
    private fun debugStart() {
        subject                     = SubjectBasicParcel()
        subject.label               = "a"
        subject.age                 = 1
        subject.gender              = 1
        subject.nextTrailModality   = TestBasic.TEST_NEXTTRIAL_NOCHOOSE
        subject.device              = Device().setRam(requireContext())

        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes             = listOf("iit.uvip.psysuite.core.tests.tfi.TestTFI",
            "iit.uvip.psysuite.core.tests.tfi.AnswerDialogFragmentTFI")
        subject.type                = TestBasic.TEST_TFI

        subject.writeJson(requireContext())
        startTest(subject)
    }
    // =====================================================================
}

