package iit.uvip.psysuite.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import iit.uvip.psysuite.R
import iit.uvip.psysuite.core.tests.fgi.SubjectFGIParcel
import iit.uvip.psysuite.core.tests.tfi.SubjectTFIParcel
import iit.uvip.psysuite.databinding.FragmentIllusiontestsBinding

class IllusionTestsFragment : TestLaunchFragment(
    layout = R.layout.fragment_illusiontests,
    landscape = false,
    hideAndroidControls = false
) {
    override val LOG_TAG: String = IllusionTestsFragment::class.java.simpleName

    private var _binding: FragmentIllusiontestsBinding? = null
    private val binding get() = _binding!!

    override fun getTestFragmentNavigationAction(): Int = R.id.action_illusionTestsFragment_to_testFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentIllusiontestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.btStartFigureGroundIllusionTest.setOnClickListener {    showSubjectDialog(SubjectFGIParcel())   }
        binding.btStartFlashIllusion.setOnClickListener {               showSubjectDialog(SubjectTFIParcel())   }
    }
}