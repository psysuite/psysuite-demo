package iit.uvip.psysuite.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.MainApplication
import iit.uvip.psysuite.R
import iit.uvip.psysuite.ResultsManager
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.temporalbinding.SubjectBindingsDialogFragment
import iit.uvip.psysuite.core.tests.temporalbinding.atb.SubjectATBParcel
import iit.uvip.psysuite.core.tests.temporalbinding.atvb.SubjectATVBParcel
import iit.uvip.psysuite.core.tests.temporalbinding.avb.SubjectAVBParcel
import iit.uvip.psysuite.core.tests.temporalbinding.tvb.SubjectTVBParcel
import iit.uvip.psysuite.core.ui.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.databinding.FragmentBindingsBinding
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager


class BindingsFragment  : BaseFragment(
    layout              = R.layout.fragment_bindings,
    landscape           = false,
    hideAndroidControls = false
) {
    override val LOG_TAG:String = BindingsFragment::class.java.simpleName

    private var _binding: FragmentBindingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var subject: SubjectBasicParcel
    private var isSubjectDFopening:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        _binding = FragmentBindingsBinding.inflate(inflater, container, false)
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

        val hasVibrator = VibrationManager.sysHasVibrator(requireContext())
        if(!hasVibrator){
            binding.btStartAtbTest.visibility    = View.INVISIBLE
            binding.btStartAtvbTest.visibility   = View.INVISIBLE
            binding.btStartTvbTest.visibility    = View.INVISIBLE
        }

    }

    override fun onResume() {
        super.onResume()
        isSubjectDFopening = false

        binding.btStartAtbTest.setOnClickListener {
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showATBSubjectDialog()
            }
        }

        binding.btStartAtvbTest.setOnClickListener {
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showATVBSubjectDialog()
            }
        }

        binding.btStartTvbTest.setOnClickListener {
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showTVBSubjectDialog()
            }
        }

        binding.btStartAvbTest.setOnClickListener {


//            debugStart()
//            return@setOnClickListener


            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showAVBSubjectDialog()
            }
        }
    }

    private fun showATBSubjectDialog(){

        subject                 = SubjectATBParcel()
        MainFragment.showDialog(
            subject,
            SubjectBindingsDialogFragment(),
            MainFragment.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE,
            this,
            parentFragmentManager
        )
    }

    private fun showATVBSubjectDialog() {
        subject                 = SubjectATVBParcel()
        MainFragment.showDialog(
            subject,
            SubjectBindingsDialogFragment(),
            MainFragment.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE,
            this,
            parentFragmentManager
        )
    }

    private fun showTVBSubjectDialog() {
        subject                 = SubjectTVBParcel()
        MainFragment.showDialog(
            subject,
            SubjectBindingsDialogFragment(),
            MainFragment.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE,
            this,
            parentFragmentManager
        )
    }

    private fun showAVBSubjectDialog(){

        subject                 = SubjectAVBParcel()
        MainFragment.showDialog(
            subject,
            SubjectBindingsDialogFragment(),
            MainFragment.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE,
            this,
            parentFragmentManager
        )
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
        MainFragment.startTest(subject, requireView(), R.id.action_bindingsFragment_to_testFragment)
    }

    private fun debugStart() {

        val subject = SubjectAVBParcel().apply {
            label               = "a"
            age                 = 1
            gender              = 1
            nextTrailModality   = TestBasic.TEST_NEXTTRIAL_ANSWER
            device              = Device().setRam(requireContext())
            vercode             = UpdateManager.getVersionCodeLocal(requireContext()).first
            stimuliDelays       = MainApplication.delaysAligner
            type                = TestBasic.TEST_AVB_TIME_SINGLESTIM
            trman_type          = TestBasic.TEST_TRMAN_ADAPTIVE
            showResult          = TestBasic.TEST_SWITCH_ENABLED
//            isDebug             = true

            writeJson(requireContext())
        }
        MainFragment.startTest(subject, requireView(), R.id.action_bindingsFragment_to_testFragment)
    }
}