package iit.uvip.audiotactilebindingapp.utility

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import java.io.*
import java.util.*

fun showToast(text:String, ctx:Context, duration:Int=Toast.LENGTH_SHORT, gravity:Int=Gravity.CENTER) {
    val t = Toast.makeText(ctx, text, duration)
    t.setGravity(gravity, 0, 0)
    t.show()
}

// by default I do not notify DM, I notify DM when explicitely requested or in case file do not exist)
fun saveText(ctx:Context, filename: String, text: String, dir:String=Environment.DIRECTORY_DOWNLOADS, overwrite:Boolean=true, notifyDm:Boolean=false){

    if (!isExternalStorageWritable()){
        showToast("Cannot write on External Storage", ctx)
        return
    }
    try {
        val path    = Environment.getExternalStoragePublicDirectory(dir)
        val file    = File(path, filename)

        val exist   = file.exists()

        if(exist) {
            if (overwrite)  deleteFile(filename, dir)
        }

        val bytes   = text.toByteArray(charset("UTF-8"))
        val stream  = FileOutputStream(file, true)
        stream.write(bytes)
        stream.close()

        if (notifyDm) {
            val down = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            down.addCompletedDownload(
                file.name,
                "User file",
                false,
                "text/plain",
                file.path,
                file.length(),
                true
            )
        }
    }
    catch (exc: Exception)
    {
        showToast("Could not save data to file!", ctx)
    }
}

fun readText(filename: String, dir:String=Environment.DIRECTORY_DOWNLOADS):String{

    val path                = Environment.getExternalStoragePublicDirectory(dir)
    val file                = File(path,filename)
    val fileInputStream     = FileInputStream(file)
    val inputStreamReader   = InputStreamReader(fileInputStream)
    val bufferedReader      = BufferedReader(inputStreamReader)
    val stringBuilder       = StringBuilder()
    var text:String?        = null

    while ({ text = bufferedReader.readLine(); text }() != null) {
        stringBuilder.append(text)
    }
    fileInputStream.close()
    return stringBuilder.toString()
}

fun getFileList(dir:String=Environment.DIRECTORY_DOWNLOADS, allowedext:List<String>):List<File>{

    val path: String    = Environment.getExternalStorageDirectory().absolutePath
    val spath           = "Download"
    val fullpath        = File(path + File.separator + spath)

//    val path:File           = Environment.getExternalStoragePublicDirectory(dir)
    val listAllFiles        = fullpath.listFiles()

    val fileList:MutableList<File> = mutableListOf()

    if (listAllFiles != null && listAllFiles.isNotEmpty()) {
        for (currentFile in listAllFiles) {
            for (ext in allowedext) {
                if (currentFile.name.endsWith(ext)) {
                    Log.e("downloadFilePath", currentFile.absolutePath) // File absolute path
                    Log.e("downloadFileName", currentFile.name)         // File Name
                    fileList.add(currentFile)
                }
            }
        }
    }
    return fileList
}

fun existFile(filename:String, dir:String=Environment.DIRECTORY_DOWNLOADS):Pair<Boolean, File?>{

    val path    = Environment.getExternalStoragePublicDirectory(dir)
    val file    = File(path, filename)

    return when(file.exists()) {
        true    -> Pair(true, file)
        false   -> Pair(false, null)
    }
}

fun deleteFile(filename:String, writedir:String=Environment.DIRECTORY_DOWNLOADS){
    val res     = existFile(filename, writedir)
    if (res.first)
        res.second!!.delete()
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


//fun String.isInt(){
//    if(this.isBlank())
//}