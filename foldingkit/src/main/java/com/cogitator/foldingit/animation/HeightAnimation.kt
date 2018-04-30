package com.cogitator.foldingit.animation

import android.view.View
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.Transformation

/**
 * Created by ankit on 26/01/2018.
 */
class HeightAnimation(mView: View, heightFrom: Int, heightTo: Int, duration: Int): Animation() {
    private val mView: View = mView
    private val mHeightFrom: Int = heightFrom
    private val mHeightTo: Int = heightTo

    init {
        this.duration = duration.toLong()
    }

    fun withInterpolator(interpolator: Interpolator?): HeightAnimation {
        if (interpolator != null) {
            this.interpolator = interpolator
        }
        return this
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
    }

    protected override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newHeight = mHeightFrom + (mHeightTo - mHeightFrom) * interpolatedTime

        if (interpolatedTime == 1f) {
            mView.getLayoutParams().height = mHeightTo
        } else {
            mView.getLayoutParams().height = newHeight.toInt()
        }
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }

    override fun isFillEnabled(): Boolean {
        return false
    }

    override fun toString(): String {
        return "HeightAnimation{" +
                "mHeightFrom=" + mHeightFrom +
                ", mHeightTo=" + mHeightTo +
                ", offset =" + startOffset +
                ", duration =" + duration +
                '}'.toString()
    }
}