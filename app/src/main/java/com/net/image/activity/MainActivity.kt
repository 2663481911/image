package com.net.image.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.net.image.R
import com.net.image.adapter.ImgIndex
import com.net.image.adapter.ImgIndexAdapter
import com.net.image.model.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(){
    private var ruleCurNum = 0   // 第几个规则
    private var pageNum:Int = 1  // 页码
    private lateinit var rule:Rule    // 当前选择规则



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.let{
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.menu)
        }
        initUI()
    }

    private fun updateRecyclerView(sortUrl:String){
        // 更新页面数据
        thread {
            val indexDataList = getList(rule, sortUrl)
            if (indexDataList.isNotEmpty()){
                val imgList = ArrayList<ImgIndex>()
                for (img_url in indexDataList) {
                    imgList.add(ImgIndex(img_url.title, img_url.href, img_url.src))
                }
                runOnUiThread {
                    val adapter = ImgIndexAdapter(this, imgList, rule, ruleCurNum)
                    recyclerView.adapter = adapter
                    index_progressBar.visibility = View.GONE
                }
            }else{
                runOnUiThread {
                    Toast.makeText(this, "没有下一页", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateSpinner(sortNameList:ArrayList<String>){
        // 更新下拉选项
        val arrayAdapter =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, sortNameList)
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
    }

    private fun changeSource(readJson:List<Rule>, id:Int) {
        // 换源
        ruleCurNum = id
        rule = readJson[id]
        title = rule.sourceName
        val sortNameList = getSortNameList(rule)
        val sortUrl = changeSort(rule, 0, pageNum)
        updateSpinner(sortNameList)
        updateRecyclerView(sortUrl)
    }

    private fun initUI(){
        moveJson(this)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager
        var readJson:List<Rule>  = readJson()    // 规则读取

        initNavigationView(readJson)
        rule = readJson[ruleCurNum]
        title = rule.sourceName

        val sortNameList = getSortNameList(rule)
        updateSpinner(sortNameList)

        val sortUrl = changeSort(rule, 0, pageNum)
        updateRecyclerView(sortUrl)

        var sortNum = 0
        // 点击end按钮重新加载
        button_end.setOnClickListener {
            val curPageNum = page_num.text.toString()
            nextPage(sortNum, curPageNum.toInt())
        }

        // 选择规则，换源
        navigation_view.setNavigationItemSelectedListener {
            changeSource(readJson, it.itemId)
            true
        }

        // 选择类别
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, positon: Int, id: Long) {
                pageNum = 1
                val sortUrl = changeSort(rule, positon, pageNum)
                Log.d("sortUrl", sortUrl)
                sortNum = positon
                updateRecyclerView(sortUrl)
                page_num.setText("1")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun initNavigationView(readJson:List<Rule>){
        navigation_view.menu.clear()
        navigation_view.run {
            for (sum in readJson.indices){
                navigation_view.menu.add(1, sum, sum, readJson[sum].sourceName)
            }
        }
    }

    // 下一页
    private fun nextPage(sortNum: Int, curPageNum: Int) {
         when (curPageNum) {
           pageNum -> pageNum += 1
             else -> pageNum = curPageNum
        }
        val sortUrl = changeSort(rule, sortNum, pageNum)
        page_num.setText(pageNum.toString())
        updateRecyclerView(sortUrl)
    }

    // 导航栏按钮功能
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
            R.id.edit_rule -> {
                val intent = Intent(this@MainActivity, RuleActivity::class.java)
                Log.d("edit,rule", ruleCurNum.toString() )
                intent.putExtra("rule", rule)
                intent.putExtra("ruleCurNum", ruleCurNum)
                intent.putExtra("name", "edit")
                startActivityForResult(intent, 1)
            }
            R.id.add_rule -> {
                val intent = Intent(this@MainActivity, RuleActivity::class.java)
                intent.putExtra("rule", rule)
                intent.putExtra("name", "add")
                startActivityForResult(intent, 1)
            }
            R.id.remove_rule -> {
                val intent = Intent(this@MainActivity, RemoveActivity::class.java)
                intent.putExtra("rule", rule)
                intent.putExtra("ruleCurNum", ruleCurNum)
                intent.putExtra("name", "remove")
                startActivityForResult(intent, 1)
            }

            R.id.add_local_rule ->{

            }
            R.id.add_net_rule ->{

            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    // 改变规则后返回事件处理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            1 -> when (resultCode) {
                RESULT_OK ->{
                    var readJson:List<Rule>  = readJson()    // 规则读取
                    initNavigationView(readJson)
                    if (readJson.size <= ruleCurNum)
                        ruleCurNum = 0
                    changeSource(readJson, ruleCurNum)
                }
                RESULT_CANCELED ->{

                }

            }
        }
    }


}


