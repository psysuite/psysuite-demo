package iit.uvip.twoafctemporalquestapp.tests

// trial is used for psychophysics:
// - trial_id
// - stimtype_code
// - stimtype_label


// - conflict_type
// - position
// - duration
// - duration2


abstract class Trial(var id:Int=-1, val type:Int, val label:String) {

    var correct_answer:Int  = -1
    var user_answer:Int     = -1
    var repetitions:Int     =  1
    var success:Boolean     =  false
    var elapsed:Int         = -1

    // data exported to log file
    abstract fun Log():String

    fun setResponse(result:Int, elapsedms:Int) {
        user_answer = result
        elapsed     = elapsedms
        success     = (result == correct_answer)
    }
}

class BisectionTrial(id:Int=-1, type:Int, label:String, val position:Int, val conflict_type:String, val duration:Int, val duration2:Int=0): Trial(id,type,label){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tlat\tconflict\tres\tcor_ans\tuser_ans\telapsed\trep\n"
        @JvmStatic val LAST_STIMULUS_DELAY  = 1000
    }

    init {
        correct_answer = when (position >= LAST_STIMULUS_DELAY / 2) {
            true -> 1
            false -> 0
        }
    }

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\t" + label + "\t" + conflict_type + "\t" + position.toString() + "\t" + duration.toString() + "\t" + success.toString() + "\t" + duration2.toString()+ "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + label + "\t" + position.toString() + "\t" + conflict_type + "\t" + success.toString() + "\t" + correct_answer.toString() + "\t" + user_answer.toString() + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\n"
    }
}

//                     trial_id    0/1      same/diff          1-18
class MusicMetersTrial(id:Int=-1, type:Int, label:String, var audio_id:Int): Trial(id,type,label){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\tlabel\tres\tcor_ans\tuser_ans\telapsed\trep\taudio_id\n"
    }

    init {
        correct_answer = when (label.contains("_same")){
            true -> 0
            false -> 1
        }
    }

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\t" + label + "\t" + success.toString() + "\t" + audio_id.toString()+ "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + label + "\t" + success.toString() + "\t" + correct_answer.toString() + "\t" + user_answer.toString() + "\t" + elapsed.toString() + "\t" + repetitions.toString() + "\t" + audio_id.toString() + "\n"
    }
}


