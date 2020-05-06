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

// base class for all longitudinal tests
open class SubjectLongitParcel(label:String="", age:Int=-1, gender:Int=-1, var session:Int=1) : SubjectBasicParcel(label, age, gender){

    private constructor(parcel: Parcel) : this(
        label           = parcel.readString()!!,
        age             = parcel.readInt(),
        gender          = parcel.readInt(),
        session         = parcel.readInt()
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SubjectLongitParcel> {
            override fun createFromParcel(parcel: Parcel)   = SubjectLongitParcel(parcel)
            override fun newArray(size: Int)                = arrayOfNulls<SubjectLongitParcel>(size)
        }

        private fun loadJsonText(jsontext:String): SubjectLongitParcel {
            val moshi           = Moshi.Builder().build()
            val jsonAdapter     = moshi.adapter(SubjectLongitParcel::class.java)
            return jsonAdapter.fromJson(jsontext)!!
        }

        fun loadSubject(): SubjectLongitParcel{
            val subj = existFile(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
            if(subj.first){
                val jsontext = readText(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
                return try {
                    loadJsonText(jsontext)
                }
                catch (e:Exception){
                    SubjectLongitParcel()
                }
            }
            return SubjectLongitParcel()
        }

    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(session)
    }

    // =============================================================================================================
    // WRITE
    // =============================================================================================================
    override fun writeJson(context: Context, filename:String){

        val moshi       = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(SubjectLongitParcel::class.java)

        return try {
            val json_subject = jsonAdapter.toJson(this)
            saveText(context, filename + MainApplication.FILE_EXTENSION, json_subject)        // var jsontext = context!!.resources.openRawResource(R.raw.script_001).bufferedReader().use { it.readText() }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return
        }
    }
}












