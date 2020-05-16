package iit.uvip.audiotactilebindingapp.tests.atb

import android.content.Context
import android.media.MediaPlayer
import iit.uvip.audiotactilebindingapp.R
import iit.uvip.audiotactilebindingapp.tests.common.TaskCode
import iit.uvip.audiotactilebindingapp.tests.common.TestBasic
import iit.uvip.audiotactilebindingapp.utility.VibrationManager

class TestATB(ctx: Context, override val data: SubjectATBParcel) : TestBasic(ctx, data)
{
    var LOG_TAG:String = TestATB::class.java.simpleName

    private var noise: MediaPlayer? = null
    private var tone1sec: MediaPlayer
    private var tone2sec: MediaPlayer
    private lateinit var currTone: MediaPlayer

    private var curISI: Long = 0L
    private var curStimDuration: Long = 0L

    private val STIM_TYPE_TIME_A_T = 0
    private val STIM_TYPE_TIME_A200_T = 1
    private val STIM_TYPE_TIME_A_T200 = 2
    private val STIM_TYPE_TIME_A = 3
    private val STIM_TYPE_TIME_A500_T = 4
    private val STIM_TYPE_TIME_A_T500 = 5
    private val STIM_TYPE_TIME_T = 6
    private val STIM_TYPE_TIME_A800_T = 7
    private val STIM_TYPE_TIME_A_T800 = 8

    private val STIM_TYPE_FREQ_A_T = 10
    private val STIM_TYPE_FREQ_A = 13
    private val STIM_TYPE_FREQ_T = 16
    private val STIM_TYPE_FREQ_A2_T = 17
    private val STIM_TYPE_FREQ_A_T2 = 18

    // old lags
    private val TYPE_AUDIO          = 0
    private val TYPE_TACTILE        = 1
    private val TYPE_AUDIOTACTILE   = 2

    private val STIM_DURATION_INF = 2000L
    private val ISI_INF = 4000L

    private val STIM_DURATION = 1000L
    private val ISI = 2000L

    private val EVENT_SECOND_TRAIN  = 1201

    private var STIM_DUR_AT: LongArray = longArrayOf()
    private var STIM_DUR_A200_T: LongArray = longArrayOf()
    private var STIM_DUR_A_T200: LongArray = longArrayOf()
    private var STIM_DUR_A: LongArray = longArrayOf()
    private var STIM_DUR_A500_T: LongArray = longArrayOf()
    private var STIM_DUR_A_T500: LongArray = longArrayOf()
    private var STIM_DUR_T: LongArray = longArrayOf()
    private var STIM_DUR_A800_T: LongArray = longArrayOf()
    private var STIM_DUR_A_T800: LongArray = longArrayOf()

    private val amplitude = 100
    private var vibration_trains_timings: MutableList<LongArray> = mutableListOf(
        STIM_DUR_AT,
        STIM_DUR_A200_T,
        STIM_DUR_A_T200,
        STIM_DUR_A,
        STIM_DUR_A500_T,
        STIM_DUR_A_T500,
        STIM_DUR_T,
        STIM_DUR_A800_T,
        STIM_DUR_A_T800
    )
    private var vibrator: VibrationManager? = null

