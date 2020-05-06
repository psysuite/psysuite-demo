package iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iit.uvip.audiotactilebindingapp.MainApplication
import iit.uvip.audiotactilebindingapp.utility.existFile
import iit.uvip.audiotactilebindingapp.utility.readText
import iit.uvip.audiotactilebindingapp.utility.saveText
import java.lang.NumberFormatException

/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
*/

// base class for all tests
// nextTrailModality = -1 => do not show switch button in the gui

open class SubjectBasicParcel(var label:String="", var age:Int=-1, var gender:Int=-1, var nextTrailModality:Int=-1) : Parcelable{

    private constructor(parcel: Parcel) : this(
        label               = parcel.readString()!!,
        age                 = parcel.readInt(),
        gender              = parcel.readInt(),
        nextTrailModality   = parcel.readInt()
    )

    companion object {

        @JvmStatic val CURR_SUBJ_FILE:String = "curr_subject"

        @JvmField  val CREATOR = object : Parcelable.Creator<SubjectBasicParcel> {
            override fun createFromParcel(parcel: Parcel)   = SubjectBasicParcel(parcel)
            override fun newArray(size: Int)                = arrayOfNulls<SubjectBasicParcel>(size)
        }

        private fun loadJsonText(jsontext:String): SubjectBasicParcel {

            val moshi           = Moshi.Builder().build()
            val jsonAdapter     = moshi.adapter(SubjectBasicParcel::class.java)
            return jsonAdapter.fromJson(jsontext)!!
        }

        fun loadSubject(): SubjectBasicParcel{
            val subj = existFile(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
            if(subj.first){
                val jsontext = readText(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
                return try {
                    loadJsonText(jsontext)
                }
                catch (e:Exception){
                    SubjectBasicParcel()
                }
            }
            return SubjectBasicParcel()
        }

        fun validate(lab:String, ag:String):String{
            var res = ""

            if(lab.isBlank())   res = "il nome è vuoto"

            try {
                ag.toInt()
            }
            catch(e:NumberFormatException){
                res = res + "\n" + "l'eta inserita non è valida"
            }
            return res
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(label)
        dest.writeInt(age)
        dest.writeInt(gender)
        dest.writeInt(nextTrailModality)
    }

    override fun equals(other: Any?): Boolean {
        if (other is SubjectBasicParcel) {
            return label.equals(other.label, ignoreCase = true)
        }
        return false
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

    // =============================================================================================================
    // WRITE
    // =============================================================================================================
    open fun writeJson(context:Context, filename:String = CURR_SUBJ_FILE){

        val moshi       = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(SubjectBasicParcel::class.java)

        return try {
            val json_subject = jsonAdapter.toJson(this)
            saveText(context, filename + MainApplication.FILE_EXTENSION, json_subject)        // var jsontext = context!!.resources.openRawResource(R.raw.script_001).bufferedReader().use { it.readText() }
        }
        catch (e: Exception){
            e.printStackTrace()
            return
        }
    }
    // =============================================================================================================
}