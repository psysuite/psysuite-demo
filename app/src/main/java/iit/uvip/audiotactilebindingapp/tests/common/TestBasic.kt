package iit.uvip.audiotactilebindingapp.tests.common

import android.content.Context
import android.os.Handler

import java.util.*
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.audiotactilebindingapp.MainApplication
import iit.uvip.audiotactilebindingapp.utility.deleteFile
import iit.uvip.audiotactilebindingapp.utility.saveText

/*
must contain all the possible codes

 */

abstract class TestBasic(protected val ctx: Context, protected val data: TestParcel) {

    val application:MainApplication = ctx.applicationContext as MainApplication

    companion object {

        @JvmStatic val TEST_BASIC_LABEL                 = "test"    // used by tests that have only one type

        @JvmStatic val TEST_SHOWTRIALS_NEVER            = 0         //  SHOWTRIALS_NEVER
        @JvmStatic val TEST_SHOWTRIALS_TRIALEND         = 1         //  SHOWTRIALS_TRIALEND
        @JvmStatic val TEST_SHOWTRIALS_ALWAYS           = 2         //  SHOWTRIALS_ALWAYS

        @JvmStatic val TEST_ABORT_ANSWER                = 0         //  SHOWTRIALS_NEVER
        @JvmStatic val TEST_ABORT_TRIALEND              = 1         //  SHOWTRIALS_TRIALEND
        @JvmStatic val TEST_ABORT_ALWAYS                = 2         //  SHOWTRIALS_ALWAYS

        @JvmStatic val TEST_NEXTTRIAL_NOCHOOSE          = -1        //  goes directly to next trial
        @JvmStatic val TEST_NEXTTRIAL_AUTO              = 0         //  goes directly to next trial
        @JvmStatic val TEST_NEXTTRIAL_BUTTON            = 1         //  wait for NEXT press
        @JvmStatic val TEST_NEXTTRIAL_ANSWER            = 2         //  wait for ANSWER dialog


        @JvmStatic val TEST_BISECTION_AUDIO             = 100
        @JvmStatic val TEST_BISECTION_TACTILE           = 101
        @JvmStatic val TEST_BISECTION_AUDIO_TACTILE     = 102
        @JvmStatic val TEST_BISECTION_AUDIO_VIDEO       = 103

        @JvmStatic val TEST_MUSICAL_METERS              = 110

        @JvmStatic val TEST_TID_SHORT_AUDIO             = 120
        @JvmStatic val TEST_TID_SHORT_TACTILE           = 121
        @JvmStatic val TEST_TID_LONG_AUDIO              = 122
        @JvmStatic val TEST_TID_LONG_TACTILE            = 123

        @JvmStatic val TEST_ATB                         = 130

        @JvmStatic val EVENT_STIMULI_START              = 200
        @JvmStatic val EVENT_STIMULI_END                = 201
        @JvmStatic val EVENT_GIVE_ANSWER                = 202
        @JvmStatic val EVENT_ANSWER_GIVEN               = 203
        @JvmStatic val EVENT_TRIAL_REPEAT               = 204
        @JvmStatic val EVENT_TRIAL_ABORT                = 205
        @JvmStatic val EVENT_TEST_END                   = 206
        @JvmStatic val EVENT_SHOW_NEXT_BUTTON           = 207
        @JvmStatic val EVENT_UPDATE_TRIAL_ID            = 208
        @JvmStatic val EVENT_UPDATE_TRIAL_ID_REMOVE     = 209   // update trial id and remove it after 1 sec
        @JvmStatic val EVENT_SHOW_1SECABORT             = 210   // show abort button for 1 sec


        @JvmStatic val TEST_PRE                         = 230
        @JvmStatic val TEST_POST                        = 231
        @JvmStatic val TEST_TRAINING                    = 232

    }
    // they are just proxy for properties (implemented / edited) in each subclass

    val testEvent:PublishRelay<Int> = PublishRelay.create()
    var mQuestion:String            = ""

    var mAnswer1:String = ""
    var mAnswer2:String = ""
    var mAnswer3:String = ""

    var showTrialsID:Int        = 0     // define when display trial id(0: never, 1: only @ trial end, 2: always)
    var abortMode:Int           = 0     // define abort modality (0:in answer dialog @ trial end, 1:button @ trial end, 2:always)
    var nextTrailModality:Int   = 0     // define how trials are displayed. 0: automatically, 1: after a next button, 2: after answer

    protected var mTrials:MutableList<TrialBasic>    = mutableListOf()
    var nTrials:Int                             = 0
    var currTrial:Int                           = 0
    protected lateinit var mTrial: TrialBasic

    protected var mResultFile: String       = ""
    protected var mStimuliHandler: Handler  = Handler()

    protected abstract fun initTest()

    abstract fun onTrialEnd()
    abstract fun show(trialid:Int, isRepeat:Boolean=false)

    // ===============================================================================================================
    protected fun createResultFile(subj_label:String, header:String){

        val c = Calendar.getInstance()
        mResultFile = subj_label + "_" +
                c.get(Calendar.YEAR).toString() +
                c.get(Calendar.MONTH).toString() +
                c.get(Calendar.DAY_OF_MONTH).toString() +
                c.get(Calendar.HOUR).toString() +
                c.get(Calendar.MINUTE).toString() +
                c.get(Calendar.SECOND).toString()
        mResultFile += ".txt"

        saveText(ctx, mResultFile, header)
    }

    open fun nextTrial(prev_result:Int=-1, elapsed:Int=-1):Int{

        if(currTrial == (nTrials - 1))
        {
            // END !
            if(prev_result > -1)
                setResponse(prev_result, elapsed, true, true)
            return EVENT_TEST_END
        }
        else
        {
            if(prev_result > -1)
                setResponse(prev_result, elapsed, true, false)
            currTrial++
            show(currTrial)
        }
        return currTrial
    }

    // calculate test result (== 0 first button || == 1 second button)
    open fun setResponse(result:Int, elapsed:Int, writeit:Boolean, notifyDM:Boolean){
        mTrial.setResponse(result, elapsed)

        if(writeit)
            saveText(ctx, mResultFile, mTrial.Log(), notifyDm = notifyDM)
    }

    open fun abortTest(){
        mStimuliHandler.removeCallbacksAndMessages(null)
        deleteFile(mResultFile)
    }
}