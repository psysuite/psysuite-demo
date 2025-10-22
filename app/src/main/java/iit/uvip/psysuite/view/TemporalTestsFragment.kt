package iit.uvip.psysuite.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import iit.uvip.psysuite.R
import iit.uvip.psysuite.core.tests.bis.SubjectBISParcel
import iit.uvip.psysuite.core.tests.tid.SubjectTIDDialogFragment
import iit.uvip.psysuite.core.tests.tid.SubjectTIDParcel
import iit.uvip.psysuite.core.tests.tir.SubjectTIRParcel
import iit.uvip.psysuite.databinding.FragmentTemporaltestsBinding

class TemporalTestsFragment : TestLaunchFragment(
    layout = R.layout.fragment_temporaltests,
    landscape = false,
    hideAndroidControls = false
) {
    override val LOG_TAG: String = TemporalTestsFragment::class.java.simpleName

    private var _binding: FragmentTemporaltestsBinding? = null
    private val binding get() = _binding!!

    override fun getTestFragmentNavigationAction(): Int = R.id.action_temporalTestsFragment_to_testFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTemporaltestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        binding.btStartBindings.setOnClickListener {
            if (!isSubjectDFopening) {
                Navigation.findNavController(requireView()).navigate(R.id.action_temporalTestsFragment_to_bindingsFragment)
            }
        }

        binding.btStartTidTest.setOnClickListener { showSubjectDialog(SubjectTIDParcel(), SubjectTIDDialogFragment())   }
        binding.btStartBis.setOnClickListener {     showSubjectDialog(SubjectBISParcel())   }
        binding.btStartTirTest.setOnClickListener { showSubjectDialog(SubjectTIRParcel())   }
    }
}


/*
    private fun debugStart() {

//        val subject = SubjectAVBParcel().apply {
//            label               = "a"
//            age                 = 1
//            gender              = 1
//            nextTrailModality   = TestBasic.TEST_NEXTTRIAL_ANSWER
//            device              = Device().setRam(requireContext())
//            vercode             = UpdateManager.getVersionCodeLocal(requireContext()).first
//            stimuliDelays       = MainApplication.delaysAligner
//            type                = TestBasic.TEST_AVB_TIME_SINGLESTIM
//            trman_type          = TestBasic.TEST_TRMAN_FIXED
//            showResult          = TestBasic.TEST_SWITCH_ENABLED
//            isDebug             = false
//
//            writeJson(requireContext())
//        }
//        val subject = SubjectBISParcel().apply {
//            label               = "a"
//            age                 = 1
//            gender              = 1
//            nextTrailModality   = TestBasic.TEST_NEXTTRIAL_ANSWER
//            device              = Device().setRam(requireContext())
//            vercode             = UpdateManager.getVersionCodeLocal(requireContext()).first
//            stimuliDelays       = MainApplication.delaysAligner
//            type                = TestBasic.TEST_BISECTION_AUDIO
//            trman_type          = TestBasic.TEST_TRMAN_FIXED
//            showResult          = TestBasic.TEST_SWITCH_ENABLED
//            isDebug             = false
//
//            writeJson(requireContext())
//        }
//        val subject = SubjectTIDParcel().apply {
//            label               = "a"
//            age                 = 1
//            gender              = 1
//            nextTrailModality   = TestBasic.TEST_NEXTTRIAL_ANSWER
//            device              = Device().setRam(requireContext())
//            vercode             = UpdateManager.getVersionCodeLocal(requireContext()).first
//            stimuliDelays       = MainApplication.delaysAligner
//            type                = TestBasic.TEST_TID_SHORT_AUDIO
//            trman_type          = TestBasic.TEST_TRMAN_FIXED
//            session             = 1
//            group               = 1
//            showResult          = TestBasic.TEST_SWITCH_ENABLED
//            isDebug             = false
//
//            writeJson(requireContext())
//        }
        startTest(subject, requireView(), R.id.action_temporalTestsFragment_to_testFragment)
    }

 */