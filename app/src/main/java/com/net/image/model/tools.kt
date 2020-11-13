package com.net.image.model

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.security.MessageDigest
import java.util.*






/**
 * @param name 文件保存位置："/storage/emulated/0/myApp/img/$name/"
 * @param imgUrl :Bitmap
 * @param context 上下文
 */
fun saveImg(name: String, imgUrl: String, context: Context):String{
//    val save_path = readInitJson(context)["save_path"]
    val absolutePath  = Environment.getExternalStorageDirectory().absolutePath
    Log.d("imgUrl", imgUrl)
    val path = "$absolutePath/myApp/img/$name/"
    val fileName: String = File(imgUrl).name.replace(Regex("\\?.*"), "")
    try {

        if(!File(path).exists()){
            File(path).mkdirs()
            Log.d("download_img", "创建文件夹$path")
        }
        val file = File("$path$fileName")
        val bitmap = Glide.with(context)
            .asBitmap()
            .load(imgUrl)
            .submit().get()
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "$path$fileName"
}


/**
 * 移动文件
 * @param content 上下文
 */
fun moveJson(content: Context, name: String = "rule"){
    val path = content.getExternalFilesDir(null)?.path
    if(!File(path,"$name.json").exists()) {
//        File(path).mkdirs()
//        File(path, "$name.json").mkdir()
        ActivityCompat.requestPermissions(
            content as Activity,
            arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"), 1)

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = content.assets.open("$name.json")
            val buffer = ByteArray(1024)
            val file = File(path, "$name.json")
            if (!file.exists()){
                file.createNewFile()
            }
            outputStream = FileOutputStream(file)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
//                stream.write(buffer, 0, length)
                outputStream.write(buffer, 0, length)
            }
            Log.d("file", file.path)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }
}


fun saveJson(context: Context, string: String, name: String = "rule"){
    var outputStream:OutputStream? = null
    try {
        val path = context.getExternalFilesDir(null)?.path
        var bytes = string.toByteArray()
        outputStream = FileOutputStream(File(path,"${name}.json"))
        outputStream.write(bytes)
    } catch (e: IOException) {
        e.printStackTrace()
    }finally {
        outputStream?.close()
    }
}

fun readInitJson(context: Context):JSONObject{

    val path= context.getExternalFilesDir(null)?.path

    val newStringBuilder = StringBuilder()
    var inputStream: InputStream? = null
    var isr:InputStreamReader? = null
    var reader:BufferedReader? = null
    try {
        inputStream = FileInputStream(File(path, "init.json"))
//        inputStream = content.assets.open("init.json")
        isr = InputStreamReader(inputStream)
        val reader = BufferedReader(isr)
        var jsonLine: String?
        while (reader.readLine().also { jsonLine = it } != null) {
            newStringBuilder.append(jsonLine)
        }
    }catch (e: IOException) {
        e.printStackTrace()
    }finally {
        inputStream?.close()
        reader?.close()
        isr?.close()
    }
    val result = newStringBuilder.toString()
    return JSONObject(result)

}

// 读取规则
fun readJson(context: Context, name: String = "rule"):List<Rule>{
//    val path = "/storage/emulated/0/myApp/${name}.json"
    val path = context.getExternalFilesDir(null)?.path
    val newStringBuilder = StringBuilder()
    var inputStream: InputStream? = null
    var isr:InputStreamReader? = null
    var reader:BufferedReader? = null
    try {
        inputStream = FileInputStream(File(path, "$name.json"))
//        inputStream = content.assets.open("init.json")
        isr = InputStreamReader(inputStream)
        val reader = BufferedReader(isr)
        var jsonLine: String?
        while (reader.readLine().also { jsonLine = it } != null) {
            newStringBuilder.append(jsonLine)
        }
    }catch (e: IOException) {
        e.printStackTrace()
    }finally {
        inputStream?.close()
        reader?.close()
        isr?.close()
    }

    val result = newStringBuilder.toString()
    Log.d("result", result)
    val jsonArray = JSONArray(result)
    Log.d("json", result)
    val typeOf = object : TypeToken<List<Rule>>() {}.type
//    val gson = GsonBuilder().setLenient().create()
    return Gson().fromJson(jsonArray.toString(), typeOf)
}

/**
 * 处理微信分享资源获取失败问题
 */
private fun insertImageToSystem(context: Context, imagePath: String, name: String): String? {
    var url: String? = ""
    try {
        url = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            imagePath,
            name,
            "有图了"
        )
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    return url
}

/**
 * 分享图片
 */
fun shareImg(content: Context, imgPath: String, name: String){

    val url = insertImageToSystem(content, imgPath, name)
    val imgUri = Uri.parse(url)
    var shareIntent = Intent()
    shareIntent.addFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent
            .FLAG_GRANT_WRITE_URI_PERMISSION
    )

//    uri = Uri.fromFile(new File(writeFile.getPath()));



    shareIntent.action = Intent.ACTION_SEND
    //其中imgUri为图片的标识符
//    shareIntent.setPackage("com.tencent.mm");
//    val cop = ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
//    shareIntent.setPackage("com.tencent.mobileqq")
//    shareIntent.component = cop
    shareIntent.putExtra(Intent.EXTRA_STREAM, imgUri)

    shareIntent.type = "image/*"
    //切记需要使用Intent.createChooser，否则会出现别样的应用选择框，您可以试试
    shareIntent = Intent.createChooser(shareIntent, "分享图片")
     content.startActivity(shareIntent)
}

/**
 * 复制到剪贴板
 * @param context
 * @param text
 */
fun putTextIntoClip(context: Context, text: String?) {
    val clipboardManager = context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
    //创建ClipData对象
    val clipData = ClipData.newPlainText("HSFAppDemoClip", text)
    //添加ClipData对象到剪切板中
    clipboardManager.setPrimaryClip(clipData)
}

fun getTextFromClip(context: Context):String {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

//    if (!clipboardManager.hasPrimaryClip()) return
    val clipData = clipboardManager.primaryClip
    //获取 ClipDescription
//    val clipDescription = clipboardManager.primaryClipDescription
//    //获取 lable
//    val lable = clipDescription!!.label.toString()
    //获取 text
    return clipData!!.getItemAt(0).text.toString()
}