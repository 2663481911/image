package com.net.image.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter


class IndexPathSpinnerAdapter(val content: Context, private val indexPathList: List<IndexPath>): BaseAdapter() {
    override fun getCount(): Int {
        return indexPathList.size
    }

    override fun getItem(position: Int): Any {
        return indexPathList[position];
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {


        return convertView!!
    }

    class IndexPath(name: String, path: String)
}