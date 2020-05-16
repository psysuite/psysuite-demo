package iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iit.uvip.audiotactilebindingapp.MainApplication
import iit.uvip.audiotactilebindingapp.tests.common.TaskCode
import iit.uvip.audiotactilebindingapp.utility.existFile
import iit.uvip.audiotactilebindingapp.utility.readText
import iit.uvip.audiotactilebindingapp.utility.saveText
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize

/*
This class manage simple subjects that participate in tests with only one condition.
in subclasses, user must resolve the condition code according to internal variables
*/

// base class for all tests
// nextTrailModality = -1 => do not show switch button in the gui

@Parcelize
open class SubjectBasicParcel(
    open var type: Int = -1,
    open var label: String = "",
    open var age: Int = -1,
    open var gender: Int = -1,
    open var nextTrailModality: Int = -1,
    open var taskcodes: List<TaskCode> = listOf()
) : Parcelable {

    private constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        listOf<TaskCode>().apply {
            if (Build.VERSION.SDK_INT >= 29) parcel.readParcelableList(
                this,
                TaskCode::class.java.classLoader
            )
            else parcel.readList(this, TaskCode::class.java.classLoader)
        }
    )

    companion object : Parceler<SubjectBasicParcel> {

        override fun SubjectBasicParcel.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(type)
            parcel.writeString(label)
            parcel.writeInt(age)
            parcel.writeInt(gender)
            parcel.writeInt(nextTrailModality)
            if (Build.VERSION.SDK_INT >= 29) parcel.writeParcelableList(taskcodes, flags)
            else parcel.writeList(taskcodes)
        }

        override fun create(parcel: Parcel) = SubjectBasicParcel(parcel)

        @JvmStatic val CURR_SUBJ_FILE:String = "curr_subject"

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

            if (lab.isBlank()) res = res + "\n" + "il nome è vuoto"

            try {
                ag.toInt()
            }
            catch(e:NumberFormatException){
                res = res + "\n" + "l'eta inserita non è valida"
            }
            return res
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is SubjectBasicParcel) {
            return label.equals(other.label, ignoreCase = true)
        }
        return false
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