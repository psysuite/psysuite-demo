package iit.uvip.audiotactilebindingapp.tests.atb

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import iit.uvip.audiotactilebindingapp.tests.common.TestBasic
import iit.uvip.audiotactilebindingapp.tests.common.TestParcel

/*
onTrialEnd()
*/
class TestATB(ctx: Context, data: TestParcel) : TestBasic(ctx, data)
{
    var LOG_TAG:String = TestATB::class.java.simpleName


//    var vibrator:Vibrator   // lateinit not necessary as initialized in construtor
    private var mToneGen    = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
    private var mTone       = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE

    private val TYPE_A_T    = 0
    private val TYPE_A200_T = 1
    private val TYPE_A_T200 = 2
    private val TYPE_A      = 3
    private val TYPE_A500_T = 4
    private val TYPE_A_T500 = 5
    private val TYPE_T      = 6
    private val TYPE_A800_T = 7
    private val TYPE_A_T800 = 8

    private val TYPE_AUDIO          = 0
    private val TYPE_TACTILE        = 1
    private val TYPE_AUDIOTACTILE   = 2

    private val STIM_DURATION       = 1000
    private val ISI                 = 1000
    private val ITI                 = 2000

    private val EVENT_SECOND_TRAIN  = 1201

    companion object {

        @JvmStatic val TEST_BASIC_LABEL                 = "ATB"

        @JvmStatic val NUM_REPETITIONS = 3

        fun getExpFactorsType():Pair<Int, String>{
            return Pair(
                TEST_ATB,
                TEST_BASIC_LABEL
            )
        }
    }

    // =============================================================================================================================

    init{

        nextTrailModality   = data.nextTrailModality
        abortMode           =
            TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID        =
            TEST_SHOWTRIALS_ALWAYS    // trial id always shown

//        val application     = ctx.applicationContext as MainApplication
//        vibrator            = application.vibrator
        initTest()
    }

    override fun initTest(){
        // mTrials list
        createTrials()

        nTrials     = mTrials.size
        currTrial   = 0

        createResultFile(data.subject_id,
            TrialATB.LOG_HEADER
        )
    }

    override fun show(trialid:Int, isRepeat:Boolean){
        mTrial = mTrials[trialid]

        if(isRepeat)    mTrial.repetitions++

        firstTrain()                // schedule first 5 stimuli
        secondTrain(mTrial.type)    // schedule second 3 stimuli
    }

    override fun nextTrial(prev_result: Int, elapsed: Int): Int {
        testEvent.accept(EVENT_UPDATE_TRIAL_ID)
        return super.nextTrial(prev_result, elapsed)
    }

    // called by secondTrain
    override fun onTrialEnd(){
        if(nextTrailModality == TEST_NEXTTRIAL_BUTTON)
            testEvent.accept(EVENT_SHOW_NEXT_BUTTON)
        else{
            // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
            mStimuliHandler.postDelayed({
                testEvent.accept(EVENT_SHOW_1SECABORT)
            }, ISI.toLong())
        }
    }

