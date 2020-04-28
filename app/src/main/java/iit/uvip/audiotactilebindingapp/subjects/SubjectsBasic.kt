package iit.uvip.audiotactilebindingapp.subjects

import android.content.Context
import com.squareup.moshi.Moshi
import iit.uvip.audiotactilebindingapp.MainApplication
import iit.uvip.audiotactilebindingapp.utility.existFile
import iit.uvip.audiotactilebindingapp.utility.readText
import iit.uvip.audiotactilebindingapp.utility.saveText


class SubjectsBasic(val context: Context) {

    companion object {

        @JvmStatic val CURR_SUBJ_FILE:String = "curr_subject"
    }

    private var subject: SubjectBasicData? = null

    // =============================================================================================================
    // WRITE
    // =============================================================================================================

    fun writeJson(filename:String= CURR_SUBJ_FILE, subj: SubjectBasicData?=null){

        val moshi       = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter<SubjectBasicData>(SubjectBasicData::class.java)

        return try {
            val json_subject = jsonAdapter.toJson(subj ?: subject)
            saveText(context, filename + MainApplication.FILE_EXTENSION, json_subject)        // var jsontext = context!!.resources.openRawResource(R.raw.script_001).bufferedReader().use { it.readText() }

        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return
        }
    }

    // =============================================================================================================
    // LOAD
    // =============================================================================================================

    fun loadSubject(): SubjectBasicData?{
        val subj = existFile(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
        if(subj.first){
            val jsontext = readText(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
            return try {
                loadJsonText(jsontext)
            }
            catch (e:Exception){
                null
            }
        }
        return null
    }

    fun loadJsonText(jsontext:String): SubjectBasicData {

        val moshi           = Moshi.Builder().build()
        val jsonAdapter     = moshi.adapter<SubjectBasicData>(SubjectBasicData::class.java)
        return jsonAdapter.fromJson(jsontext)!!
    }
}