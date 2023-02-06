package iit.uvip.psysuite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.core.model.parcel.SubjectBasicListParcel
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.sample.SubjectSampleDialogFragment
import iit.uvip.psysuite.core.tests.sample.SubjectSampleParcel
import iit.uvip.psysuite.core.ui.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.utility.TestResult
import kotlinx.android.synthetic.main.fragment_main.*
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.ui.showAlert
import org.albaspazio.core.updater.UpdateManager


class MainFragment : BaseFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
)
{

    private lateinit var subject: SubjectBasicParcel
    override val LOG_TAG:String = MainFragment::class.java.simpleName
    private var isSubjectDFopening:Boolean = false

    companion object {
        @JvmStatic val isDebug:Boolean = false


        @JvmStatic val TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE: Int    = 1
        @JvmStatic val TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE:Int    = 2
        @JvmStatic val TARGET_FRAGMENT_TVB_SUBJECT_REQUEST_CODE:Int     = 3
        @JvmStatic val TARGET_FRAGMENT_AVB_SUBJECT_REQUEST_CODE:Int     = 4
        @JvmStatic val TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE: Int    = 5
        @JvmStatic val TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE: Int    = 6
        @JvmStatic val TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE: Int    = 7
        @JvmStatic val TARGET_FRAGMENT_SAMPLE_SUBJECT_REQUEST_CODE: Int = 8
        @JvmStatic val TARGET_FRAGMENT_TFI_SUBJECT_REQUEST_CODE: Int    = 9
        @JvmStatic val TARGET_FRAGMENT_FGI_SUBJECT_REQUEST_CODE: Int    = 10

        fun showDialog(subj:SubjectBasicParcel, df:SubjectBasicDialogFragment, rc:Int, frg:Fragment, pfm:FragmentManager){

            subj.isDebug    = isDebug

            if(isDebug){
                subj.label  = "a"
                subj.age    = 1
                subj.gender = 0
            }

            val bundle = Bundle()
            bundle.putParcelable("subject", subj)

            df.arguments    = bundle
            df.setTargetFragment(frg, rc)
            df.isCancelable = false
            df.show(pfm, "Modifica Soggetto")
        }

        fun startTest(subj:SubjectBasicParcel, v:View, nav_action:Int = R.id.action_mainFragment_to_testFragment){
            subj.stimuliDelays  = MainApplication.delaysAligner   // these values were obtained with the oscilloscope and are device-dependent

            val bundle = Bundle()
            bundle.putParcelable(TestBasic.TESTINFO_BUNDLE_LABEL, subj)
            Navigation.findNavController(v).navigate(nav_action, bundle)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // in the fragment going back here I call: setNavigationResult(TestResult(...), TestBasic.TEST_BUNDLE_RESULT_LABEL) and then Navigation.findNavController(requireView()).popBackStack()
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)?.
            observe(viewLifecycleOwner) {   ResultsManager.getInstance(requireActivity()).onTestFinished(it) }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume(){
        super.onResume()

        isSubjectDFopening = false

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        labVersion.text = "ver. ${BuildConfig.VERSION_NAME}"


        bt_start_temporal_test.setOnClickListener {
            if(!isSubjectDFopening) {
                Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_temporaltestsFragment)
            }
        }

        bt_start_musicalmeter.setOnClickListener {
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showMMDSubjectDialog()
            }
        }

        bt_start_tfi_test.setOnClickListener {
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showTFISubjectDialog()
            }
        }

        bt_start_figure_ground_illusion_test.setOnClickListener {
//            debugStart()
//            return@setOnClickListener

            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showFGISubjectDialog()
            }
        }

        bt_start_sample_test.setOnClickListener {
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showSampleSubjectDialog()
            }
        }
    }

    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================


    private fun showFGISubjectDialog(){

        subject                     = SubjectBasicListParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes             = listOf("iit.uvip.psysuite.core.tests.fgi.TestFGI")
        subject.whitenoise          = TestBasic.TEST_WNOISE_DISABLED    // white noise UI flag is invisible

        showDialog(subject, SubjectBasicDialogFragment(), TARGET_FRAGMENT_FGI_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    private fun showMMDSubjectDialog() {

        subject                     = SubjectBasicParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes             = listOf("iit.uvip.psysuite.core.tests.mmd.TestMMD")
        subject.whitenoise          = TestBasic.TEST_WNOISE_DISABLED    // white noise UI flag is invisible

        showDialog(subject, SubjectBasicDialogFragment(), TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    private fun showTFISubjectDialog() {

        subject                     = SubjectBasicParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes             = listOf("iit.uvip.psysuite.core.tests.tfi.TestTFI",
                                             "iit.uvip.psysuite.core.tests.tfi.AnswerDialogFragmentTFI")

        showDialog(subject, SubjectBasicDialogFragment(), TARGET_FRAGMENT_TFI_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    private fun showSampleSubjectDialog(){

        subject                     = SubjectSampleParcel()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.classes             = listOf("iit.uvip.psysuite.core.tests.sample.TestSample")

        showDialog(subject, SubjectSampleDialogFragment(), TARGET_FRAGMENT_SAMPLE_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    //================================================================================================================
    // 2 - CALLBACK FROM DATA INSERTION DIALOG CLOSE
    //================================================================================================================
    // subject info !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {

        try {
            isSubjectDFopening = false
            if (data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectBasicParcel? == null)
                return

            when (requestCode) {
                TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE,
                TARGET_FRAGMENT_TFI_SUBJECT_REQUEST_CODE,
                TARGET_FRAGMENT_FGI_SUBJECT_REQUEST_CODE,
                TARGET_FRAGMENT_SAMPLE_SUBJECT_REQUEST_CODE -> {
                    subject = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT)!!
                    subject.device = Device().setRam(requireContext())
                    subject.vercode = UpdateManager.getVersionCodeLocal(requireContext()).first
                    subject.stimuliDelays = MainApplication.delaysAligner
                    subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info
                }
            }
            startTest(subject, requireView())
        }catch (e:Exception){
            showAlert(requireActivity(), resources.getString(iit.uvip.psysuite.core.R.string.critical_error), resources.getString(
                iit.uvip.psysuite.core.R.string.contact_developer))
            showAlert(requireActivity(), resources.getString(iit.uvip.psysuite.core.R.string.critical_error), e.toString())
        }
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
        subject.classes             = listOf("iit.uvip.psysuite.core.tests.fgi.TestFGI")
        subject.type                = TestBasic.TEST_FGI_1_UNSCRAMBLED

        subject.writeJson(requireContext())
        startTest(subject, requireView())
    }
    // =====================================================================
}