    private fun firstTrain(){

        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIOTACTILE)
            testEvent.accept(EVENT_STIMULI_START)
        }, ISI.toLong())
        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIOTACTILE)
        }, 2*ISI.toLong())
        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIOTACTILE)
        }, 3*ISI.toLong())
        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIOTACTILE)
        }, 4*ISI.toLong())
        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIOTACTILE)
        }, 5*ISI.toLong())
    }

    private fun deliverStimulus(type:Int){

        when(type) {
            TYPE_AUDIO            ->    mToneGen.startTone(mTone, STIM_DURATION)
            TYPE_TACTILE          ->    application.vibrate(STIM_DURATION.toLong())
            TYPE_AUDIOTACTILE     -> {
                                        mToneGen.startTone(mTone, STIM_DURATION)
                                        application.vibrate(STIM_DURATION.toLong())
            }
        }
    }

    // class Trial(var id:Int=-1, val type:Int, val label:String, var audio_id:Int, var correct_answer:Int=-1, var user_answer:Int=-1,
    //                 var success:Boolean=false, var elapsed:Int=-1, var repetitions:Int=1)
    private fun createTrials()
    {
        var cnt     = -1
        for(i in 0 until NUM_REPETITIONS) {
            mTrials.add(TrialATB(++cnt, TYPE_A_T))
            mTrials.add(TrialATB(++cnt, TYPE_A_T200))
            mTrials.add(TrialATB(++cnt, TYPE_A))
            mTrials.add(TrialATB(++cnt, TYPE_A800_T))
            mTrials.add(TrialATB(++cnt, TYPE_T))
            mTrials.add(TrialATB(++cnt, TYPE_A_T500))
            mTrials.add(TrialATB(++cnt, TYPE_A))
            mTrials.add(TrialATB(++cnt, TYPE_A200_T))
            mTrials.add(TrialATB(++cnt, TYPE_A_T))
            mTrials.add(TrialATB(++cnt, TYPE_A_T800))
            mTrials.add(TrialATB(++cnt, TYPE_T))
            mTrials.add(TrialATB(++cnt, TYPE_A500_T))
        }
    }

    private fun secondTrain(type:Int){

        when(type){
            TYPE_A_T    -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIOTACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIOTACTILE)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIOTACTILE)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 9*ISI.toLong())
            }
            TYPE_A      -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 9*ISI.toLong())
            }
            TYPE_T      -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 9*ISI.toLong())
            }
            TYPE_A_T200 -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (6*ISI + 200).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (7*ISI + 200).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (8*ISI + 200).toLong())

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (9*ISI + 200).toLong())
            }
            TYPE_A_T500 -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (6*ISI + 500).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (7*ISI + 500).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (8*ISI + 500).toLong())

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (9*ISI + 500).toLong())
            }
            TYPE_A_T800 -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (6*ISI + 800).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (7*ISI + 800).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (8*ISI + 800).toLong())

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (9*ISI + 800).toLong())
            }
            TYPE_A200_T -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (6*ISI + 200).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (7*ISI + 200).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (8*ISI + 200).toLong())

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (9*ISI + 200).toLong())

            }
            TYPE_A500_T -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (6*ISI + 500).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (7*ISI + 500).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (8*ISI + 500).toLong())

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (9*ISI + 500).toLong())
            }
            TYPE_A800_T -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 6*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (6*ISI + 800).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 7*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (7*ISI + 800).toLong())

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 8*ISI.toLong())
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (8*ISI + 800).toLong())

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (9*ISI + 800).toLong())
            }
        }
    }
}

/*
This App perform an Audio-Tactile Binding (ATB) test:

It has one single experimental condition composed by 36 trials (with fixed scheme!).
Each trial consists in a pair of stimulation modalities (audio and tactle) each composed by two consecutive trains of respectively 5 and 3 either audio and/or tactile stimuli (stim duration 1sec, isi=1sec). ITI=2sec.

single trial:
                1st train               2nd train
        __    __    __    __    __  |  __    __    __
A    __|  |__|  |__|  |__|  |__|  |_|_|  |__|  |__|  |____
                                    |
        __    __    __    __    __  |  __    __    __
T    __|  |__|  |__|  |__|  |__|  |_|_|  |__|  |__|  |____
                                    |
                                    |

in the second train, one of the two modalities can be in synch with other, delayed/anticipated by 200-500-800 ms or absent
in total, there are 9 types of stimuli

CODE    #REP    TYPE
0       6       A,T
1       3       A+200,T
2       3       A,T+200
3       6       A
4       3       A+500,T
5       3       A,T+500
6       6       T
7       3       A+800,T
8       3       A,T+800

The presentation order is fixed, 3 repetitions of the following 12 trials:

codes order: 0,2,3,7,6,5,3,1,0,8,6,4

A,T
A,T+200
A
A+800,T
T
A,T+500
A
A+200,T
A,T
A,T+800
T
A+500,T

Exported Data: trial_id, type
 */
