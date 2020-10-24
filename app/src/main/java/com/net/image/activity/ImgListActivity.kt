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
import com.net.image.R
import com.net.image.adapter.ImgListAdapter
import com.net.image.model.Rule
import com.net.image.model.getImgUrlList
import com.net.image.model.saveImg
import kotlinx.android.synthetic.main.activity_img_list.*
import kotlin.concurrent.thread


class ImgListActivity : AppCompatActivity() {

    private val arrayList = ArrayList<ImgListAdapter.ImgPath>()
    lateinit var  imgList:List<String>
    lateinit var name:String
    lateinit var rule:Rule
    lateinit var path:String

    private fun initUI() {
        path = intent.getStringExtra("path").toString()
        name = intent.getStringExtra("title").toString()
        rule = intent.getSerializableExtra("rule") as Rule
        this.title = name

        thread {
            imgList = getImgUrlList(rule, path)
            runOnUiThread {
                initImgIndex()
                val layoutManager = GridLayoutManager(this@ImgListActivity, 1)
                img_list_recyclerView.layoutManager = layoutManager
                val adapter = ImgListAdapter(this@ImgListActivity, arrayList, imgList)
                img_list_recyclerView.adapter = adapter
            }
        }

        list_down_all.setOnClickListener {
            Toast.makeText(this, "下载中。。。。", Toast.LENGTH_SHORT).show()
            for(imgUrl in imgList){
                val pathName = saveImg(name, imgUrl,this)
                Log.d("download_img", pathName)
            }
//            Toast.makeText(this@ImgListActivity, "下载完成", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_list)
        setSupportActionBar(list_toolbar)
        supportActionBar?.let{
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.back)
        }
        initUI()

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
            R.id.open_browser ->{
                val uri: Uri = Uri.parse(path)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        return true
    }

}
