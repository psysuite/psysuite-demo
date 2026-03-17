package org.albaspazio.psysuite.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.albaspazio.psysuite.R
import org.albaspazio.psysuite.tests.tsp.SubjectTSPParcel
import org.albaspazio.psysuite.tests.ttc.SubjectTTCParcel
import org.albaspazio.psysuite.databinding.FragmentPredictiontestsBinding

class PredictionTestsFragment  :  TestLaunchFragment(
    layout              = R.layout.fragment_predictiontests,
    landscape           = false,
    hideAndroidControls = false
){
    override val LOG_TAG:String = PredictionTestsFragment::class.java.simpleName

    private var _binding: FragmentPredictiontestsBinding? = null
    private val binding get() = _binding!!

    override fun getTestFragmentNavigationAction(): Int = R.id.action_predictionTestsFragment_to_testFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        _binding = FragmentPredictiontestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.btStartTtcTest.setOnClickListener{  showSubjectDialog(SubjectTTCParcel())   }
        binding.btStartTspTest.setOnClickListener{  showSubjectDialog(SubjectTSPParcel())   }
    }
}