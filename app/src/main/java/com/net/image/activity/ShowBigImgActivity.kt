package com.net.image.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.net.image.R
import com.net.image.adapter.BigViewPagerAdapter
import com.net.image.model.saveImg
import com.net.image.model.shareImg
import kotlinx.android.synthetic.main.activity_show_big_img.*
import kotlinx.android.synthetic.main.activity_show_big_img.view.*
import kotlinx.android.synthetic.main.show_big_img_item.view.*
import kotlin.concurrent.thread


class ShowBigImgActivity : AppCompatActivity() {
    lateinit var pathName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_big_img)
        supportActionBar?.hide()


        // 获取图片地址列表
        val imgUrlList = intent.getStringArrayListExtra("imgList") as ArrayList<String>
        // 获取点击的图片
        var index_position = intent.getIntExtra("position", 0)
        var name = intent.getStringExtra("name")
        val aList = ArrayList<View>()
        for(imgUrl in imgUrlList){
            val view = layoutInflater.inflate(R.layout.show_big_img_item, null, false)
            Glide.with(view)
                .load(imgUrl)
                .dontAnimate()
                .into(view.big_img_view)
            aList!!.add(view)

            // 添加圆点
            val imageView = ImageView(view.context);
            imageView.layoutParams = ViewGroup.LayoutParams(20, 20)
            imageView.setPadding(20, 0, 20, 0)
            imageView.setBackgroundResource(R.drawable.indicator)
            view_indicator.addView(imageView)
        }

        show_big_viewPager.adapter = BigViewPagerAdapter(aList!!, imgUrlList)
        // 设置初始加载位点击的图片位置
        show_big_viewPager.currentItem = index_position

        // 设置初始加载点击的图片
        view_indicator[index_position].setBackgroundResource(R.drawable.indicators)
        show_big_viewPager.addOnPageChangeListener(object : OnPageChangeListener {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                // 设置当前点
                view_indicator[position].setBackgroundResource(R.drawable.indicators)
                //下面就是获取上一个位置，并且把点的状体设置成默认状体
                view_indicator[index_position].setBackgroundResource(R.drawable.indicator)
                //下面是记录本次的位置，因为在滑动，他就会变成过时的点了
                index_position = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        bottom_linear.down.setOnClickListener {
            thread {
                pathName = name?.let { it1 -> saveImg(it1, imgUrlList[index_position], this) }.toString()
                runOnUiThread {
                    Toast.makeText(baseContext, pathName, Toast.LENGTH_SHORT).show()
                }
            }
        }

        send_img.setOnClickListener {
            val imgPath = name?.let { it1 -> saveImg(it1, imgUrlList[index_position], this) }
            imgPath?.let { it1 ->
                if (name != null) {
                    shareImg(this, it1, name)
                }
            }
        }



        bottom_linear.setting_back.setOnClickListener {
            // 设置壁纸
            Glide.with(this.baseContext).asBitmap().load(imgUrlList[index_position]).into(
                object : SimpleTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
//                        WallpaperManager.getInstance(baseContext).setBitmap(resource)
//                        runOnUiThread {
//                            Toast.makeText(baseContext, "设置成功", Toast.LENGTH_SHORT).show()
//                        }
                        val intent = Intent(Intent.ACTION_ATTACH_DATA)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.putExtra("mimeType", "image/*")
                        val uri: Uri = Uri.parse(
                            MediaStore.Images.Media
                                .insertImage(
                                    baseContext.contentResolver,
                                    resource, null, null
                                )
                        )
                        intent.data = uri
                        startActivityForResult(intent, 1)
                    }


                })


        }


    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()

    }



}




