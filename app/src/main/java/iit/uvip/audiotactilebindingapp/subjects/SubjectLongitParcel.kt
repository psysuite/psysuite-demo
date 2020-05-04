package iit.uvip.audiotactilebindingapp.subjects

import android.os.Parcel
import android.os.Parcelable

// base class for all longitudinal tests
open class SubjectLongitParcel(label:String, age:Int, gender:Int, var session:Int=1) : SubjectBasicParcel(label, age, gender){

    private constructor(parcel: Parcel) : this(
        label           = parcel.readString()!!,
        age             = parcel.readInt(),
        gender          = parcel.readInt(),
        session         = parcel.readInt()
    )


    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<SubjectLongitParcel> {
            override fun createFromParcel(parcel: Parcel) =
                SubjectLongitParcel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SubjectLongitParcel>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(session)
    }
}












