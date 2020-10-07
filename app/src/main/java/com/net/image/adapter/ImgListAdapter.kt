package com.net.image.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.net.image.R
import com.net.image.activity.ShowBigImgActivity
import java.util.ArrayList

class ImgListAdapter(val imgList:List<ImgPath>) :
    RecyclerView.Adapter<ImgListAdapter.ViewHolder>(){
    private var context: Context? = null
    lateinit var  imgUrlList:List<String>;

    constructor(context: Context, imgList: List<ImgPath>, imgUrlList:List<String>):this(imgList){
        // 传入上下文
        this.context = context
        this.imgUrlList = imgUrlList
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val imageView: ImageView = view.findViewById(R.id.image_list_img)

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.img_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imgPath = imgList[position]

        Glide.with(holder.itemView).load(imgPath.imgPath)//.thumbnail(0.5f)
            .into(holder.imageView)
        holder.imageView.setOnClickListener {
            Log.d("path", imgList.toString())
            val intent = Intent(context, ShowBigImgActivity::class.java)
            // 传递数据, 图片列表

            intent.putStringArrayListExtra("imgList", imgUrlList as ArrayList<String>?)
            intent.putExtra("position", position)
            intent.putExtra("name", imgPath.name)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            context?.startActivity(intent)
        }



    }

    override fun getItemCount() = imgList.size

    /**
     * @param imgPath 图片地址
     * @param imgId R.
     */
    class ImgPath(val name:String, val imgPath:String)
}