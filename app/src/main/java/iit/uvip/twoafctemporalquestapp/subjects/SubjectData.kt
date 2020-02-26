package iit.uvip.twoafctemporalquestapp.subjects

import android.os.Parcel
import android.os.Parcelable
import iit.uvip.twoafctemporalquestapp.tests.Test
import java.lang.NumberFormatException

class SubjectData(var label:String, var age:Int, var gender:Int, var modality:Int, var interval_type:Int, var first_modality:Int) : Parcelable{

    private constructor(parcel: Parcel) : this(
        label           = parcel.readString()!!,
        age             = parcel.readInt(),
        gender          = parcel.readInt(),
        modality        = parcel.readInt(),
        interval_type   = parcel.readInt(),
        first_modality  = parcel.readInt()
    )


    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<SubjectData> {
            override fun createFromParcel(parcel: Parcel) =
                SubjectData(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SubjectData>(size)
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
        dest.writeInt(modality)
        dest.writeInt(interval_type)
        dest.writeInt(first_modality)
    }

    override fun equals(other: Any?): Boolean {
        if (other is SubjectData) {
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

    fun getType():Int{
        return if(interval_type == 0) {
                    if(modality == 0)  Test.TEST_TID_SHORT_AUDIO
                    else               Test.TEST_TID_SHORT_TACTILE
                }
                else {
                    if(modality == 0)  Test.TEST_TID_LONG_AUDIO
                    else               Test.TEST_TID_LONG_TACTILE
                }
    }
}












