package com.net.image.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.gson.Gson
import com.net.image.R
import com.net.image.adapter.ImgIndex
import com.net.image.adapter.ImgIndexAdapter
import com.net.image.model.*
import com.net.image.model.FileUtils.getFilePathByUri
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(){
    private var ruleCurNum = 0   // 第几个规则
    private var pageNum:Int = 1  // 页码
    private lateinit var rule:Rule    // 当前选择规则
    var ruleList:List<Rule>  = ArrayList()   // 规则读取
    private var sortNum = 0   // 第几个分类
    private var isBottomRefreshing:Boolean = false    // 底部刷新
    private lateinit var adapter:ImgIndexAdapter
    private val imgList = ArrayList<ImgIndex>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.let{
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.menu)
        }

        adapter = ImgIndexAdapter(this, ruleCurNum)
        recyclerView.adapter = adapter


        moveJson(this, "rule")
        moveJson(this, "init")

        ruleList = readJson(this)
        initUI()
    }

    private fun updateRecyclerView(sortUrl: String, delData:Boolean=true){
        // 更新页面数据
        if (delData) {
            imgList.clear()
            recyclerView.removeAllViews()
            top_bar.visibility = View.VISIBLE
        }
        thread {
            Log.d("rule", rule.toString())
            val indexDataList = getList(rule, sortUrl, pageNum)

            if (indexDataList.isNotEmpty()){
                isBottomRefreshing = false
                for (img_url in indexDataList) {
                    imgList.add(ImgIndex(img_url.title, img_url.href, img_url.src))
                }
                runOnUiThread {
//                    val adapter = ImgIndexAdapter(this, rule, ruleCurNum)
//                    recyclerView.adapter = adapter
//
                    adapter.setData(imgList)
                    adapter.notifyDataSetChanged()
                    top_bar.visibility = View.GONE
                    index_progressBar.visibility = View.GONE
                }
            }else{
                runOnUiThread {
                    Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateSpinner(sortNameList: ArrayList<String>){
        // 更新下拉选项
        val arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, sortNameList)
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
        top_bar.visibility = View.VISIBLE
    }

    private fun changeSource(id: Int) {
        // 换源
        ruleCurNum = id
        rule = ruleList[id]
        title = rule.sourceName

        val sortNameList = getSortNameList(rule)
        val sortUrl = changeSort(rule, 0, pageNum)
        updateSpinner(sortNameList)
        updateRecyclerView(sortUrl)
    }

    private fun initUI(){

//        recyclerView.layoutManager =
//            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager

        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lastVisiblePosition: Int = layoutManager.findLastVisibleItemPosition()
                    if (lastVisiblePosition >= layoutManager.itemCount - 1) {
                        Log.d("layoutManager", layoutManager.itemCount.toString())
//                        System.out.println("====自动加载");
                        if (layoutManager.itemCount != 0 && !isBottomRefreshing) {

                            nextPage(false)
                            Log.d("isBottomRefreshing", isBottomRefreshing.toString())

                            index_progressBar.visibility = View.VISIBLE
                            isBottomRefreshing = true
                        }
                    }
                }
            }
        })

        initNavigationView()
        rule = ruleList[ruleCurNum]
        title = rule.sourceName

        val sortNameList = getSortNameList(rule)
        updateSpinner(sortNameList)


        button_next.setOnClickListener {
            nextPage()
        }

        button_pre.setOnClickListener {
            prePage()
        }


        set_page_num.setOnEditorActionListener {
                _, _, _ ->
            val curPageNum = set_page_num.text.toString()
            try {
                val num = curPageNum.trim().toInt()
                setPageNum(num)
            } catch (e: NumberFormatException) {
                Log.d("int", e.toString())
            }

            false
        }


        // 选择规则，换源
        navigation_view.setNavigationItemSelectedListener {

            rule = ruleList[it.itemId]

            // 置顶点击的，只是改变文件
            val readJson = ruleList.toMutableList()
            readJson.removeAt(it.itemId)
            readJson.add(0, rule)
            saveJson(this, Gson().toJson(readJson).toString())

//            initNavigationView()
            changeSource(it.itemId)
            true
        }

        // 选择类别
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, positon: Int, id: Long
            ) {
                pageNum = 1
                val sortUrl = changeSort(rule, positon, pageNum)
                Log.d("sortUrl", sortUrl)
                sortNum = positon
                updateRecyclerView(sortUrl)
                set_page_num.setText("1")
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }



    private fun initNavigationView(){
        navigation_view.menu.clear()
        Log.d("initNavigationView", "initNavigationView")
        navigation_view.run {
            for (sum in ruleList.indices){
                Log.d("name", ruleList[sum].sourceName)
                navigation_view.menu.add(1, sum, sum, ruleList[sum].sourceName)
            }
        }

    }


    // 上一页
    private fun prePage() {
        if (pageNum > 1) {
            pageNum -= 1
            val sortUrl = changeSort(rule, sortNum, pageNum)
            set_page_num.setText(pageNum.toString())
            updateRecyclerView(sortUrl)
        }
    }

    private fun setPageNum(num: Int){
        pageNum = num
        val sortUrl = changeSort(rule, sortNum, pageNum)
        set_page_num.setText(pageNum.toString())
        updateRecyclerView(sortUrl)
    }


    // 下一页
    private fun nextPage(delData: Boolean=true) {
//         when (curPageNum) {
//             pageNum -> pageNum += 1
//             else -> pageNum = curPageNum
//        }
        pageNum += 1
        val sortUrl = changeSort(rule, sortNum, pageNum)
        set_page_num.setText(pageNum.toString())
        updateRecyclerView(sortUrl, delData)
    }

    // 导航栏按钮功能
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
            R.id.edit_rule -> {
                val intent = Intent(this@MainActivity, RuleActivity::class.java)
                Log.d("edit,rule", ruleCurNum.toString())
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

            R.id.add_local_rule -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*" //设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, 2)
            }

            R.id.add_net_rule -> {

            }

            R.id.save_pos -> {
                // 保存文件夹
//                intent = volume.createAccessIntent(null)
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, 3)
//                val sm = getSystemService(Context.STORAGE_SERVICE) as StorageManager
//                val volume: StorageVolume = sm.primaryStorageVolume
//                volume.createAccessIntent(null).also { intent ->
//                    startActivityForResult(intent, 3)
//                }
            }

            R.id.copy_rule ->{
                putTextIntoClip(this, Gson().toJson(rule).toString())
                Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
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
            3 -> {
                val uri: Uri? = data?.data
                val fromTreeUri = uri?.let { DocumentFile.fromTreeUri(this, it)?.uri }
                val filePath = fromTreeUri?.let { getFilePathByUri(baseContext, it) }.toString()
                Log.d("pathUrl", filePath)
                val initJson = readInitJson(this)

                initJson.put("save_path", filePath)
                Log.d("initJson", initJson.toString())
                saveJson(this, initJson.toString(), "init")
            }
            2 -> {
                val uri: Uri? = data?.data
                val filePathByUri = uri?.let { getFilePathByUri(baseContext, it) }
                Log.d("file", "${filePathByUri.toString()} + ${uri.toString()}")

            }
            1 -> when (resultCode) {
                RESULT_OK -> {
                    ruleList = readJson(this)    // 规则读取
                    initNavigationView()
                    ruleCurNum = 0
                    changeSource(ruleCurNum)
                }
                RESULT_CANCELED -> {

                }

            }
        }
    }
}


