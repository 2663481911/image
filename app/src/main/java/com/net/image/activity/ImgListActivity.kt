package com.net.image.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.net.image.R
import com.net.image.adapter.ImgListAdapter
import com.net.image.model.*
import kotlinx.android.synthetic.main.activity_img_list.*
import kotlin.concurrent.thread


class ImgListActivity : AppCompatActivity() {

    private val arrayList = ArrayList<ImgListAdapter.ImgPath>()
    var  imgList:List<String> = ArrayList()
    lateinit var name:String
    lateinit var rule:Rule
    lateinit var path:String
    private var layoutNum:Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_list)
        setSupportActionBar(list_toolbar)
        supportActionBar?.let{
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.back)
        }
        val readInitJson = readInitJson(this)
        layoutNum = readInitJson["list_layout"].toString().toInt()
        val readJson = readJson(this)
        rule = readJson[0]
        initUI()
    }

    private fun initUI() {
        path = intent.getStringExtra("path").toString()
        name = intent.getStringExtra("title").toString()
//        rule = intent.getSerializableExtra("rule") as Rule
        this.title = name



        thread {
            if (imgList.isEmpty())
                imgList = getImgUrlList(rule, path)
            runOnUiThread {
                if (arrayList.isEmpty())
                    initImgIndex()
                when (layoutNum) {
                    //  流式布局
                    1 -> img_list_recyclerView.layoutManager = StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                    2 -> {
                        val layoutManager = GridLayoutManager(this@ImgListActivity, 2)
                        img_list_recyclerView.layoutManager = layoutManager
                    }
                    else -> {
                        val layoutManager = GridLayoutManager(this@ImgListActivity, 1)
                        img_list_recyclerView.layoutManager = layoutManager
                    }
                }


                val adapter = ImgListAdapter(this@ImgListActivity, arrayList, imgList)
                img_list_recyclerView.adapter = adapter
            }
        }

        list_down_all.setOnClickListener {
            Toast.makeText(this, "下载中。。。。", Toast.LENGTH_SHORT).show()
            thread {
                for(imgUrl in imgList){
                    val pathName = saveImg(name, imgUrl, this)
                    Log.d("download_img", pathName)
                }
            }
//            Toast.makeText(this@ImgListActivity, "下载完成", Toast.LENGTH_SHORT).show()
        }
    }


    private fun initImgIndex(){
        // 添加图片地址
        for(img_url in imgList){
            arrayList.add(ImgListAdapter.ImgPath(name, img_url))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.img_list_show_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.open_browser -> {
                val uri: Uri = Uri.parse(path)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            R.id.switch_layout -> {
                if (layoutNum == 3)
                    layoutNum = 1
                else layoutNum += 1
                val readInitJson = readInitJson(this)
                readInitJson.put("list_layout", layoutNum)
                saveJson(this, readInitJson.toString(), "init")
                initUI()
            }
        }
        return true
    }

}
