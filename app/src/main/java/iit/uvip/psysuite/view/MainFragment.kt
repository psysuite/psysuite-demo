package iit.uvip.psysuite.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.BuildConfig
import iit.uvip.psysuite.MainApplication
import iit.uvip.psysuite.R
import iit.uvip.psysuite.ResultsManager
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.sample.SubjectSampleDialogFragment
import iit.uvip.psysuite.core.tests.sample.SubjectSampleParcel
import iit.uvip.psysuite.core.ui.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.databinding.FragmentMainBinding
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager


class MainFragment : BaseFragment(
    layout              = R.layout.fragment_main,
    landscape           = false,
    hideAndroidControls = false
) {
    private lateinit var subject: SubjectBasicParcel
    override val LOG_TAG:String = MainFragment::class.java.simpleName

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var isSubjectDFopening:Boolean = false

    companion object {
        @JvmStatic val isDebug:Boolean = false

        @JvmStatic val TARGET_FRAGMENT_SUBJECT_REQUEST_CODE: Int    = 1

        fun showDialog(subj:SubjectBasicParcel, df: DialogFragment, rc:Int, frg:Fragment, pfm:FragmentManager){

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        mMainView = binding.root
        setupFragmentResultListener()
        return mMainView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        binding.labVersion.text = "ver. ${BuildConfig.VERSION_NAME}"

        binding.btStartTemporalTest.setOnClickListener {
            if(!isSubjectDFopening) Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_temporaltestsFragment)
        }

        binding.btStartVariousTest.setOnClickListener {
            if(!isSubjectDFopening) Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_varioustestsFragment)
        }

        binding.btStartIllusionTest.setOnClickListener {
            if(!isSubjectDFopening) Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_illusiontestsFragment)
        }

        binding.btStartPredictionTest.setOnClickListener {
            if(!isSubjectDFopening) Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_predictiontestsFragment)
        }

        binding.btStartSampleTest.setOnClickListener {
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showSampleSubjectDialog()
            }
        }
    }
    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================

    private fun showSampleSubjectDialog(){
        subject                     = SubjectSampleParcel()
        showDialog(subject, SubjectSampleDialogFragment(), TARGET_FRAGMENT_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    //================================================================================================================
    // 2 - CALLBACK FROM DATA INSERTION DIALOG CLOSE
    //================================================================================================================
    // subject info !
    private fun setupFragmentResultListener() {
        // Listener for answer results
        parentFragmentManager.setFragmentResultListener(TARGET_FRAGMENT_SUBJECT_REQUEST_CODE.toString(),viewLifecycleOwner) { _, result ->
            isSubjectDFopening = false
            val subj = result.getParcelable<SubjectBasicParcel>(SubjectBasicDialogFragment.EVENT_SUBJECT)
            if (subj == null) return@setFragmentResultListener

            subject = subj
            subject.device = Device().setRam(requireContext())
            subject.vercode = UpdateManager.getVersionCodeLocal(requireContext()).first
            subject.stimuliDelays = MainApplication.delaysAligner
            subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info
            startTest(subject, requireView())
        }
    }

    // =====================================================================
    fun debugStart() {

//        val subject = SubjectBISParcel().apply {
//            label               = "a"
//            age                 = 1
//            gender              = 1
//            nextTrailModality   = TestBasic.TEST_NEXTTRIAL_NOCHOOSE
//            device              = Device().setRam(requireContext())
//            vercode             = UpdateManager.getVersionCodeLocal(requireContext()).first
//            stimuliDelays       = MainApplication.delaysAligner
//            type                = TestBasic.TEST_BISECTION_AUDIO
//            trman_type          = TestBasic.TEST_TRMAN_ADAPTIVE
////            isDebug             = true
//
//            writeJson(requireContext())
//        }
        startTest(subject, requireView())
    }
    // =====================================================================
}

