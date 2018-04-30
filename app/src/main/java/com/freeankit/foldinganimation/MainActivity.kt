package com.freeankit.foldinganimation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // get our folding cell


        // attach click listener to fold btn
        toggle_btn.setOnClickListener({
                folding_cell.toggle(false)
        })

        // attach click listener to toast btn
        toggle_instant_btn.setOnClickListener({
            folding_cell.toggle(true)
        })

    }
}
