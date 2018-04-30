package com.freeankit.foldingit.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.widget.RelativeLayout

/**
 * Created by ankit on 26/01/2018.
 */

class FoldingKitView : RelativeLayout {

    var backView: View? = null
        private set
    var frontView: View? = null
        private set

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        this.layoutParams = layoutParams
        this.clipToPadding = false
        this.clipChildren = false
    }

    constructor(frontView: View, backView: View, context: Context) : super(context) {
        this.frontView = frontView
        this.backView = backView

        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)

        this.clipToPadding = false
        this.clipChildren = false

        if (this.backView != null) {
            this.addView(this.backView)
            val mBackViewParams = this.backView!!.layoutParams as RelativeLayout.LayoutParams
            mBackViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            this.backView!!.layoutParams = mBackViewParams
            layoutParams.height = mBackViewParams.height
        }

        if (this.frontView != null) {
            this.addView(this.frontView)
            val frontViewLayoutParams = this.frontView!!.layoutParams as RelativeLayout.LayoutParams
            frontViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            this.frontView!!.layoutParams = frontViewLayoutParams
        }

        this.layoutParams = layoutParams
    }

    fun withFrontView(frontView: View): FoldingKitView {
        this.frontView = frontView

        if (this.frontView != null) {
            this.addView(this.frontView)
            val frontViewLayoutParams = this.frontView!!.layoutParams as RelativeLayout.LayoutParams
            frontViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            this.frontView!!.layoutParams = frontViewLayoutParams
        }
        return this
    }

    fun withBackView(backView: View): FoldingKitView {
        this.backView = backView

        if (this.backView != null) {
            this.addView(this.backView)
            val mBackViewParams = this.backView!!.layoutParams as RelativeLayout.LayoutParams
            mBackViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            this.backView!!.layoutParams = mBackViewParams

            val layoutParams = this.layoutParams as RelativeLayout.LayoutParams
            layoutParams.height = mBackViewParams.height
            this.layoutParams = layoutParams
        }

        return this
    }

    fun animateFrontView(animation: Animation) {
        if (this.frontView != null)
            frontView!!.startAnimation(animation)
    }
}
