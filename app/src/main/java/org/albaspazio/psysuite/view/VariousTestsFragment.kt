package org.albaspazio.psysuite.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.albaspazio.psysuite.R
import org.albaspazio.psysuite.tests.mmd.SubjectMMDParcel
import org.albaspazio.psysuite.tests.rt.SubjectRTParcel
import org.albaspazio.psysuite.databinding.FragmentVarioustestsBinding

class VariousTestsFragment  :  TestLaunchFragment(
    layout              = R.layout.fragment_varioustests,
    landscape           = false,
    hideAndroidControls = false
){
    override val LOG_TAG:String = VariousTestsFragment::class.java.simpleName

    private var _binding: FragmentVarioustestsBinding? = null
    private val binding get() = _binding!!

    override fun getTestFragmentNavigationAction(): Int = R.id.action_variousTestsFragment_to_testFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        _binding = FragmentVarioustestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.btStartMusicalmeter.setOnClickListener{     showSubjectDialog(SubjectMMDParcel())   }
        binding.btStartRt.setOnClickListener{               showSubjectDialog(SubjectRTParcel())    }
    }
}