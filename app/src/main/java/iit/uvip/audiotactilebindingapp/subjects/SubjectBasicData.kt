package iit.uvip.audiotactilebindingapp.subjects

import android.os.Parcel
import android.os.Parcelable
import java.lang.NumberFormatException

open class SubjectBasicData(var label:String, var age:Int, var gender:Int) : Parcelable{

    private constructor(parcel: Parcel) : this(
        label           = parcel.readString()!!,
        age             = parcel.readInt(),
        gender          = parcel.readInt()
    )


    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<SubjectBasicData> {
            override fun createFromParcel(parcel: Parcel) =
                SubjectBasicData(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SubjectBasicData>(size)
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
    }

    override fun equals(other: Any?): Boolean {
        if (other is SubjectBasicData) {
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

}












