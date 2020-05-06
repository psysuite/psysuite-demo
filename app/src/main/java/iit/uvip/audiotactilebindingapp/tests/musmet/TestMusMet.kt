package iit.uvip.audiotactilebindingapp.tests.musmet

import android.content.Context
import iit.uvip.audiotactilebindingapp.R
import android.media.MediaPlayer
import iit.uvip.audiotactilebindingapp.tests.common.TestBasic
import iit.uvip.audiotactilebindingapp.tests.common.TestParcel

class TestMusMet(ctx: Context, data: TestParcel) : TestBasic(ctx, data)
{
    var LOG_TAG:String = TestMusMet::class.java.simpleName

    companion object {
        @JvmStatic val NUM_TRIALS = 18
        @JvmStatic val TEST_BASIC_LABEL                 = "MusMet"

        fun getExpFactorsType():Pair<Int, String> {
            return Pair(
                TEST_MUSICAL_METERS,
                TEST_BASIC_LABEL
            )
        }
    }

    // =============================================================================================================================

    init{

        mAnswer1        = ctx.resources.getString(R.string.mmeters_rb1_text)
        mAnswer2        = ""
        mAnswer3        = ctx.resources.getString(R.string.mmeters_rb3_text)

        initTest()
    }

    override fun initTest(){
        // set question & create mTrials list
        mQuestion = ctx.resources.getString(R.string.mmeters_question_text)
        createTrials()

        nTrials     = mTrials.size
        currTrial   = 0

        createResultFile(data.subject_id, TrialMusMet.LOG_HEADER)
    }

    override fun show(trialid:Int, isRepeat:Boolean){
        mTrial = mTrials[trialid]

        if(isRepeat)    mTrial.repetitions++

        val resname = when(mTrial.type == 0){
            true    -> "mmc" + (mTrial as TrialMusMet).audio_id + "_same"
            false   -> "mmc" + (mTrial as TrialMusMet).audio_id
        }
        deliverStimulus(resname)
    }

    override fun onTrialEnd(){
        testEvent.accept(EVENT_GIVE_ANSWER)
    }

    private fun deliverStimulus(resname:String){

        val mediaPlayer = MediaPlayer.create(ctx, ctx.resources.getIdentifier(resname, "raw", ctx.packageName))
        mediaPlayer.setOnCompletionListener{
            testEvent.accept(EVENT_STIMULI_END)
        }
        mediaPlayer.start()
    }

    // class Trial(var id:Int=-1, val type:Int, val label:String, var audio_id:Int, var correct_answer:Int=-1, var user_answer:Int=-1,
    //                 var success:Boolean=false, var elapsed:Int=-1, var repetitions:Int=1)
    private fun createTrials()
    {
        for(i in 1 until (NUM_TRIALS +1) ){
            mTrials.add(
                TrialMusMet(
                    -1,
                    0,
                    "same",
                    i
                )
            )
            mTrials.add(
                TrialMusMet(
                    -1,
                    1,
                    "diff",
                    i
                )
            )
        }
        mTrials.shuffle()

        // set trial id according to its order in the list
        for(i in 0 until mTrials.size)
            mTrials[i].id = (i + 1)
    }
}