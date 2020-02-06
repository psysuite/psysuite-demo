package iit.uvip.twoafctemporalquestapp

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.view.Gravity
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.*

fun showToast(text:String, ctx:Context, duration:Int=Toast.LENGTH_SHORT, gravity:Int=Gravity.CENTER) {
    val t = Toast.makeText(ctx, text, duration)
    t.setGravity(gravity, 0, 0)
    t.show()
}

// by default I do not notify DM, I notify DM when explicitely requested or in case file do not exist)
fun saveData(ctx:Context, filename: String, text: String, notifyDm:Boolean=false){

    if (!isExternalStorageWritable()){
        showToast("Cannot write on External Storage", ctx)
        return
    }
    try {
        val path    = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file    = File(path, filename)
        val bytes   = text.toByteArray(charset("UTF-8"))
        val stream  = FileOutputStream(file, true)
        stream.write(bytes)
        stream.close()

        if (notifyDm) {
            val down = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            down.addCompletedDownload(file.name, "User file", false, "text/plain", file.path, file.length(), true)
        }
    }
    catch (exc: Exception)
    {
        showToast("Could not save data to file!", ctx)
    }
}


fun deleteFile(filename:String){
    val path    = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file    = File(path, filename)
    if (file.exists())
        file.delete()
}

fun isExternalStorageWritable(): Boolean {
    val state = Environment.getExternalStorageState()
    return (Environment.MEDIA_MOUNTED == state)
}

/* Checks if external storage is available to at least read */
fun isExternalStorageReadable(): Boolean {
    val state = Environment.getExternalStorageState()
    return (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state)
}

fun getTimeDifference(startdate:Date):Int{

    val now:Long = Date().time

    return (now - startdate.time).toInt()

}

