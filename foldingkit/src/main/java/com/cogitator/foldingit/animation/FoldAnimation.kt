package com.cogitator.foldingit.animation

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.Transformation
import java.lang.IllegalStateException


/**
 * Created by ankit on 26/01/2018.
 */
class FoldAnimation(foldMode: FoldAnimationMode, cameraHeight: Int, duration: Long) : Animation() {
    enum class FoldAnimationMode {
        FOLD_UP, UNFOLD_DOWN, FOLD_DOWN, UNFOLD_UP
    }

    private var mFoldMode: FoldAnimationMode? = null
    private var mCameraHeight: Int = 0
    private var mFromDegrees: Float = 0.toFloat()
    private var mToDegrees: Float = 0.toFloat()
    private var mCenterX: Float = 0.toFloat()
    private var mCenterY: Float = 0.toFloat()
    private var mCamera: Camera? = null


    init {
        this.mFoldMode = foldMode
        this.fillAfter = true
        this.duration = duration
        this.mCameraHeight = cameraHeight
    }

    fun withAnimationListener(animationListener: Animation.AnimationListener): FoldAnimation {
        this.setAnimationListener(animationListener)
        return this
    }

    fun withStartOffset(offset: Int): FoldAnimation {
        this.startOffset = offset.toLong()
        return this
    }

    fun withInterpolator(interpolator: Interpolator?): FoldAnimation {
        if (interpolator != null) {
            this.interpolator = interpolator
        }
        return this
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        this.mCamera = Camera()
        mCamera?.setLocation(0F, 0F, (-mCameraHeight).toFloat())

        this.mCenterX = (width / 2).toFloat()
        when (mFoldMode) {
            FoldAnimationMode.FOLD_UP -> {
                this.mCenterY = 0f
                this.mFromDegrees = 0f
                this.mToDegrees = 90f
            }
            FoldAnimationMode.FOLD_DOWN -> {
                this.mCenterY = height.toFloat()
                this.mFromDegrees = 0f
                this.mToDegrees = -90f
            }
            FoldAnimationMode.UNFOLD_UP -> {
                this.mCenterY = height.toFloat()
                this.mFromDegrees = -90f
                this.mToDegrees = 0f
            }
            FoldAnimationMode.UNFOLD_DOWN -> {
                this.mCenterY = 0f
                this.mFromDegrees = 90f
                this.mToDegrees = 0f
            }
            else -> throw IllegalStateException("Unknown animation mode.")
        }
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val camera = mCamera
        val matrix = t.matrix
        val fromDegrees = mFromDegrees
        val degrees = fromDegrees + (mToDegrees - fromDegrees) * interpolatedTime

        camera?.save()
        camera?.rotateX(degrees)
        camera?.getMatrix(matrix)
        camera?.restore()

        matrix.preTranslate(-mCenterX, -mCenterY)
        matrix.postTranslate(mCenterX, mCenterY)
    }

    override fun toString(): String {
        return "FoldAnimation{" +
                "mFoldMode=" + mFoldMode +
                ", mFromDegrees=" + mFromDegrees +
                ", mToDegrees=" + mToDegrees +
                '}'
    }
}