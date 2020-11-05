@file:Suppress("DEPRECATION")

package com.net.image.model

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.security.MessageDigest
import java.util.*


fun getNextData(data: String, pageNum: Int = 0, type: String = ""):String{
    val jsonObject = JSONObject(data)
//    val next = jsonObject.keys().next()
    var jsonObject1 = JSONObject()
    for(next in jsonObject.keys()) {
        try {
            jsonObject1 = JSONObject(jsonObject.getString(next))
        }catch (e: Exception){
            Log.d("getNextData", e.toString())
        }
//        val jsonObject1 = JSONObject(jsonObject.getString(next))
        if (jsonObject1.length() != 0 ) {
            val ctime = jsonObject1.getString("ctime")
            val param = jsonObject1.getJSONObject("param")
            val time = Date().time
            jsonObject1.put("ctime", time)
            param.put("time_point", time / 1000)
            param.put("start", (pageNum - 1) * 20)
            if (type != "") {
                param.put("works_category", type)
            }
            val signCode = getSignCode(param.toString())
            jsonObject1.put("sign_code", signCode)
            jsonObject.put(next, jsonObject1.toString())
            Log.d(ctime, jsonObject.toString())
            return jsonObject.toString()
        }
    }
    return jsonObject.toString()
}


fun getSignCode(param: String):String{
    val text = "poco_${param}_app"
    val instance: MessageDigest = MessageDigest.getInstance("MD5")
    //对字符串加密，返回字节数组
    val digest:ByteArray = instance.digest(text.toByteArray())
    var sb : StringBuffer = StringBuffer()
    for (b in digest) {
        //获取低八位有效值
        var i :Int = b.toInt() and 0xff
        //将整数转化为16进制
        var hexString = Integer.toHexString(i)
        if (hexString.length < 2) {
            //如果是一位的话，补0
            hexString = "0" + hexString
        }
        sb.append(hexString)
    }
    return sb.substring(5, 24)
}


/**
 * @param name 文件保存位置："/storage/emulated/0/myApp/img/$name/"
 * @param imgUrl :Bitmap
 * @param context 上下文
 */
fun saveImg(name: String, imgUrl: String, context: Context):String{
    val save_path = readInitJson()["save_path"]
    Log.d("imgUrl", imgUrl)
    val path = "$save_path/$name/"
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
//        Log.d("download_img", "下载$file")
//        Glide.with(context).asBitmap().load(imgUrl).into(
//            object : SimpleTarget<Bitmap?>() {
//                override fun onResourceReady(
//                    resource: Bitmap,
//                    transition: Transition<in Bitmap?>?
//                ) {
//                    val out = FileOutputStream(file)
//                    resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
//                    out.flush()
//                    out.close()
//                }
//            }
//        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "$path$fileName"
}


/**
 * 移动文件
 * @param content 上下文
 */
fun moveJson(content: Context, name: String = "rule", initPath: String){
    val path = "$initPath/myApp/"
    if(!File("$path$name.json").exists()) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = content.assets.open("$name.json")
            InputStreamReader(inputStream)
            val buffer = ByteArray(1024)
            outputStream = FileOutputStream("$path$name.json")
            var n: Int
            while (-1 != inputStream.read(buffer).also { n = it }) {
                outputStream.write(buffer, 0, n)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }
}


fun saveJson(string: String, name: String = "rule"){
    var outputStream:OutputStream? = null
    try {
        var bytes = string.toByteArray()
        val b = bytes.size //是字节的长度，不是字符串的长度
        outputStream = FileOutputStream("/storage/emulated/0/myApp/${name}.json")
        outputStream.run {
            write(bytes, 0, b)
            write(bytes)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }finally {
        outputStream?.close()
    }
}

fun readInitJson(path: String = "/storage/emulated/0/myApp/init.json")
        :JSONObject{
    val newStringBuilder = StringBuilder()
    var inputStream: InputStream? = null
    try {
        inputStream = FileInputStream(path)
//        inputStream = content.assets.open("init.json")
        val isr = InputStreamReader(inputStream)
        val reader = BufferedReader(isr)
        var jsonLine: String?
        while (reader.readLine().also { jsonLine = it } != null) {
            newStringBuilder.append(jsonLine)
        }
        reader.close()
        isr.close()
        inputStream.close()
    }catch (e: IOException) {
        e.printStackTrace()
    }
    val result = newStringBuilder.toString()
    return JSONObject(result)

}

// 读取规则
fun readJson(name: String = "rule"):List<Rule>{
    val path = "/storage/emulated/0/myApp/${name}.json"
    val newStringBuilder = StringBuilder()
    var inputStream: InputStream? = null
    try {
        inputStream = FileInputStream(path)
//        inputStream = content.assets.open("rule.json")
        val isr = InputStreamReader(inputStream)
        val reader = BufferedReader(isr)
        var jsonLine: String?
        while (reader.readLine().also { jsonLine = it } != null) {
            newStringBuilder.append(jsonLine)
        }
        reader.close()
        isr.close()
        inputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
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
