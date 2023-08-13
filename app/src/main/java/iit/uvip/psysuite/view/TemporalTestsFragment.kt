package iit.uvip.psysuite.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.MainApplication
import iit.uvip.psysuite.R
import iit.uvip.psysuite.ResultsManager
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.bis.SubjectBISParcel
import iit.uvip.psysuite.core.tests.tid.SubjectTIDDialogFragment
import iit.uvip.psysuite.core.tests.tid.SubjectTIDParcel
import iit.uvip.psysuite.core.ui.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.databinding.FragmentTemporaltestsBinding
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager

class TemporalTestsFragment  :  BaseFragment(
    layout              = R.layout.fragment_temporaltests,
    landscape           = false,
    hideAndroidControls = false
){
    override val LOG_TAG:String = TemporalTestsFragment::class.java.simpleName

    private var _binding: FragmentTemporaltestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var subject: SubjectBasicParcel
    private var isSubjectDFopening:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        _binding = FragmentTemporaltestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // in the fragment going back here I call:
        // findNavController().previousBackStackEntry?.savedStateHandle?.set(TestResult(...), TestBasic.TEST_BUNDLE_RESULT_LABEL) and then Navigation.findNavController(requireView()).popBackStack()
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)?.
            observe(viewLifecycleOwner) {   ResultsManager.getInstance(requireActivity()).onTestFinished(it)        }
    }

    override fun onResume() {
        super.onResume()

        binding.btStartTidTest.setOnClickListener{
            if(!isSubjectDFopening){
                isSubjectDFopening = true
                showTIDSubjectDialog()
            }
        }

        binding.btStartBindings.setOnClickListener {
            if(!isSubjectDFopening) {
                Navigation.findNavController(requireView()).navigate(R.id.action_temporalTestsFragment_to_bindingsFragment)
            }
        }

        binding.btStartBis.setOnClickListener {
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showBISSubjectDialog()
            }
        }
    }
    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================
    private fun showTIDSubjectDialog(){

        subject = SubjectTIDParcel()
        (subject as SubjectTIDParcel).spinner_data_resource = R.array.tid_sessions_array

        MainFragment.showDialog(
            subject,
            SubjectTIDDialogFragment(),
            MainFragment.TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE,
            this,
            parentFragmentManager
        )
    }

    private fun showBISSubjectDialog(){

        subject = SubjectBISParcel()
        MainFragment.showDialog(
            subject,
            SubjectBasicDialogFragment(),
            MainFragment.TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE,
            this,
            parentFragmentManager
        )
    }

    private fun debugStart() {

        val subject = SubjectTIDParcel().apply {
            label               = "a"
            age                 = 1
            gender              = 1
            nextTrailModality   = TestBasic.TEST_NEXTTRIAL_ANSWER
            device              = Device().setRam(requireContext())
            vercode             = UpdateManager.getVersionCodeLocal(requireContext()).first
            stimuliDelays       = MainApplication.delaysAligner
            type                = TestBasic.TEST_TID_SHORT_AUDIO
            trman_type          = TestBasic.TEST_TRMAN_ADAPTIVE
            session             = 1
            group               = 1
            showResult          = TestBasic.TEST_SWITCH_ENABLED
//            isDebug             = true

            writeJson(requireContext())
        }
        MainFragment.startTest(subject, requireView(), R.id.action_temporalTestsFragment_to_testFragment)
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
            MainFragment.TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE,
            MainFragment.TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE -> {
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
            R.id.action_temporalTestsFragment_to_testFragment
        )
    }
}