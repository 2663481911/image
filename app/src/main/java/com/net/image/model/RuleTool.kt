package com.net.image.model

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.*


/**
 * @param ruleListIsJson   首页是否是json格式
 * @param data post请求表单数据json格式
 * @param sourceUrl 源地址，一般是首页
 * @param sourceName 网站名
 * @param sortUrl 分类
 * @param ruleList 列表规则
 * @param ruleHref 列表地址规则
 * @param ruleTitle 列表标题规则
 * @param ruleSrc 列表图标规则
 * @param ruleImageList 图片列表规则
 * @param ruleImageListIsJson 图片列表是否是json格式
 * @param ruleImageListIsJsonRe 是否运用正则
 * @param imageUrlReplace 图片地址替换规则
 * @param imageListData json格式的键
 * @param ruleImageSrc 图片列表地址规则
 * @param sourceImage  图片源地址
 * @param sourceIndexImgLast 首页缩略图后缀
 */
class Rule(
    var ruleListIsJson: String = "0",
    var data: String = "",
    var sourceUrl: String = "",
    var sourceName: String = "",
    var sortUrl: String = "",
    var ruleList: String = "",
    var ruleHref: String = "",
    var ruleTitle: String = "",
    var ruleSrc: String = "",
    var ruleImageList: String = "",
    var ruleImageListIsJson: String = "0",
    var ruleImageListIsJsonRe: String = "0",
    var imageUrlReplace: String = "",
    var imageListData: String = "",
    var sourceImage: String = "",
    var nextPage: String = "",
    var reqMethod: String = "GET",
    var sourceIndexImgLast: String = "",
    var postUrl: String = "",
    var cookie: String = ""
):Serializable



/**
 * @param href 图片列表页地址
 * @param title 图片列表页名字
 * @param src 显示的图片
 */
class IndexImageDate(var href: String, var title: String, var src: String)


// 读取规则
fun readJson(path: String="/storage/emulated/0/myApp/img/rule.json"):List<Rule>{
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
    val jsonArray = JSONArray(result)
    Log.d("json", result)
    val typeOf = object : TypeToken<List<Rule>>() {}.type
//    val gson = GsonBuilder().setLenient().create()
    return Gson().fromJson(jsonArray.toString(), typeOf)
}

fun convertCookie(cookie: String): HashMap<String, String>? {
    val cookiesMap = HashMap<String, String>()
    if (cookie!="") {
        val split = cookie.split(";")
        for (item in split)
            cookiesMap[item.split("=")[0]] = item.split("=")[1]
    }
    return cookiesMap
}


/**
 * 获取列表
 * @param path 首页地址
 * @param rule
 * @return List<IndexImageDate> 图片列表页地址列表：
 */
fun getList(rule: Rule, path: String):List<IndexImageDate>{
    if (rule.ruleListIsJson != "1") {
        val imgUrlList = ArrayList<IndexImageDate>()
        try {
            val document: Document = Jsoup.connect(path)
                .cookies(convertCookie(rule.cookie))
                .get()
            val elements = document.select(rule.ruleList)

            for (img in elements) {
                val ruleHrefList = rule.ruleHref.split(",")
                val ruleSrcList = rule.ruleSrc.split(",")
                val href = when (ruleHrefList.size) {
                    1 -> img.attr(rule.ruleHref.trim())
                    else -> img.selectFirst(ruleHrefList[0].trim()).attr(ruleHrefList[1].trim())
                }
                Log.d("href", href)

                val src = when (ruleSrcList.size) {
                    1 -> img.attr(rule.ruleSrc.trim())
                    else -> img.selectFirst(ruleSrcList[0].trim()).attr(ruleSrcList[1].trim())
                }
                val title = when (rule.ruleTitle) {
                    "" -> img.text()
                    else -> img.selectFirst(rule.ruleTitle.trim()).text()
                }

                imgUrlList.add(IndexImageDate(href, title, src))
            }
        } catch (e: Exception) {
            e.printStackTrace();
        }
//        Log.d("imgUrlList", imgUrlList.toString())
        return imgUrlList
    }else{
        return postMethod(rule, path)
    }
}


