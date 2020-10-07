package com.net.image.model


// 返回对应分类的地址
/**
 * @param rule 规则
 * @param num
 */
fun changeSort(rule: Rule, num:Int, pageNum:Int): String {
    when(rule.sourceName){
        "POCO" ->{
            val type = rule.sortUrl.split("\n")[num].split("::")[1]
            rule.data = getNextData(rule.data, pageNum, type)
            return rule.postUrl
        }
        else ->{
            val sortList = rule.sortUrl.split("\n")
            if (sortList.size > num){
                val sort = sortList[num].split("::")
                if (sort.size == 2){
                    return sort[1].replace("{{page}}", pageNum.toString())
                }
            }
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
