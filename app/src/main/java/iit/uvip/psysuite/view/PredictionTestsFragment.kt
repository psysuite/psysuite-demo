package iit.uvip.psysuite.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.MainApplication
import iit.uvip.psysuite.R
import iit.uvip.psysuite.ResultsManager
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.ttc.SubjectTTCParcel
import iit.uvip.psysuite.core.ui.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.databinding.FragmentPredictiontestsBinding
import iit.uvip.psysuite.view.MainFragment.Companion.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE
import iit.uvip.psysuite.view.MainFragment.Companion.showDialog
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager

class PredictionTestsFragment  :  BaseFragment(
    layout              = R.layout.fragment_predictiontests,
    landscape           = false,
    hideAndroidControls = false
){
    override val LOG_TAG:String = PredictionTestsFragment::class.java.simpleName

    private var _binding: FragmentPredictiontestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var subject: SubjectBasicParcel
    private var isSubjectDFopening:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        _binding = FragmentPredictiontestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // in the fragment going back here I call:
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)?.
            observe(viewLifecycleOwner) {   ResultsManager.getInstance(requireActivity()).onTestFinished(it)        }
    }

    override fun onResume() {
        super.onResume()

        binding.btStartTtcTest.setOnClickListener{
            if(!isSubjectDFopening){
                isSubjectDFopening = true
                showTTCSubjectDialog()
            }
        }

    }
    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================
    private fun showTTCSubjectDialog(){
        subject                     = SubjectTTCParcel()
        showDialog(subject, SubjectBasicDialogFragment(), TARGET_FRAGMENT_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    //================================================================================================================
    // 2 - CALLBACK FROM DATA INSERTION DIALOG CLOSE
    //================================================================================================================
    // subject info !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {

        isSubjectDFopening = false
        if (data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectBasicParcel? == null)
            return

        when(requestCode){
            MainFragment.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE -> {
                subject                 = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT)!!
                subject.device          = Device().setRam(requireContext())
                subject.vercode         = UpdateManager.getVersionCodeLocal(requireContext()).first
                subject.stimuliDelays   = MainApplication.delaysAligner
                subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info
            }
        }
        MainFragment.startTest(
            subject,
            requireView(),
            R.id.action_predictionTestsFragment_to_testFragment
        )
    }
}