fun getNextPage(rule: Rule, path: String):Set<String>{
    val pathList = HashSet<String>()
        if (rule.nextPage != null && rule.nextPage.isNotEmpty()) {
            val document = Jsoup.connect(path).get()
            val elements = document.select(rule.nextPage)
            for (document in elements) {
                val href = document.attr("abs:href")
                if (href != "")
                    pathList.add(document.attr("abs:href"))
            }

            if (elements.size > 1) {
                val element = elements[elements.size - 1]
                val href = element.attr("abs:href")
                val document = Jsoup.connect(href).get()
                val documents = document.select(rule.nextPage)
                for (document in documents) {
                    val href = document.attr("abs:href")
                    if (href != "")
                        pathList.add(document.attr("abs:href"))
                }
            }
        } else {
            pathList.add(path)
        }
    return pathList
}

fun postMethod(rule: Rule, path: String):List<IndexImageDate>{
    val imgUrlList = ArrayList<IndexImageDate>()
    val okHttpClient = OkHttpClient().newBuilder().build()
    val jsonObject = JSONObject(rule.data)
    val requestBody: RequestBody = FormBody.Builder().apply {
        for(key in jsonObject.keys()){
            add(key, jsonObject.getString(key))
        }
//        getData(rule.data)
    }
        .build()
    val request: Request = Request.Builder()
        .post(requestBody)
        .url(path)
        .build()
    val call: Call = okHttpClient.newCall(request)
    try {
        val response:Response  = call.execute();
        val json = response.body?.string()
        val document: Any = Configuration.defaultConfiguration().jsonProvider().parse(json)
        val srcList: List<String> = JsonPath.read(document, rule.ruleSrc)
        val hrefList: List<String> = JsonPath.read(document, rule.ruleHref)
        val titleList: List<String> = JsonPath.read(document, rule.ruleTitle)
        for (i in srcList.indices){
            imgUrlList.add(
                IndexImageDate(
                    hrefList[i], titleList[i], rule.sourceImage +
                            srcList[i] + rule.sourceIndexImgLast
                )
            )
//            imgUrlList.add(IndexImageDate(href, title, src))
        }
    } catch (e: Exception) {
        Log.i("runPost", e.toString());
    }
    return imgUrlList
}

fun getPocoImgUrlLIst(rule: Rule, path: String):List<String>{
    val imgList = ArrayList<String>()
    val document = Jsoup.connect(path).userAgent("Mozilla/5.0").get()
    val text = document.selectFirst(rule.ruleImageList).text()
//    Log.d("recommand_list", text)
    for(r in Regex(rule.imageListData).findAll(text)){
        imgList.add(
            rule.sourceImage + (r.groups[1]?.value ?: "")
                .trim().replace(Regex(rule.imageUrlReplace), "")
        )
    }
    return imgList
}


fun getImgUrlList(rule: Rule, path: String):List<String>{
    // 获取图片地址列表

    Log.i("href", path+"1")
    val imgList = ArrayList<String>()
    try {
        val nextPage = getNextPage(rule, path)
        for (page in nextPage) {
            val document: Document = Jsoup.connect(page).userAgent("Mozilla/5.0").get();
            val elements = document.select(rule.ruleImageList)

            when {
                rule.ruleImageListIsJson == "1" -> {
                    val jsonArray = JSONArray(elements[0].text())
                    for (i in 0 until jsonArray.length()) {
                        val o = jsonArray.get(i) as JSONObject
                        imgList.add(rule.sourceImage + o.getString(rule.imageListData))
                    }
                }
                rule.ruleImageListIsJsonRe == "1" -> {
                    val text = elements[0].text()
                    for (r in Regex(rule.imageListData).findAll(text)) {
                        imgList.add(
                            rule.sourceImage + (r.groups[1]?.value ?: "")
                                .trim().replace(Regex(rule.imageUrlReplace), "")
                        )
                    }
                }
                else -> {
                    for (element in elements) {
                        val ruleImageSrcList = rule.imageListData.split(",")
                        val replaceList = rule.imageUrlReplace.split(",")
                        val imgUrl = when (ruleImageSrcList.size) {
                            1 -> rule.sourceImage + element.attr(ruleImageSrcList[0].trim())
                            else -> rule.sourceImage + element.selectFirst(ruleImageSrcList[0])
                                .attr(
                                    ruleImageSrcList[1].trim()
                                )
                        }
                        when (replaceList.size) {
                            1 -> imgList.add(imgUrl.replace(Regex(rule.imageUrlReplace), ""))
                            else -> imgList.add(
                                imgUrl.replace(
                                    Regex(replaceList[0]),
                                    replaceList[1]
                                )
                            )
                        }
                    }
                }
            }
        }
    }catch (e: java.lang.Exception){
        Log.i("ed", e.toString())
    }
        return imgList
}



