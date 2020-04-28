package iit.uvip.audiotactilebindingapp.subjects

import android.os.Parcel
import android.os.Parcelable
import iit.uvip.audiotactilebindingapp.tests.Test

class SubjectTIDData(label:String, age:Int, gender:Int, var modality:Int, var interval_type:Int, var first_modality:Int) : SubjectBasicData(label, age, gender){

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
        val CREATOR = object : Parcelable.Creator<SubjectTIDData> {
            override fun createFromParcel(parcel: Parcel) =
                SubjectTIDData(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SubjectTIDData>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(modality)
        dest.writeInt(interval_type)
        dest.writeInt(first_modality)
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












