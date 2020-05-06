package iit.uvip.audiotactilebindingapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import iit.uvip.audiotactilebindingapp.R
import iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel.*
import iit.uvip.audiotactilebindingapp.tests.common.subjects_dialog.SubjectBasicSpinnerDialogFragment
import iit.uvip.audiotactilebindingapp.tests.tid.SubjectTIDDialogFragment
import iit.uvip.audiotactilebindingapp.tests.atb.TestATB
import iit.uvip.audiotactilebindingapp.tests.bis.TestBIS
import iit.uvip.audiotactilebindingapp.tests.common.TestBasic
import iit.uvip.audiotactilebindingapp.tests.common.TestParcel
import iit.uvip.audiotactilebindingapp.tests.common.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.audiotactilebindingapp.tests.musmet.TestMusMet
import iit.uvip.audiotactilebindingapp.tests.tid.SubjectTIDParcel
import iit.uvip.audiotactilebindingapp.tests.tid.TestTID
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : BaseFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
)
{
    private lateinit var subject: SubjectBasicParcel

    override val LOG_TAG:String = MainFragment::class.java.simpleName

    private var testName:String = ""
    private var testCode:Int = -1

    companion object {
        @JvmStatic val TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE: Int    = 1
        @JvmStatic val TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE: Int    = 2
        @JvmStatic val TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE: Int    = 3
        @JvmStatic val TARGET_FRAGMENT_MUSMET_SUBJECT_REQUEST_CODE: Int = 4
        @JvmStatic val EVENT_SUBJECT:String                             = "subject"
    }

    override fun onResume(){
        super.onResume()

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        bt_start_tid_test.setOnClickListener{
            showTIDSubjectDialog()
        }

        bt_start_atb_test.setOnClickListener {
            showATBSubjectDialog()
        }

        bt_start_bisection.setOnClickListener {
            showBISSubjectDialog()
        }

        bt_start_musicalmeter.setOnClickListener {
            showMusMetSubjectDialog()
        }
    }

    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================
    private fun showATBSubjectDialog(){

        subject                     = SubjectBasicParcel.loadSubject()
        subject.nextTrailModality   = TestBasic.TEST_NEXTTRIAL_BUTTON   // can choose whether pausing each trial

        val bundle  = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(requireFragmentManager(), "Modifica Soggetto")
    }

    private fun showTIDSubjectDialog(){

        subject     = SubjectTIDParcel.loadSubject()

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectTIDDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(requireFragmentManager(), "Modifica Soggetto")
    }

    private fun showBISSubjectDialog(){

        subject                                                     = SubjectBasicListParcel.loadSubject()
        subject.nextTrailModality                                   = TestBasic.TEST_NEXTTRIAL_ANSWER
        (subject as SubjectBasicListParcel).spinner_data_resource   = R.array.bis_tests_array
        (subject as SubjectBasicListParcel).spinner_label           = resources.getString(R.string.test_type)

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicSpinnerDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(requireFragmentManager(), "Modifica Soggetto")
    }

    private fun showMusMetSubjectDialog(){

        subject                                                     = SubjectBasicParcel.loadSubject()
        subject.nextTrailModality                                   = TestBasic.TEST_NEXTTRIAL_ANSWER

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_MUSMET_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(requireFragmentManager(), "Modifica Soggetto")
    }
    //================================================================================================================
    // 2 - CALLBACK FROM DATA INSERTION DIALOG CLOSE
    //================================================================================================================
    // subject info !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        // Make sure fragment codes match up
        subject = data?.getParcelableExtra(EVENT_SUBJECT) ?: subject

        when(requestCode){
            TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE -> {
                if (subject != null) (subject as SubjectBasicParcel).writeJson(requireContext())
                onATBSubjectUpdated()
            }

            TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE -> {
                if (subject != null) (subject as SubjectTIDParcel).writeJson(requireContext())
                onTIDSubjectUpdated()
            }

            TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE -> {
                if (subject != null) (subject as SubjectBasicListParcel).writeJson(requireContext())
                onBISSubjectUpdated()
            }

            TARGET_FRAGMENT_MUSMET_SUBJECT_REQUEST_CODE -> {
                if (subject != null) (subject as SubjectBasicParcel).writeJson(requireContext())
                onMusMetSubjectUpdated()
            }
        }
    }
    //================================================================================================================
    // 3 - SUBJECT DATA INSERTED
    //================================================================================================================
    private fun onATBSubjectUpdated(){

        if(subject != null) {
            val test_param = TestATB.getExpFactorsType()
            testCode = test_param.first
            testName = test_param.second
            startATBtest()
        }
    }

    private fun onTIDSubjectUpdated(){

        if(subject != null) {
            val test_param = TestTID.getExpFactorsType(subject as SubjectTIDParcel)
            testCode = test_param.first
            testName = test_param.second
            startTIDtest()
        }
    }

    private fun onBISSubjectUpdated(){

        if(subject != null) {
            val test_param = TestBIS.getExpFactorsType(subject as SubjectBasicListParcel)
            testCode = test_param.first
            testName = test_param.second
            startBIStest()
        }
    }

    private fun onMusMetSubjectUpdated(){

        if(subject != null) {
            val test_param = TestMusMet.getExpFactorsType()
            testCode = test_param.first
            testName = test_param.second
            startMusMettest()
        }
    }
    //================================================================================================================
    // 4 - START TESTS
    //================================================================================================================
    private fun startATBtest(){
        val bundle = Bundle()
        bundle.putParcelable("test",TestParcel(testCode, testName, subject!!.label,
                                                session = TestBasic.TEST_PRE,
                                                nextTrailModality = subject!!.nextTrailModality))

        Navigation.findNavController(bt_start_bisection).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }

    private fun startTIDtest(){
        val bundle = Bundle()
        bundle.putParcelable("test",TestParcel(testCode, testName, subject!!.label,
                                                session = (subject!! as SubjectTIDParcel).session,
                                                nextTrailModality = subject!!.nextTrailModality))

        Navigation.findNavController(bt_start_tid_test).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }

    private fun startBIStest(){
        val bundle = Bundle()
        bundle.putParcelable("test", TestParcel( testCode, testName, subject!!.label,
                                                 nextTrailModality = subject!!.nextTrailModality))

        Navigation.findNavController(bt_start_bisection).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }

    private fun startMusMettest(){
        val bundle = Bundle()
        bundle.putParcelable("test", TestParcel( testCode, testName, subject!!.label,
                                                 nextTrailModality = subject!!.nextTrailModality))

        Navigation.findNavController(bt_start_bisection).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }
}