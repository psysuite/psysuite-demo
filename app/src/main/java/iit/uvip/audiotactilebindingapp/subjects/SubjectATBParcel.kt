package iit.uvip.audiotactilebindingapp.subjects

import android.os.Parcel
import android.os.Parcelable

// session

class SubjectATBParcel(label:String, age:Int, gender:Int, var nextTrailModality:Int) : SubjectBasicParcel(label, age, gender){

    private constructor(parcel: Parcel) : this(
        label               = parcel.readString()!!,
        age                 = parcel.readInt(),
        gender              = parcel.readInt(),
        nextTrailModality   = parcel.readInt()
    )


    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<SubjectATBParcel> {
            override fun createFromParcel(parcel: Parcel) =
                SubjectATBParcel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SubjectATBParcel>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(nextTrailModality)
    }

}












