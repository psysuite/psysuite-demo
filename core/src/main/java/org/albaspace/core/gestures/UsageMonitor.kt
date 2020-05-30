package org.albaspace.core.gestures

import android.util.Log
import java.util.*

class UsageMonitor {

    private var mGestures:MutableList<String> = mutableListOf()
    private var nGestures:Int = 0

    private lateinit var startTime:Date
    private var elapsedTime:Long = 0

    private var executions:MutableList<Pair<Long,Boolean>> = mutableListOf()
    private var isMonitoring:Boolean = false

    private var onGoingExecutionStartTime:Long = 0

    fun startTrial(){

        isMonitoring    = true
        mGestures       = mutableListOf()
        nGestures       = 0

        startTime       = Date()
        elapsedTime     = 0
    }

    // when user start its code
    fun scriptStarted(){

        if(!isMonitoring) return
        onGoingExecutionStartTime = Date().time
    }

    // when code is ended and monitor communicate its result
    fun setResult(res:Boolean){

        if(!isMonitoring) return
        executions.add(Pair(onGoingExecutionStartTime, res))
    }

    // store results
    fun endTrial():String{

        if(!isMonitoring) return ""

        // TODO: store results somewhere
        isMonitoring    = false
        elapsedTime     = (Date().time - startTime.time)

        // data exported to log file
        var res = nGestures.toString() +  "\t" + elapsedTime.toString() + "\n"
        mGestures.map{
            res = res + it + "\n"
        }
        return res
    }

    fun addGesture(gesture:String){

        if(!isMonitoring) return

        mGestures.add(gesture)
        nGestures = mGestures.size
        Log.d("USAGE_MONITOR", "gesture detected: $gesture")

    }
}