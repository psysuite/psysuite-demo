package iit.uvip.audiotactilebindingapp.tests

import android.os.Parcel
import android.os.Parcelable

class TestData(var type:Int, var name:String, var subject_id:String="", var time:Int=-1, var session:Int=-1) : Parcelable{

    private constructor(parcel: Parcel) : this(
        type        = parcel.readInt(),
        name        = parcel.readString()!!,
        subject_id  = parcel.readString()!!,
        time        = parcel.readInt(),
        session     = parcel.readInt()
    )

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<TestData> {
            override fun createFromParcel(parcel: Parcel) =
                TestData(parcel)
            override fun newArray(size: Int) = arrayOfNulls<TestData>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        dest.writeString(name)
        dest.writeString(subject_id)
        dest.writeInt(time)
        dest.writeInt(session)
    }

    override fun equals(other: Any?): Boolean {
        if (other is TestData) {
            return name.equals(other.name, ignoreCase = true)
        }
        return false
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}












