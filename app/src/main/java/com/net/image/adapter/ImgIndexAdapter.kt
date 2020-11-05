package com.net.image.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.net.image.R
import com.net.image.activity.ImgListActivity
import com.net.image.model.Rule


class ImgIndexAdapter(
    val context: Context, private val imgList: List<ImgIndex>, val rule: Rule,
    val curRuleNum: Int
) :
    RecyclerView.Adapter<ImgIndexAdapter.ViewHolder>(){



    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        // 获取控件
        val indexImg:ImageView = view.findViewById(R.id.imageIndex_img)
        val indexName:TextView = view.findViewById(R.id.imageIndex_name)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


        // 添加控件位置
        val view = LayoutInflater.from(context)
            .inflate(R.layout.img_index_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 向控件中添加数据
        val imgIndex = imgList[position]
        if (imgIndex.imgId != 0) holder.indexImg.setImageResource(imgIndex.imgId)
        else Glide.with(holder.itemView)
            .load(imgIndex.imgSrc)
            .centerCrop()
            .placeholder(R.drawable.load)
            .dontAnimate()
            .into(holder.indexImg)

        holder.indexName.text = imgIndex.name

        // 添加点击事件
        holder.itemView.setOnClickListener {
            Log.d("path", imgIndex.path)
            val intent = Intent(context, ImgListActivity::class.java)
            // 传递数据
            intent.putExtra("path", imgIndex.path)
            intent.putExtra("title", imgIndex.name)
            intent.putExtra("rule", rule)
            intent.putExtra("ruleCurNum", curRuleNum)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            context?.startActivity(intent)
        }


    }

    // 数据长度
    override fun getItemCount() = imgList.size
}


/**
 * @param name 名字
 * @param path 地址
 * @param imgSrc 图片
 */
class ImgIndex(val name: String, val path: String, val imgId: Int = 0, val imgSrc: String = ""){
    constructor(name: String, imgId: Int, path: String):this(name, path, imgId, "")
    constructor(name: String, path: String, imgPath: String):this(name, path, 0, imgPath)
}