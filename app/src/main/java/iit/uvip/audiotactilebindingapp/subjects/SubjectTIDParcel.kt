package iit.uvip.audiotactilebindingapp.subjects

import android.os.Parcel
import android.os.Parcelable

// session

class SubjectTIDParcel(label:String, age:Int, gender:Int, session:Int, var modality:Int, var interval_type:Int, var first_modality:Int) : SubjectLongitParcel(label, age, gender, session){

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
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(modality)
        dest.writeInt(interval_type)
        dest.writeInt(first_modality)
    }

}












