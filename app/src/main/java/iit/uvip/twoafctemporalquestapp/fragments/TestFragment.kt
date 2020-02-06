package iit.uvip.twoafctemporalquestapp.fragments


import android.content.Intent
import android.os.Bundle
import androidx.navigation.Navigation
import iit.uvip.twoafctemporalquestapp.tests.BisectionTest
import iit.uvip.twoafctemporalquestapp.tests.MusicalMeterTest
import iit.uvip.twoafctemporalquestapp.tests.Test
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import iit.uvip.twoafctemporalquestapp.R
import iit.uvip.twoafctemporalquestapp.showToast
import kotlinx.android.synthetic.main.fragment_test.*


class TestFragment : BaseFragment(
    layout = R.layout.fragment_test,
    landscape = false,
    hideAndroidControls = true
){

    override val LOG_TAG = TestFragment::class.java.simpleName

    private val disposable = CompositeDisposable()

    private val TARGET_FRAGMENT_REQUEST_CODE:Int    = 1

    private lateinit var mTest: Test

    private var isAnswerDialogOn: Boolean = false

    private var mTestType: Int = 0
    private var mTestName: String = ""
    private var mSubjectID: String = ""

    private var currTrial:Int = 0
    // ==========================================================================================================================
    // ==========================================================================================================================

    companion object {

        @JvmStatic val EVENT_ANSWER_CODE:String     = "answer_code"
        @JvmStatic val EVENT_ANSWER_RESULT:String   = "answer_result"
        @JvmStatic val EVENT_TIME_TO_ANSWER:String  = "answer_time"

        fun newIntent(resp_code: Int, elapsedTime:Int, resp_id:Int?): Intent {
            val intent = Intent()
            intent.putExtra(EVENT_ANSWER_CODE, resp_code)
            intent.putExtra(EVENT_TIME_TO_ANSWER, elapsedTime)
            intent.putExtra(EVENT_ANSWER_RESULT, resp_id)
            return intent
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        mTestType   = arguments?.getString("test_type").toString().toInt()
        mTestName   = arguments?.getString("test_name").toString()
        mSubjectID  = arguments?.getString("subject_id").toString()

        when(mTestType)
        {
            Test.TEST_BISECTION_AUDIO,
            Test.TEST_BISECTION_TACTILE,
            Test.TEST_BISECTION_AUDIO_TACTILE,
            Test.TEST_BISECTION_AUDIO_VIDEO      -> mTest = BisectionTest(context!!, mTestType, mTestName + "_" + mSubjectID, circleView)
            Test.TEST_MUSICAL_METERS             -> mTest = MusicalMeterTest(context!!, mTestType)
        }
        currTrial = 0
        mTest.show(currTrial)
    }

    override fun onResume() {
        super.onResume()
        mTest.testEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when(it)
                {
                    Test.EVENT_STIMULI_END -> showAnswerDialog()
                    Test.EVENT_STIMULI_START -> {}
                }
            }
            .addTo(disposable)
//        mTest.show(currTrial)
    }

    override fun onPause(){
        super.onPause()
        disposable.clear()
    }

    //---------------------------------------------------------------------------------------------------------------------------------------
    // create answer dialog and process response (repeat same trial or show next one
    private fun showAnswerDialog(){

        val b = Bundle()
        b.putInt("trial_id", currTrial)
        b.putInt("tot_trials", mTest.nTrials)
        b.putString("question", mTest.mQuestion)
        b.putString("answer1", mTest.mAnswer1)
        b.putString("answer2", mTest.mAnswer2)
        b.putString("answer3", mTest.mAnswer3)

        val editNameDialogFragment = AnswerDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_REQUEST_CODE)
        editNameDialogFragment.arguments = b
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(fragmentManager!!, "Inserisci risposta")
        isAnswerDialogOn = true
    }

    // answer !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        // Make sure fragment codes match up
        if(requestCode == TARGET_FRAGMENT_REQUEST_CODE)
        {
            when(data?.getIntExtra(EVENT_ANSWER_CODE, 0))
            {
                Test.EVENT_ANSWER_GIVEN -> {
                    val result      = data.getIntExtra(EVENT_ANSWER_RESULT, -1)
                    val elapsedTime = data.getIntExtra(EVENT_TIME_TO_ANSWER, -1)
                    currTrial = mTest.nextTrial(result, elapsedTime)
                    if(currTrial == Test.EVENT_TEST_END)
                    {
                        showToast(getText(R.string.test_ended).toString(), context!!)
                        Navigation.findNavController(view!!).navigate(R.id.action_testFragment_to_mainFragment)
                    }
                }
                Test.EVENT_TRIAL_REPEAT -> {
                    mTest.show(currTrial, true)
                }
                Test.EVENT_TRIAL_ABORT -> {
                    mTest.abortTest()
                    fragmentManager?.popBackStack()
                }
            }
        }
    }


    // ========================================================================================================================================

}