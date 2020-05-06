package iit.uvip.audiotactilebindingapp.tests.tid

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iit.uvip.audiotactilebindingapp.MainApplication
import iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel.SubjectLongitParcel
import iit.uvip.audiotactilebindingapp.utility.existFile
import iit.uvip.audiotactilebindingapp.utility.readText
import iit.uvip.audiotactilebindingapp.utility.saveText

// session

class SubjectTIDParcel(label:String="", age:Int=-1, gender:Int=-1, session:Int=-1, var modality:Int=-1, var interval_type:Int=-1, var first_modality:Int=-1) : SubjectLongitParcel(label, age, gender, session){

    private constructor(parcel: Parcel) : this(
        label           = parcel.readString()!!,
        age             = parcel.readInt(),
        gender          = parcel.readInt(),
        session         = parcel.readInt(),
        modality        = parcel.readInt(),
        interval_type   = parcel.readInt(),
        first_modality  = parcel.readInt()
    )


    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<SubjectTIDParcel> {
            override fun createFromParcel(parcel: Parcel) =
                SubjectTIDParcel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SubjectTIDParcel>(size)
        }

        private fun loadJsonText(jsontext:String): SubjectTIDParcel {
            val moshi           = Moshi.Builder().build()
            val jsonAdapter     = moshi.adapter(SubjectTIDParcel::class.java)
            return jsonAdapter.fromJson(jsontext)!!
        }

        fun loadSubject(): SubjectTIDParcel {
            val subj = existFile(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
            if(subj.first){
                val jsontext = readText(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
                return try {
                    loadJsonText(
                        jsontext
                    )
                }
                catch (e:Exception){
                    SubjectTIDParcel()
                }
            }
            return SubjectTIDParcel()
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(modality)
        dest.writeInt(interval_type)
        dest.writeInt(first_modality)
    }

    // =============================================================================================================
    // WRITE
    // =============================================================================================================

    override fun writeJson(context: Context, filename:String){

        val moshi       = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(SubjectTIDParcel::class.java)

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

    // =============================================================================================================
    // LOAD
    // =============================================================================================================


    // =============================================================================================================
}












