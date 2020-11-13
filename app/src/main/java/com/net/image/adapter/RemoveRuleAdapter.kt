package com.net.image.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.net.image.R
import com.net.image.model.Rule
import com.net.image.model.readJson
import com.net.image.model.saveJson
import com.google.gson.Gson

class RemoveRuleAdapter(val context: Context, private val nameList: MutableList<String>) :
    RecyclerView.Adapter<RemoveRuleAdapter.ViewHolder>(){


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        // 获取控件
        val removeName: TextView = view.findViewById(R.id.remove_name)
        val removeButton: ImageButton = view.findViewById(R.id.remove_button)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 添加控件位置
        val view = LayoutInflater.from(context)
            .inflate(R.layout.remove_recyclerview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 向控件中添加数据
        val name = nameList[position]
        holder.removeName.text = name
        holder.removeButton.setOnClickListener {
//            listener?.onClickItemView()
            AlertDialog.Builder(context).apply {
                setMessage("是否删除:$name")
                setTitle("")
                setIcon(R.drawable.reminder)
                setCancelable(false)
                setPositiveButton("yes"){ _, _ ->
                    nameList.removeAt(position)
                    //删除动画
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    val readJson = readJson(context).toMutableList()
                    readJson.removeAt(position)
                    saveJson(context, Gson().toJson(readJson).toString())
                }
                setNegativeButton("No"){ _, _ ->

                }
                show()
            }


        }
    }

    // 数据长度
    override fun getItemCount() = nameList.size
//
//    fun setOnClickListener(listener: OnClickListener?) {
//        this.listener = listener
//    }
//
//    private var listener: OnClickListener? = null
//
//    interface OnClickListener {
//        fun onClickItemView()
//    }
}