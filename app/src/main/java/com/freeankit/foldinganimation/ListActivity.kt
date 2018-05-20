package com.freeankit.foldinganimation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_list.*

/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 18/05/2018 (MM/DD/YYYY)
 */
class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerview.apply {
            setHasFixedSize(true)
            recyclerview.layoutManager = LinearLayoutManager(this@ListActivity)
            recyclerview.adapter = SampleAdapter()
        }

    }
}