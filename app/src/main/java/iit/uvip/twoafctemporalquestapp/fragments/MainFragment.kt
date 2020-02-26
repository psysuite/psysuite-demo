package iit.uvip.twoafctemporalquestapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import iit.uvip.twoafctemporalquestapp.R
import iit.uvip.twoafctemporalquestapp.subjects.SubjectData
import iit.uvip.twoafctemporalquestapp.subjects.Subjects
import iit.uvip.twoafctemporalquestapp.tests.TestData
import iit.uvip.twoafctemporalquestapp.tests.TIDTest
import iit.uvip.twoafctemporalquestapp.tests.Test
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : BaseFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
)
{
    private lateinit var mSubjects: Subjects
    private var subject: SubjectData? = null

    override val LOG_TAG:String = MainFragment::class.java.simpleName

    private var trainingTestName:String = ""
    private var trainingTestCode:Int = -1

    companion object {
        @JvmStatic val TARGET_FRAGMENT_REQUEST_CODE: Int    = 1
        @JvmStatic val EVENT_SUBJECT:String                 = "subject"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mSubjects = Subjects(context!!)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        subject = mSubjects.loadSubject()
        onSubjectUpdated()

        activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        bt_insert_subject.setOnClickListener{
            showSubjectDialog()
        }

        bt_start_pre_test.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("test", TestData(trainingTestCode, trainingTestName, subject!!.label, Test.TEST_PRE))
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_testFragment, bundle)
        }

        bt_start_training.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("test",TestData(trainingTestCode, trainingTestName, subject!!.label, Test.TEST_TRAINING, 1))
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_testFragment, bundle)
        }

        bt_start_post_test.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("test", TestData(trainingTestCode, trainingTestName, subject!!.label, Test.TEST_POST))
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_testFragment, bundle)
        }
    }

    private fun onSubjectUpdated(){
        bt_start_post_test.isEnabled    = (subject != null)
        bt_start_pre_test.isEnabled     = (subject != null)
        bt_start_pre_test.isEnabled     = (subject != null)

        if(subject != null) {
            trainingTestCode = subject!!.getType()
            trainingTestName = TIDTest.getTestName(trainingTestCode)
        }
    }

    private fun showSubjectDialog(){

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(fragmentManager!!, "Modifica Soggetto")
    }

    // subject info !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        // Make sure fragment codes match up
        if(requestCode == TARGET_FRAGMENT_REQUEST_CODE)
            subject = data?.getParcelableExtra(EVENT_SUBJECT) ?: subject

        if(subject != null) mSubjects.writeJson(subj=subject)
        onSubjectUpdated()
    }

}