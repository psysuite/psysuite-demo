package iit.uvip.audiotactilebindingapp.fragments


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.navigation.Navigation
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import iit.uvip.audiotactilebindingapp.R
import iit.uvip.audiotactilebindingapp.tests.*
import iit.uvip.audiotactilebindingapp.utility.showToast
import kotlinx.android.synthetic.main.fragment_test.*

/*
Three operative modalities:

- trial have an answer dialog, where user can also abort
- trial have no answer dialog, at the end of the trial, the following trial is displayed
- trial have no answer dialog, at the end of the trial, test stops and wait for user press.

 */

class TestFragment : BaseFragment(
    layout = R.layout.fragment_test,
    landscape = false,
    hideAndroidControls = true
){

    private lateinit var mTest:TestBasic
    override val LOG_TAG                            = TestFragment::class.java.simpleName
    private val disposable                          = CompositeDisposable()
    private val TARGET_FRAGMENT_REQUEST_CODE:Int    = 1
    private var isAnswerDialogOn:Boolean            = false
    private var currTrial:Int                       = 0

    private var isPaused:Boolean                    = false
    private var mHandler: Handler                   = Handler()
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

        val test:TestParcel? = arguments?.getParcelable("test") ?: return
        when(test!!.type)
        {
            TestBasic.TEST_BISECTION_AUDIO,
            TestBasic.TEST_BISECTION_TACTILE,
            TestBasic.TEST_BISECTION_AUDIO_TACTILE,
            TestBasic.TEST_BISECTION_AUDIO_VIDEO        -> mTest = TestBisection(requireContext(), test, circleView)

            TestBasic.TEST_MUSICAL_METERS               -> mTest = TestMusicalMeter(requireContext(), test)

            TestBasic.TEST_TID_SHORT_AUDIO,
            TestBasic.TEST_TID_SHORT_TACTILE,
            TestBasic.TEST_TID_LONG_AUDIO,
            TestBasic.TEST_TID_LONG_TACTILE             -> mTest = TestTID(requireContext(), test)

            TestBasic.TEST_ATB                          -> mTest = TestATBinding(requireContext(), test)

        }
        bt_next.visibility  = View.INVISIBLE
        bt_abort.visibility = View.INVISIBLE
        bt_pause.visibility = View.INVISIBLE

        currTrial = 0
        if (mTest.showTrialsID == TestBasic.TEST_SHOWTRIALS_ALWAYS) showTrialId(false)
        if (mTest.abortMode == TestBasic.TEST_ABORT_ALWAYS){
            bt_abort.visibility = View.VISIBLE
            bt_pause.visibility = View.VISIBLE
        }

        mTest.show(currTrial)
    }

    override fun onResume() {
        super.onResume()

        setEventsFlow()

        bt_abort.setOnClickListener{
            onAbortTest()
        }

        bt_pause.setOnClickListener{
            if(isPaused){
                bt_pause.text = resources.getString(R.string.pause)
                bt_pause.visibility = View.INVISIBLE
                mTest.nextTrial()
                if(mTest.abortMode != TestBasic.TEST_ABORT_ALWAYS)  bt_abort.visibility = View.INVISIBLE
            }
            else{
                mHandler.removeCallbacksAndMessages(null)
                bt_pause.text = resources.getString(R.string.resume)
            }
            isPaused = !isPaused
        }
    }

    override fun onPause(){
        super.onPause()
        disposable.clear()
    }

    private fun onTestEnded(){
        showToast(getText(R.string.test_ended).toString(), requireContext())
        Navigation.findNavController(requireView()).navigate(R.id.action_testFragment_to_mainFragment)
    }

    private fun onAbortTest(){
        mTest.abortTest()
        mHandler.removeCallbacksAndMessages(null)
        Navigation.findNavController(requireView()).navigate(R.id.action_testFragment_to_mainFragment)
    }

    // here I manage all trial-by-trial behaviours
    private fun setEventsFlow(){
        mTest.testEvent
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            when(it){
                TestBasic.EVENT_STIMULI_START               -> {}
                TestBasic.EVENT_STIMULI_END                 -> mTest.onTrialEnd()
                TestBasic.EVENT_GIVE_ANSWER                 -> showAnswerDialog()
                TestBasic.EVENT_SHOW_NEXT_BUTTON            -> showNext()
                TestBasic.EVENT_UPDATE_TRIAL_ID             -> showTrialId(false)
                TestBasic.EVENT_UPDATE_TRIAL_ID_REMOVE      -> showTrialId(true)
                TestBasic.EVENT_SHOW_1SECABORT             -> showShortAbort()
            }
        }
        .addTo(disposable)

        // button is shown when an answer dialog is not displayed
        bt_next.setOnClickListener{

            bt_next.visibility      = View.INVISIBLE
            bt_pause.visibility     = View.INVISIBLE

            currTrial               = mTest.nextTrial()

            if(currTrial == TestBasic.EVENT_TEST_END) onTestEnded()
            else {
                when(mTest.showTrialsID) {
                    TestBasic.TEST_SHOWTRIALS_ALWAYS    -> showTrialId(false)
                    TestBasic.TEST_SHOWTRIALS_TRIALEND  -> showTrialId(true)
                }
                if(mTest.abortMode == TestBasic.TEST_ABORT_TRIALEND){
                    bt_abort.visibility = View.INVISIBLE
                }
            }
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    private fun showNext(remove:Boolean=false){
        bt_next.visibility = View.VISIBLE

        if(mTest.abortMode == TestBasic.TEST_ABORT_ALWAYS || mTest.abortMode == TestBasic.TEST_ABORT_TRIALEND)
            bt_abort.visibility = View.VISIBLE
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    private fun showShortAbort(){
        bt_abort.visibility = View.VISIBLE
        bt_pause.visibility = View.VISIBLE

        mHandler.postDelayed({
            bt_abort.visibility = View.INVISIBLE
            bt_pause.visibility = View.INVISIBLE
            mTest.nextTrial()
        }, 1000L)
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    private fun showTrialId(remove:Boolean=false){
        txtTrialId.visibility   = View.VISIBLE
        txtTrialId.text         = resources.getString(R.string.trial_id, (mTest.currTrial + 1).toString())
        if(remove){
            mHandler.postDelayed({
                txtTrialId.visibility = View.INVISIBLE
            }, 1000L)
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------
    // create answer dialog and process response (repeat same trial or show next one
    private fun showAnswerDialog(){

        val b = Bundle()
        b.putInt("trial_id",    currTrial)
        b.putInt("tot_trials",  mTest.nTrials)
        b.putString("question", mTest.mQuestion)
        b.putString("answer1",  mTest.mAnswer1)
        b.putString("answer2",  mTest.mAnswer2)
        b.putString("answer3",  mTest.mAnswer3)

        val editNameDialogFragment = AnswerDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_REQUEST_CODE)
        editNameDialogFragment.arguments = b
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(requireFragmentManager(), "Inserisci risposta")
        isAnswerDialogOn = true
    }

    // answer !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        // Make sure fragment codes match up
        if(requestCode == TARGET_FRAGMENT_REQUEST_CODE)
        {
            when(data?.getIntExtra(EVENT_ANSWER_CODE, 0))
            {
                TestBasic.EVENT_ANSWER_GIVEN -> {
                    val result      = data.getIntExtra(EVENT_ANSWER_RESULT, -1)
                    val elapsedTime = data.getIntExtra(EVENT_TIME_TO_ANSWER, -1)

                    // call next trial & check whether it was the last => test ended
                    if(mTest.nextTrial(result, elapsedTime) == TestBasic.EVENT_TEST_END)    onTestEnded()
                }
                TestBasic.EVENT_TRIAL_REPEAT    -> mTest.show(currTrial, true)
                TestBasic.EVENT_TRIAL_ABORT     -> onAbortTest()
            }
        }
    }
    // ========================================================================================================================================
}