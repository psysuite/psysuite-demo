package iit.uvip.psysuite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.core.model.parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.tests.temporalbinding.SubjectBindingsDialogFragment
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
            if(!isSubjectDFopening) {
                isSubjectDFopening = true
                showAVBSubjectDialog()
            }
        }
    }

    private fun showATBSubjectDialog(){

        subject                 = SubjectBasicParcel()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB")

        MainFragment.showDialog(subject, SubjectBindingsDialogFragment(), MainFragment.TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    private fun showATVBSubjectDialog() {
        subject                 = SubjectBasicParcel()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB")

        MainFragment.showDialog(subject, SubjectBindingsDialogFragment(), MainFragment.TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    private fun showTVBSubjectDialog() {
        subject                 = SubjectBasicParcel()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.tvb.TestTVB")

        MainFragment.showDialog(subject, SubjectBindingsDialogFragment(), MainFragment.TARGET_FRAGMENT_TVB_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
    }

    private fun showAVBSubjectDialog(){

        subject                 = SubjectBasicParcel()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.classes         = listOf("iit.uvip.psysuite.core.tests.temporalbinding.avb.TestAVB")
        subject.whitenoise      = TestBasic.TEST_WNOISE_DISABLED

        MainFragment.showDialog(subject, SubjectBindingsDialogFragment(), MainFragment.TARGET_FRAGMENT_AVB_SUBJECT_REQUEST_CODE, this, parentFragmentManager)
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
            MainFragment.TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE,
            MainFragment.TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE,
            MainFragment.TARGET_FRAGMENT_AVB_SUBJECT_REQUEST_CODE,
            MainFragment.TARGET_FRAGMENT_TVB_SUBJECT_REQUEST_CODE -> {
                subject                 = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT)!!
                subject.device          = Device().setRam(requireContext())
                subject.vercode         = UpdateManager.getVersionCodeLocal(requireContext()).first
                subject.stimuliDelays   = MainApplication.delaysAligner
                subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info
            }
        }
        MainFragment.startTest(subject, requireView(), R.id.action_bindingsFragment_to_testFragment)
    }
}