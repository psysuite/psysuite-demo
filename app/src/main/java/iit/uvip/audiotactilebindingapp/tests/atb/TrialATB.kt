package iit.uvip.audiotactilebindingapp.tests.atb

import iit.uvip.audiotactilebindingapp.tests.common.TrialBasic


//                     trial_id    0-8      "none"
class TrialATB(id:Int=-1, type:Int): TrialBasic(id, type, ""){

    companion object {
        @JvmStatic val LOG_HEADER           = "id\ttype\n"
    }

    init {}

    // all class exported as string
    override fun toString():String{
        return id.toString() + "\t" + type.toString() + "\n"
    }

    // data exported to log file
    override fun Log():String{
        return id.toString() +  "\t" + type.toString() + "\n"
    }
}