    private var vibration_trains_amplitudes: MutableList<IntArray> = mutableListOf(
        intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
        intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
        intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
        intArrayOf(amplitude, 0, amplitude),
        intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
        intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
        intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
        intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude),
        intArrayOf(amplitude, 0, amplitude, 0, amplitude, 0, amplitude)
    )

    companion object {

        @JvmStatic
        val TEST_BASIC_LABEL = "ATB"
        @JvmStatic
        val NUM_REPETITIONS = 3

        fun getConditionsInfo(ctx: Context): List<TaskCode> {
            return mutableListOf(
                TaskCode(
                    TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.time),
                    TEST_ATB_TIME
                ),
                TaskCode(
                    TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.frequency),
                    TEST_ATB_FREQUENCY
                ),
                TaskCode(
                    TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.time_infants),
                    TEST_ATB_TIME_INF
                ),
                TaskCode(
                    TEST_BASIC_LABEL + "_" + ctx.resources.getString(R.string.frequency_infants),
                    TEST_ATB_FREQUENCY_INF
                )
            )
        }
    }
    // =============================================================================================================================

    init{

        vibrator = application.vibrator
        nextTrailModality   = data.nextTrailModality
        abortMode = TEST_ABORT_TRIALEND       // abort @ trial end
        showTrialsID = TEST_SHOWTRIALS_ALWAYS    // trial id always shown

        if (data.whitenoise) noise = MediaPlayer.create(
            ctx,
            ctx.resources.getIdentifier("wnoise_20s", "raw", ctx.packageName)
        )

        tone1sec = MediaPlayer.create(
            ctx,
            ctx.resources.getIdentifier("tone200hz_1sec", "raw", ctx.packageName)
        )
        tone2sec = MediaPlayer.create(
            ctx,
            ctx.resources.getIdentifier("tone200hz_2sec", "raw", ctx.packageName)
        )

        when (data.type) {
            TEST_ATB_FREQUENCY_INF,
            TEST_ATB_TIME_INF -> {
                curISI = ISI_INF
                curStimDuration = STIM_DURATION_INF
            }
            TEST_ATB_FREQUENCY,
            TEST_ATB_TIME -> {
                curISI = ISI
                curStimDuration = STIM_DURATION
            }
        }
        when (data.type) {
            TEST_ATB_TIME,
            TEST_ATB_TIME_INF -> initTimeArrays()

            TEST_ATB_FREQUENCY,
            TEST_ATB_FREQUENCY_INF -> initFreqArrays()
        }
        initTest()
    }

    private fun initTimeArrays() {
        STIM_DUR_AT = longArrayOf(
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration
        )
        STIM_DUR_A200_T = longArrayOf(
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration
        )
        STIM_DUR_A_T200 = longArrayOf(
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration + 200L,
            curStimDuration,
            curStimDuration + 200L
        )
        STIM_DUR_A = longArrayOf(curStimDuration, curStimDuration, curStimDuration)
        STIM_DUR_A500_T = longArrayOf(
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration
        )
        STIM_DUR_A_T500 = longArrayOf(
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration + 500L,
            curStimDuration,
            curStimDuration + 500L
        )
        STIM_DUR_T = longArrayOf(
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration
        )
        STIM_DUR_A800_T = longArrayOf(
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration
        )
        STIM_DUR_A_T800 = longArrayOf(
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration,
            curStimDuration + 800L,
            curStimDuration,
            curStimDuration + 800L
        )

        vibration_trains_timings = mutableListOf(
            STIM_DUR_AT,
            STIM_DUR_A200_T,
            STIM_DUR_A_T200,
            STIM_DUR_A,
            STIM_DUR_A500_T,
            STIM_DUR_A_T500,
            STIM_DUR_T,
            STIM_DUR_A800_T,
            STIM_DUR_A_T800
        )
    }

    private fun initFreqArrays() {

    }

    override fun initTest(){
        // mTrials list
        createTrials()
        currTone = tone2sec

        nTrials     = mTrials.size
        currTrial   = 0

        createResultFile(data.label, TrialATB.LOG_HEADER)
    }

    override fun show(trialid:Int, isRepeat:Boolean){
        mTrial = mTrials[trialid]

        if(isRepeat)    mTrial.repetitions++

        noise?.setVolume(0.5f, 0.5f)
        noise?.start()

        firstTrain(mTrial.type)     // schedule first 2 stimuli
        secondTrain(mTrial.type)    // schedule second 2 stimuli
    }

    override fun nextTrial(prev_result: Int, elapsed: Int): Int {
        testEvent.accept(EVENT_UPDATE_TRIAL_ID)
        return super.nextTrial(prev_result, elapsed)
    }

    // called by secondTrain
    override fun onTrialEnd(){

        noise?.stop()
        noise?.prepare()

        if(nextTrailModality == TEST_NEXTTRIAL_BUTTON)
            testEvent.accept(EVENT_SHOW_NEXT_BUTTON)
        else{
            // create a ITI=2sec pause by waiting for 1sec and invoking a 1sec wait in TestFragment
            mStimuliHandler.postDelayed({
                testEvent.accept(EVENT_SHOW_1SECABORT)
            }, curISI)
        }
    }

    private fun firstTrain(type: Int) {

        vibrator?.vibratePattern(vibration_trains_timings[type], vibration_trains_amplitudes[type])
        deliverStimulus(TYPE_AUDIO)
        testEvent.accept(EVENT_STIMULI_START)

        mStimuliHandler.postDelayed({
            deliverStimulus(TYPE_AUDIO)
        }, curISI)
    }

    private fun deliverStimulus(type:Int){

        when(type) {
            TYPE_AUDIO -> {
                currTone.start()
                mStimuliHandler.postDelayed({
                    currTone.stop()
                    currTone.prepare()
                }, curStimDuration)
            }
            TYPE_TACTILE -> vibrator?.vibrateSingle(curStimDuration)
            TYPE_AUDIOTACTILE     -> {
                currTone.start()
                mStimuliHandler.postDelayed({
                    currTone.stop()
                    currTone.prepare()
                }, curStimDuration)
                vibrator?.vibrateSingle(curStimDuration)
            }
        }
    }

    // class Trial(var id:Int=-1, val type:Int, val label:String, var audio_id:Int, var correct_answer:Int=-1, var user_answer:Int=-1,
    //                 var success:Boolean=false, var elapsed:Int=-1, var repetitions:Int=1)
    private fun createTrials()
    {
        var cnt     = -1
        for(i in 0 until NUM_REPETITIONS) {
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A800_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A_T800))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_T))
        }
    }

    private fun secondTrain(type:Int){

        when(type){
            STIM_TYPE_TIME_A_T -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 2 * curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 4 * curISI)
            }
            STIM_TYPE_TIME_A -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 2 * curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                }, 3 * curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 4 * curISI)
            }
            STIM_TYPE_TIME_T -> {
                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 2 * curISI)
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                }, 3*curISI)
                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, 4 * curISI)
            }
            STIM_TYPE_TIME_A_T200 -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 2 * curISI)
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, (2*curISI + 200L))

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 3 * curISI)
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, (3*curISI + 200L))

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (4 * curISI + 200L))
            }
            STIM_TYPE_TIME_A_T500 -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 2 * curISI)
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, (2*curISI + 500L))

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 3 * curISI)
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, (3*curISI + 500L))

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (4 * curISI + 500))
            }
            STIM_TYPE_TIME_A_T800 -> {
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 2 * curISI)
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, (2*curISI + 800L))

                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, 3 * curISI)
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, (3*curISI + 800L))

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (4 * curISI + 800))
            }
            STIM_TYPE_TIME_A200_T -> {
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, 2*curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (2 * curISI + 200))

