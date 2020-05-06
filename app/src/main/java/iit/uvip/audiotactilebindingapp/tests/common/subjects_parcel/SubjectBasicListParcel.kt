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

/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
 */

// base class for all tests
open class SubjectBasicListParcel(label:String = "", age:Int = -1, gender:Int = -1, var spinner_sel:Int = -1, var spinner_label:String = "", var spinner_data_resource:Int = -1) : SubjectBasicParcel(label, age, gender){

    private constructor(parcel: Parcel) : this(
        label                   = parcel.readString()!!,
        age                     = parcel.readInt(),
        gender                  = parcel.readInt(),
        spinner_sel             = parcel.readInt(),
        spinner_label           = parcel.readString()!!,
        spinner_data_resource   = parcel.readInt()
    )

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<SubjectBasicListParcel> {
            override fun createFromParcel(parcel: Parcel)   = SubjectBasicListParcel(parcel)
            override fun newArray(size: Int)                = arrayOfNulls<SubjectBasicListParcel>(size)
        }


        private fun loadJsonText(jsontext:String): SubjectBasicListParcel {
            val moshi           = Moshi.Builder().build()
            val jsonAdapter     = moshi.adapter(SubjectBasicListParcel::class.java)
            return jsonAdapter.fromJson(jsontext)!!
        }

        fun loadSubject(): SubjectBasicListParcel{
            val subj = existFile(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
            if(subj.first){
                val jsontext = readText(CURR_SUBJ_FILE + MainApplication.FILE_EXTENSION)
                return try {
                    loadJsonText(jsontext)
                }
                catch (e:Exception){
                    SubjectBasicListParcel()
                }
            }
            return SubjectBasicListParcel()
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(spinner_sel)
        dest.writeString(spinner_label)
    }

    // =============================================================================================================
    // WRITE
    // =============================================================================================================
    override fun writeJson(context: Context, filename:String){

        val moshi       = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(SubjectBasicListParcel::class.java)

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












