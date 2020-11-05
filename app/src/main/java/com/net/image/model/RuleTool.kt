package com.net.image.model


import android.util.Log
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
 * @param data post请求表单数据
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
 * @param sourceImage  图片源地址
 * @param sourceIndexImgLast 首页缩略图后缀
 * @param cookie 登录
 * @param js 加载的js库
 * @param jsMethod js方法
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
    var sourceIndexImgLast: String = "",
    var nextPage: String = "",
    var reqMethod: String = "GET",
    var postUrl: String = "",
    var cookie: String = "",
    var js: String="",
    var jsMethod:String = ""
):Serializable



/**
 * @param href 图片列表页地址
 * @param title 图片列表页名字
 * @param src 显示的图片
 */
class IndexImageDate(var href: String, var title: String, var src: String)




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
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36 Edg/86.0.622.58")
                .get()

            Log.d("document", document.text())
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
    var js = ""
    if (rule.js.isNotEmpty()){
        for(jsPath in rule.js.split("\n"))
            js = getJs(jsPath).toString()
    }
    Log.d("data", rule.data)
    rule.data = runJs(js + rule.jsMethod, rule.data) as String
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
        Log.d("json", json.toString())
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



fun getImgUrlList(rule: Rule, path: String):List<String>{
    // 获取图片地址列表

    Log.i("href", path)
    val imgList = ArrayList<String>()
    try {
        val nextPage = getNextPage(rule, path)
        for (page in nextPage) {
            val document: Document = Jsoup.connect(page).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36 Edg/86.0.622.58").get();
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
    Log.d("imgList", imgList.toString())
    return imgList
}



