package com.net.image.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.net.image.R
import com.net.image.adapter.RemoveRuleAdapter
import com.net.image.model.readJson
import kotlinx.android.synthetic.main.activity_remove.*


class RemoveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove)
        setSupportActionBar(remove_toolbar)
        supportActionBar?.let{
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.back)
        }

        val readJson = readJson()
        val nameList = ArrayList<String>()
        val layoutManager = LinearLayoutManager(this)
        remove_recyclerview.layoutManager = layoutManager
        for (rule in readJson) nameList.add(rule.sourceName)
        val removeRuleAdapter = RemoveRuleAdapter(this, nameList)
        remove_recyclerview.adapter = removeRuleAdapter
//        removeRuleAdapter.setOnClickListener(object : RemoveRuleAdapter.OnClickListener{
//            override fun onClickItemView() {
//                TODO("Not yet implemented")
//            }
//
//        })

    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(RESULT_OK)
                finish()
            }
        }
        return true
    }
}