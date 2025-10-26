package iit.uvip.psysuite.view

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.MainActivity
import iit.uvip.psysuite.MainApplication
import iit.uvip.psysuite.ResultsManager
import iit.uvip.psysuite.core.model.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.ui.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.view.MainFragment.Companion.TARGET_FRAGMENT_SUBJECT_REQUEST_CODE
import org.albaspazio.core.accessory.Device
import org.albaspazio.core.accessory.setRam
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.updater.UpdateManager

/**
 * Base class for test fragments that handle subject dialog and test navigation
 */
abstract class TestLaunchFragment(
    layout: Int,
    landscape: Boolean = false,
    hideAndroidControls: Boolean = false
) : BaseFragment(layout, landscape, hideAndroidControls) {

    protected lateinit var subject: SubjectBasicParcel
    protected var isSubjectDFopening: Boolean = false

    /**
     * Override this to provide the navigation action to TestFragment
     */
    abstract fun getTestFragmentNavigationAction(): Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTestResultObserver()
        setupSubjectDialogResultListener()
    }

    /**
     * Observes test results coming back from TestFragment
     */
    private fun setupTestResultObserver() {

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        val resultLiveData = savedStateHandle?.getLiveData<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)

        resultLiveData?.observe(viewLifecycleOwner) { result ->
            if (result != null) {
//                    subject.device = Device().setRam(requireContext())
//                    subject.vercode = UpdateManager.getVersionCodeLocal(requireContext()).first
//                    subject.stimuliDelays = MainApplication.delaysAligner
//                    subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info

                // Restore dynamic orientation when test finishes (tablets only)
                (requireActivity() as MainActivity).restoreDynamicOrientation()

                ResultsManager.getInstance(requireActivity()).onTestFinished(result)
                savedStateHandle.remove<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)
            }
        }

    }

    /**
     * Sets up the fragment result listener for subject dialog. here I start each test with the user filled subject parcel
     */
    private fun setupSubjectDialogResultListener() {
        parentFragmentManager.setFragmentResultListener(
            TARGET_FRAGMENT_SUBJECT_REQUEST_CODE.toString(),
            viewLifecycleOwner
        ) { _, result ->
            isSubjectDFopening = false
            val subj = result.getParcelable<SubjectBasicParcel>(SubjectBasicDialogFragment.SUBJECT_PARCEL)
            if (subj == null) return@setFragmentResultListener

            subject = subj
            subject.device = Device().setRam(requireContext())
            subject.vercode = UpdateManager.getVersionCodeLocal(requireContext()).first
            subject.stimuliDelays = MainApplication.delaysAligner
            subject.writeJson(requireContext()) // is NOT block-aware, always writes without block info
            
            // Lock orientation to landscape for tests (tablets only)
            (requireActivity() as MainActivity).lockOrientationToLandscape()
            
            // Navigate to TestFragment with the specific action for this fragment
            MainFragment.startTest(subject, requireView(), getTestFragmentNavigationAction())
        }
    }

    /**
     * Helper method to show subject dialog
     */
    protected fun showSubjectDialog(subjectParcel: SubjectBasicParcel, dialogFragment: androidx.fragment.app.DialogFragment = SubjectBasicDialogFragment()) {
        if (!isSubjectDFopening) {
            isSubjectDFopening = true
            subject = subjectParcel
            MainFragment.showDialog(
                subject,
                dialogFragment,
                TARGET_FRAGMENT_SUBJECT_REQUEST_CODE,
                this,
                parentFragmentManager
            )
        }
    }
}