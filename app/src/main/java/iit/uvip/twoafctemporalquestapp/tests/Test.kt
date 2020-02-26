package iit.uvip.twoafctemporalquestapp.tests

import android.content.Context
import android.os.Handler

import java.util.*
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.twoafctemporalquestapp.utility.deleteFile
import iit.uvip.twoafctemporalquestapp.utility.saveText

abstract class Test(protected val ctx: Context, protected val data: TestData) {

    companion object {

        @JvmStatic val TEST_BISECTION_AUDIO             = 100
        @JvmStatic val TEST_BISECTION_TACTILE           = 101
        @JvmStatic val TEST_BISECTION_AUDIO_TACTILE     = 102
        @JvmStatic val TEST_BISECTION_AUDIO_VIDEO       = 103
        @JvmStatic val TEST_MUSICAL_METERS              = 110

        @JvmStatic val TEST_TID_SHORT_AUDIO             = 120
        @JvmStatic val TEST_TID_SHORT_TACTILE           = 121
        @JvmStatic val TEST_TID_LONG_AUDIO              = 122
        @JvmStatic val TEST_TID_LONG_TACTILE            = 123

        @JvmStatic val TEST_PRE                         = 130
        @JvmStatic val TEST_POST                        = 131
        @JvmStatic val TEST_TRAINING                    = 132

        @JvmStatic val EVENT_STIMULI_START              = 200
        @JvmStatic val EVENT_STIMULI_END                = 201
        @JvmStatic val EVENT_ANSWER_GIVEN               = 202
        @JvmStatic val EVENT_TRIAL_REPEAT               = 203
        @JvmStatic val EVENT_TRIAL_ABORT                = 204

        @JvmStatic val EVENT_TEST_END                   = 205
    }

    // they are just proxy for properties (implemented / edited) in each subclass

    val testEvent:PublishRelay<Int> = PublishRelay.create()
    var mQuestion:String            = ""

    var mAnswer1:String = ""
    var mAnswer2:String = ""
    var mAnswer3:String = ""

    protected var mTrials:MutableList<Trial>    = mutableListOf()
    var nTrials:Int                             = 0
    protected var currTrial:Int                 = 0
    protected lateinit var mTrial:Trial

    protected var mResultFile: String       = ""
    protected var mStimuliHandler: Handler  = Handler()

    protected abstract fun initTest()
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

    open fun nextTrial(prev_result:Int, elapsed:Int):Int{

        if(currTrial == (nTrials - 1))
        {
            // END !
            setResponse(prev_result, elapsed, true, true)
            return EVENT_TEST_END
        }
        else
        {
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

        deleteFile(mResultFile)
    }
}