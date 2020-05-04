package iit.uvip.audiotactilebindingapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import iit.uvip.audiotactilebindingapp.R
import iit.uvip.audiotactilebindingapp.subjects.SubjectATBParcel
import iit.uvip.audiotactilebindingapp.subjects.SubjectBasicParcel
import iit.uvip.audiotactilebindingapp.subjects.SubjectTIDParcel
import iit.uvip.audiotactilebindingapp.subjects.SubjectsBasic
import iit.uvip.audiotactilebindingapp.tests.TestATBinding
import iit.uvip.audiotactilebindingapp.tests.TestParcel
import iit.uvip.audiotactilebindingapp.tests.TestBasic
import iit.uvip.audiotactilebindingapp.tests.TestTID
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : BaseFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
)
{
    private lateinit var mSubjects: SubjectsBasic
    private var subject: SubjectBasicParcel? = null

    override val LOG_TAG:String = MainFragment::class.java.simpleName

    private var trainingTestName:String = ""
    private var trainingTestCode:Int = -1

    companion object {
        @JvmStatic val TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE: Int    = 1
        @JvmStatic val TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE: Int    = 2
        @JvmStatic val EVENT_SUBJECT:String                             = "subject"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mSubjects = SubjectsBasic(requireContext())
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        subject = mSubjects.loadSubject()

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        bt_start_tid_test.setOnClickListener{
            showTIDSubjectDialog()
        }

        bt_start_atb_test.setOnClickListener {
            showATBSubjectDialog()
        }
    }

    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================
    private fun showATBSubjectDialog(){

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectATBDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(requireFragmentManager(), "Modifica Soggetto")
    }

    private fun showTIDSubjectDialog(){

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectTIDDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE)
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
        when(requestCode){
            TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(EVENT_SUBJECT) ?: subject
                if (subject != null) mSubjects.writeJson(subj = subject)
                onATBSubjectUpdated()
            }

            TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(EVENT_SUBJECT) ?: subject
                if (subject != null) mSubjects.writeJson(subj = subject)
                onTIDSubjectUpdated()
            }
        }
    }
    //================================================================================================================
    // 3 - SUBJECT DATA INSERTED
    //================================================================================================================
    private fun onATBSubjectUpdated(){

        if(subject != null) {
            val test_param = TestATBinding.getExpFactorsType()
            trainingTestCode = test_param.first
            trainingTestName = test_param.second
            startATBtest()
        }
    }

    private fun onTIDSubjectUpdated(){

        if(subject != null) {
            val test_param = TestTID.getExpFactorsType(subject as SubjectTIDParcel)
            trainingTestCode = test_param.first
            trainingTestName = test_param.second
        }
    }
    //================================================================================================================
    // 4 - START TESTS
    //================================================================================================================
    private fun startATBtest(){
        val bundle = Bundle()
        bundle.putParcelable("test", TestParcel(trainingTestCode, trainingTestName, subject!!.label, session = TestBasic.TEST_PRE, nextTrailModality = (subject!! as SubjectATBParcel).nextTrailModality))
        Navigation.findNavController(bt_start_atb_test).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }

    private fun startTIDtest(){
        val bundle = Bundle()
        bundle.putParcelable("test", TestParcel(trainingTestCode, trainingTestName, subject!!.label, session = (subject!! as SubjectTIDParcel).session, nextTrailModality = TestBasic.TEST_NEXTTRIAL_ANSWER))
        Navigation.findNavController(bt_start_atb_test).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }
}