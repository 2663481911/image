package com.net.image.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * @param viewLists view列表
 * @param imgUrlList 图片地址列表
 */
class BigViewPagerAdapter(private val viewLists:ArrayList<View>, private val imgUrlList:ArrayList<String>) : PagerAdapter() {

    override fun getCount(): Int {
        return viewLists.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewLists[position])
        return viewLists[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(viewLists[position])
    }
}