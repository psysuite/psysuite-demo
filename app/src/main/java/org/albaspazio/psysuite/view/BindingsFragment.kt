package org.albaspazio.psysuite.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.albaspazio.psysuite.tests.temporalbinding.atb.SubjectATBParcel
import org.albaspazio.psysuite.tests.temporalbinding.atvb.SubjectATVBParcel
import org.albaspazio.psysuite.tests.temporalbinding.avb.SubjectAVBParcel
import org.albaspazio.psysuite.tests.temporalbinding.tvb.SubjectTVBParcel
import org.albaspazio.psysuite.databinding.FragmentBindingsBinding
import org.albaspazio.core.accessory.VibrationManager
import org.albaspazio.psysuite.R


class BindingsFragment  : TestLaunchFragment(
    layout              = R.layout.fragment_bindings,
    landscape           = false,
    hideAndroidControls = false
) {
    override val LOG_TAG:String = BindingsFragment::class.java.simpleName

    private var _binding: FragmentBindingsBinding? = null
    private val binding get() = _binding!!

    override fun getTestFragmentNavigationAction(): Int = R.id.action_bindingsFragment_to_testFragment

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

        val hasVibrator = VibrationManager.sysHasVibrator(requireContext())
        if(!hasVibrator){
            binding.btStartAtbTest.visibility    = View.GONE
            binding.btStartAtvbTest.visibility   = View.GONE
            binding.btStartTvbTest.visibility    = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        isSubjectDFopening = false

        binding.btStartAtbTest.setOnClickListener {     showSubjectDialog(SubjectATBParcel())  }
        binding.btStartAtvbTest.setOnClickListener {    showSubjectDialog(SubjectATVBParcel())  }
        binding.btStartTvbTest.setOnClickListener {     showSubjectDialog(SubjectTVBParcel())   }
        binding.btStartAvbTest.setOnClickListener {     showSubjectDialog(SubjectAVBParcel())   }
    }
}