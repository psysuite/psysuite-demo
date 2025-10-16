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
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.databinding.FragmentMainBinding


class MainFragment : TestLaunchFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
) {
    override val LOG_TAG: String = MainFragment::class.java.simpleName

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun getTestFragmentNavigationAction(): Int = R.id.action_mainFragment_to_testFragment

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        mMainView = binding.root
        return mMainView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MainFragment has additional logic to remove the result after processing
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        val resultLiveData = savedStateHandle?.getLiveData<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)

        resultLiveData?.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                ResultsManager.getInstance(requireActivity()).onTestFinished(result)
                savedStateHandle.remove<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)
            }
        }
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
//            debugStart()
//            return@setOnClickListener
            showSubjectDialog(SubjectSampleParcel(), SubjectSampleDialogFragment())
        }
    }

    // =====================================================================
    fun debugStart() {

//        val subject = SubjectTSPParcel().apply {
//            label               = "a"
//            age                 = 1
//            gender              = 1
//            nextTrailModality   = TestBasic.TEST_NEXTTRIAL_AUTO
//            device              = Device().setRam(requireContext())
//            vercode             = UpdateManager.getVersionCodeLocal(requireContext()).first
//            stimuliDelays       = MainApplication.delaysAligner
//            type                = TestBasic.TEST_TSP_A_SUB
//            trman_type          = TestBasic.TEST_TRMAN_FIXED
////            isDebug             = true
//
//            writeJson(requireContext())
//        }
//        startTest(subject, requireView())
    }
    // =====================================================================
}

