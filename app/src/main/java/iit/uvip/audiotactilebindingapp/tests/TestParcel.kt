package iit.uvip.audiotactilebindingapp.tests

import android.os.Parcel
import android.os.Parcelable
//                                                                                                             if==1: press next to run next trials
class TestParcel(var type:Int, var name:String, var subject_id:String="", var time:Int=-1, var session:Int=-1, var nextTrailModality:Int=0) : Parcelable{

    private constructor(parcel: Parcel) : this(
        type                = parcel.readInt(),
        name                = parcel.readString()!!,
        subject_id          = parcel.readString()!!,
        time                = parcel.readInt(),
        session             = parcel.readInt(),
        nextTrailModality   = parcel.readInt()
    )

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<TestParcel> {
            override fun createFromParcel(parcel: Parcel)   = TestParcel(parcel)
            override fun newArray(size: Int)                = arrayOfNulls<TestParcel>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        dest.writeString(name)
        dest.writeString(subject_id)
        dest.writeInt(time)
        dest.writeInt(session)
        dest.writeInt(nextTrailModality)
    }

    override fun equals(other: Any?): Boolean {
        if (other is TestParcel) {
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












