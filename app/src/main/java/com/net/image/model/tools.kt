package com.net.image.model


import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.*
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList


fun getNextData(data:String, pageNum:Int=0, type:String=""):String{
    val jsonObject = JSONObject(data)
//    val next = jsonObject.keys().next()
    var jsonObject1 = JSONObject()
    for(next in jsonObject.keys()) {
        try {
            jsonObject1 = JSONObject(jsonObject.getString(next))
        }catch (e:Exception){
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
                param.put("works_category", 1)
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


fun getSignCode(param:String):String{
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
 * @param path 图片列表页地址
 * @return 图片地址列表
 */
fun getImgUrlList(path: String):List<String>{
    // 获取图片地址列表
    val document: Document = Jsoup.connect(path).get();
    val text = document.selectFirst("#imgs_json").text()
    val jsonArray = JSONArray(text)

    val imgList = ArrayList<String>()
    for(i in 0 until jsonArray.length()){
        val o = jsonArray.get(i) as JSONObject
        imgList.add("http://imgoss.cnu.cc/" + o.getString("img"))
    }
    return imgList
}



/**
 * @param name 文件保存位置："/storage/emulated/0/myApp/img/$name/"
 * @param imgUrl :Bitmap
 * @param context 上下文
 */
fun saveImg(
    name: String,
    imgUrl: String,
    context: Context,
    ):String{
    val save_path = readInitJson(context)["save_path"]
    Log.d("imgUrl", imgUrl)
    val path = "$save_path$name/"
    val fileName: String = File(imgUrl).name.replace(Regex("\\?.*"), "")
    try {
//        val bitmap = Glide.with(context)
//            .asBitmap()
//            .load(imgUrl).error(R.drawable.load)
//            .submit().get()

        Glide.with(context).asBitmap().load(imgUrl).into(
            object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    if(!File(path).exists()){
                        File(path).mkdirs()
                        Log.d("download_img", "创建文件夹$path")
                    }
                    File(imgUrl).name
                    val file = File("$path$fileName")
                    Log.d("download_img", "下载$file")
                    val out = FileOutputStream(file)
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush()
                    out.close()
                }
            }
        )


    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "$path$fileName"
}

/**
 * 读取properties文件
 */
fun readProperties(c: Context): Properties {

    val props = Properties()
    try {
        //方法一：通过activity中的context攻取setting.properties的FileInputStream
        //注意这地方的参数appConfig在eclipse中应该是appConfig.properties才对,但在studio中不用写后缀
        //InputStream in = c.getAssets().open("appConfig.properties");
        val `in`: InputStream = c.assets.open("appConfig.properties")
        //方法二：通过class获取setting.properties的FileInputStream
        //InputStream in = PropertiesUtill.class.getResourceAsStream("/assets/  setting.properties "));
        val bufferedReader = BufferedReader(InputStreamReader(`in`))
        props.load(bufferedReader)

    } catch (e1: Exception) {
        // TODO Auto-generated catch block
        e1.printStackTrace()
    }
    return props
}

/**
 * 移动文件
 * @param content 上下文
 */
fun moveJson(content: Context){
    val path = "/storage/emulated/0/myApp/img/rule.json"
    if(!File(path).exists()) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = content.assets.open("rule.json")
            InputStreamReader(inputStream)
            val buffer = ByteArray(1024)
            outputStream = FileOutputStream(path)
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

fun saveInitJson(){

}

fun saveJson(string: String){
    var outputStream:OutputStream? = null
    try {
        var bytes = string.toByteArray()
        val b = bytes.size //是字节的长度，不是字符串的长度
        outputStream = FileOutputStream("/storage/emulated/0/myApp/img/rule.json")
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