//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, 3*curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (3 * curISI + 200L))

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (4 * curISI + 200L))

            }
            STIM_TYPE_TIME_A500_T -> {
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, 2*curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (2 * curISI + 500L))

//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, 3*curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (3 * curISI + 500L))

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (4 * curISI + 500L))
            }
            STIM_TYPE_TIME_A800_T -> {
//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, 2*curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (2 * curISI + 800L))

//                mStimuliHandler.postDelayed({
//                    deliverStimulus(TYPE_TACTILE)
//                    testEvent.accept(EVENT_SECOND_TRAIN)
//                }, 3*curISI)
                mStimuliHandler.postDelayed({
                    deliverStimulus(TYPE_AUDIO)
                    testEvent.accept(EVENT_SECOND_TRAIN)
                }, (3 * curISI + 800))

                mStimuliHandler.postDelayed({
                    onTrialEnd()
                }, (4 * curISI + 800L))
            }
        }
    }

    // class Trial(var id:Int=-1, val type:Int, val label:String, var audio_id:Int, var correct_answer:Int=-1, var user_answer:Int=-1,
    //                 var success:Boolean=false, var elapsed:Int=-1, var repetitions:Int=1)
    private fun createTrials_old() {
        var cnt = -1
        for (i in 0 until NUM_REPETITIONS) {
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A_T200))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A800_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A_T500))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A200_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A_T800))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_T))
            mTrials.add(TrialATB(++cnt, STIM_TYPE_TIME_A500_T))
        }
    }

}

/*
This App perform an Audio-Tactile Binding (ATB) test:

It has one single experimental condition composed by 36 trials (with fixed scheme!).
Each trial consists in a pair of stimulation modalities (audio and tactle) each composed by two consecutive trains of respectively 2 and 2 either audio and/or tactile stimuli (stim duration 2sec, isi=2sec). ITI=2sec.

single trial:
       1st train    2nd train
        __    __  |  __    __
A    __|  |__|  |_|_|  |__|  |__
                  |
        __    __  |  __    __
T    __|  |__|  |_|_|  |__|  |__
                  |
                  |

in the second train, one of the two modalities can be in synch with other, delayed/anticipated by 800 ms or absent
in total, there are 5 types of stimuli

CODE    #REP    TYPE
0       6       A,T
3       6       A
6       6       T
7       3       A+800,T
8       3       A,T+800

The presentation order is fixed, 3 repetitions of the following 12 trials:

codes order: 0,3,7,6,3,0,8,6

A,T
A
A+800,T
T
A
A,T
A,T+800
T

Exported Data: trial_id, type



OLD TASK

single trial:
                1st train               2nd train
        __    __    __    __    __  |  __    __    __
A    __|  |__|  |__|  |__|  |__|  |_|_|  |__|  |__|  |____
                                    |
        __    __    __    __    __  |  __    __    __
T    __|  |__|  |__|  |__|  |__|  |_|_|  |__|  |__|  |____
                                    |
                                    |

OLD SCHEMA
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
 */
