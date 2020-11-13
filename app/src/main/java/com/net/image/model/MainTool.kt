package com.net.image.model

import android.util.Log
import kotlin.concurrent.thread


// 返回对应分类的地址
/**
 * @param rule 规则
 * @param num
 */
fun changeSort(rule: Rule, num:Int, pageNum:Int): String {
    when(rule.js.length){
        0 ->{
            val sortList = rule.sortUrl.split("\n")
            if (sortList.size > num){
                val sort = sortList[num].split("::")
                if (sort.size == 2){
                    return sort[1].replace("{{page}}", pageNum.toString())
                }
            }
        }
        else ->{
            val urlData = rule.sortUrl.split("\n")[num].split("::", limit=2)[1]
//            rule.data = getNextData(rule.data, pageNum, type)
            Log.d("urlData", urlData)
            rule.data = urlData.split(",", limit = 2)[1].trim()
            return urlData.split(",", limit = 2)[0]
        }
    }
    return ""
}


fun getSortNameList(rule: Rule): ArrayList<String> {
    val sortNameList = ArrayList<String>()
    val sortList = rule.sortUrl.split("\n")
    for (sort in sortList){
        val name = sort.split("::")[0]
        sortNameList.add(name)
    }

    return sortNameList
}
