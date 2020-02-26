package iit.uvip.twoafctemporalquestapp.subjects

import android.content.Context
import com.squareup.moshi.Moshi
import iit.uvip.twoafctemporalquestapp.MainApplication
import iit.uvip.twoafctemporalquestapp.utility.existFile
import iit.uvip.twoafctemporalquestapp.utility.readText
import iit.uvip.twoafctemporalquestapp.utility.saveText


class Subjects(val context: Context) {

    companion object {

        @JvmStatic val CURR_SUBJ_FILE:String = "curr_subject"
    }

    private var subject: SubjectData? = null

    // =============================================================================================================
    // WRITE
    // =============================================================================================================

    fun writeJson(filename:String= CURR_SUBJ_FILE, subj: SubjectData?=null){

        val moshi       = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter<SubjectData>(
            SubjectData::class.java)

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

    fun loadSubject(): SubjectData?{
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

    fun loadJsonText(jsontext:String): SubjectData {

        val moshi           = Moshi.Builder().build()
        val jsonAdapter     = moshi.adapter<SubjectData>(
            SubjectData::class.java)

        return jsonAdapter.fromJson(jsontext)!!
    }
}