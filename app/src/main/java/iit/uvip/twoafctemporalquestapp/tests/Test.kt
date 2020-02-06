package iit.uvip.twoafctemporalquestapp.tests

import android.content.Context
import android.os.Handler

import java.util.*
import com.jakewharton.rxrelay2.PublishRelay
import iit.uvip.twoafctemporalquestapp.deleteFile
import iit.uvip.twoafctemporalquestapp.saveData


abstract class Test(protected val ctx: Context, val mType:Int, val mSubjLabel: String="result") {

    // they are just proxy for properties (implemented / edited) in each subclass
    abstract fun show(trialid:Int, isRepeat:Boolean=false)
    val testEvent:PublishRelay<Int> = PublishRelay.create()
    var mQuestion:String            = ""
    var nTrials:Int                 = 0

    protected abstract fun initTest()

    companion object {

        @JvmStatic val TEST_BISECTION_AUDIO             = 100
        @JvmStatic val TEST_BISECTION_TACTILE           = 101
        @JvmStatic val TEST_BISECTION_AUDIO_TACTILE     = 102
        @JvmStatic val TEST_BISECTION_AUDIO_VIDEO       = 103
        @JvmStatic val TEST_MUSICAL_METERS              = 110


        @JvmStatic val EVENT_STIMULI_START              = 200
        @JvmStatic val EVENT_STIMULI_END                = 201
        @JvmStatic val EVENT_ANSWER_GIVEN               = 202
        @JvmStatic val EVENT_TRIAL_REPEAT               = 203
        @JvmStatic val EVENT_TRIAL_ABORT                = 204

        @JvmStatic val EVENT_TEST_END                   = 205
    }

    var mAnswer1:String = ""
    var mAnswer2:String = ""
    var mAnswer3:String = ""

    protected var mTrials:MutableList<Trial>          = mutableListOf()

    protected lateinit var mTrial:Trial
    protected var currTrial:Int             = 0
    protected var mResultFile: String       = ""
    protected var mStimuliHandler: Handler  = Handler()

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

        saveData(ctx, mResultFile, header)
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
            saveData(ctx, mResultFile, mTrial.Log(), notifyDM)
    }

    open fun abortTest(){

        deleteFile(mResultFile)
    }
}