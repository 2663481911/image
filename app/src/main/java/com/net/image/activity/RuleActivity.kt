package com.net.image.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.net.image.R
import com.net.image.model.Rule
import com.net.image.model.readJson
import com.net.image.model.saveJson
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_rule_edit.*

class RuleActivity : AppCompatActivity() {
    private lateinit var name:String
    private lateinit var rule:Rule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_edit)
        setSupportActionBar(rule_toolbar)
        supportActionBar?.let{
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.back)
        }

        name = intent.getStringExtra("name").toString()
        rule = intent.getSerializableExtra("rule") as Rule


        when(name) {
            "edit" ->  {
                showRule(rule)
                title = rule.sourceName
            }
            "add" -> {
                title = "add"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.rule_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                setResult(RESULT_CANCELED)
                finish()
            }

            R.id.save_rule ->{
                val intent = Intent()
                when (name) {
                    "edit" -> {
                        var ruleNum = this.intent.getIntExtra("ruleCurNum", 0)
                        Log.d("name", "edit")
                        editRule(rule, ruleNum)
                    }
                    "add" -> {
                        editRule(rule, -1)
                    }
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        return true
    }

    private fun showRule(rule:Rule){
        rule_data.setText(rule.data)
        rule_href.setText(rule.ruleHref)
        rule_imageList.setText(rule.ruleImageList)
        rule_imageListData.setText(rule.imageListData)
        rule_imageListIsJson.setText(rule.ruleImageListIsJson.toString())
        rule_list.setText(rule.ruleList)
        rule_sortUrl.setText(rule.sortUrl)
        rule_sourceImage.setText(rule.sourceImage)
        rule_sourceName.setText(rule.sourceName)
        rule_sourceUrl.setText(rule.sourceUrl)
        rule_src.setText(rule.ruleSrc)
        rule_title.setText(rule.ruleTitle)
        rule_nextPage.setText(rule.nextPage)
        reqMethod.setText(rule.reqMethod)
        rule_imageUrlReplace.setText(rule.imageUrlReplace)
        rule_imageListIsJsonRe.setText(rule.ruleImageListIsJsonRe)
        rule_sourceIndexImgLast.setText(rule.sourceIndexImgLast)
        rule_postUrl.setText(rule.postUrl)
        rule_cookie.setText(rule.cookie)
    }

    private fun editRule(rule:Rule, ruleNum:Int){
        rule.imageUrlReplace = rule_imageUrlReplace.text.toString()
        rule.data = rule_data.text.toString()
        rule.ruleHref = rule_href.text.toString()
        rule.ruleImageList = rule_imageList.text.toString()
        rule.imageListData= rule_imageListData.text.toString()
        rule.ruleImageListIsJson = rule_imageListIsJson.text.toString()
        rule.ruleList = rule_list.text.toString()
        rule.sortUrl = rule_sortUrl.text.toString()
        rule.sourceImage = rule_sourceImage.text.toString()
        rule.sourceName= rule_sourceName.text.toString()
        rule.sourceUrl = rule_sourceUrl.text.toString()
        rule.ruleSrc = rule_src.text.toString()
        rule.ruleTitle = rule_title.text.toString()
        rule.nextPage = rule_nextPage.text.toString()
        rule.reqMethod = reqMethod.text.toString()
        rule.sourceIndexImgLast = rule_sourceIndexImgLast.text.toString()
        rule.ruleImageListIsJsonRe = rule_imageListIsJsonRe.text.toString()
        rule.postUrl = rule_postUrl.text.toString()
        rule.cookie = rule_cookie.text.toString()

        val readJson = readJson().toMutableList()
        if (ruleNum == -1)
            readJson.add(rule)
        else(ruleNum >= 0)
            readJson[ruleNum] = rule
        saveJson(Gson().toJson(readJson).toString())
        Log.d("readJson", Gson().toJson(readJson).toString())

    }
}


