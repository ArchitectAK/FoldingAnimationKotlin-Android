package com.cogitator.foldingit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.cogitator.foldingit.animation.AnimationEndListener
import com.cogitator.foldingit.animation.FoldAnimation
import com.cogitator.foldingit.animation.HeightAnimation
import com.freeankit.foldingit.R
import com.freeankit.foldingit.views.FoldingKitView
import java.util.*

/**
 * Created by ankit on 26/01/2018.
 */

open class FoldingKit : RelativeLayout {

    private val TAG = "FoldingKit"

    // state variables
    var isUnfolded: Boolean = false
        private set
    private var mAnimationInProgress: Boolean = false

    // default values
    private val DEF_ANIMATION_DURATION = 1000
    private val DEF_BACK_SIDE_COLOR = Color.GRAY
    private val DEF_ADDITIONAL_FLIPS = 0
    private val DEF_CAMERA_HEIGHT = 30

    // current settings
    private var mAnimationDuration = DEF_ANIMATION_DURATION
    private var mBackSideColor = DEF_BACK_SIDE_COLOR
    private var mAdditionalFlipsCount = DEF_ADDITIONAL_FLIPS
    private var mCameraHeight = DEF_CAMERA_HEIGHT

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeFromAttributes(context, attrs)
        this.clipChildren = false
        this.clipToPadding = false
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeFromAttributes(context, attrs)
        this.clipChildren = false
        this.clipToPadding = false
    }

    constructor(context: Context) : super(context) {
        this.clipChildren = false
        this.clipToPadding = false
    }

    /**
     * Initializes folding cell programmatically with custom settings
     *
     * @param animationDuration    animation duration, default is 1000
     * @param backSideColor        color of back side, default is android.graphics.Color.GREY (0xFF888888)
     * @param additionalFlipsCount count of additional flips (after first one), set 0 for auto
     */
    fun initialize(animationDuration: Int, backSideColor: Int, additionalFlipsCount: Int) {
        this.mAnimationDuration = animationDuration
        this.mBackSideColor = backSideColor
        this.mAdditionalFlipsCount = additionalFlipsCount
    }

    /**
     * Initializes folding cell programmatically with custom settings
     *
     * @param animationDuration    animation duration, default is 1000
     * @param backSideColor        color of back side, default is android.graphics.Color.GREY (0xFF888888)
     * @param additionalFlipsCount count of additional flips (after first one), set 0 for auto
     */
    fun initialize(cameraHeight: Int, animationDuration: Int, backSideColor: Int, additionalFlipsCount: Int) {
        this.mAnimationDuration = animationDuration
        this.mBackSideColor = backSideColor
        this.mAdditionalFlipsCount = additionalFlipsCount
        this.mCameraHeight = cameraHeight
    }

    /**
     * Unfold cell with (or without) animation
     *
     * @param skipAnimation if true - change state of cell instantly without animation
     */
    private fun unfold(skipAnimation: Boolean) {
        if (isUnfolded || mAnimationInProgress) return

        // get main content parts
        val contentView = getChildAt(0) ?: return
        val titleView = getChildAt(1) ?: return

        // hide title and content views
        titleView.visibility = View.GONE
        contentView.visibility = View.GONE

        // Measure views and take a bitmaps to replace real views with images
        val bitmapFromTitleView = measureViewAndGetBitmap(titleView, this.measuredWidth)
        val bitmapFromContentView = measureViewAndGetBitmap(contentView, this.measuredWidth)

        if (skipAnimation) {
            contentView.visibility = View.VISIBLE
            this@FoldingKit.isUnfolded = true
            this@FoldingKit.mAnimationInProgress = false
            this.layoutParams.height = contentView.height
        } else {
            // create layout container for animation elements
            val foldingLayout = createAndPrepareFoldingContainer()
            this.addView(foldingLayout)
            // calculate heights of animation parts
            val heights = calculateHeightsForAnimationParts(titleView.height, contentView.height, mAdditionalFlipsCount)
            // create list with animation parts for animation
            val foldingCellElements = prepareViewsForAnimation(heights, bitmapFromTitleView, bitmapFromContentView)
            // start unfold animation with end listener
            val childCount = foldingCellElements.size
            val part90degreeAnimationDuration = mAnimationDuration / (childCount * 2)
            startUnfoldAnimation(foldingCellElements, foldingLayout, part90degreeAnimationDuration, object : AnimationEndListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    contentView.visibility = View.VISIBLE
                    foldingLayout.visibility = View.GONE
                    this@FoldingKit.removeView(foldingLayout)
                    this@FoldingKit.isUnfolded = true
                    this@FoldingKit.mAnimationInProgress = false
                }
            })

            startExpandHeightAnimation(heights, part90degreeAnimationDuration * 2)
            this.mAnimationInProgress = true
        }

    }

    /**
     * Fold cell with (or without) animation
     *
     * @param skipAnimation if true - change state of cell instantly without animation
     */
    private fun fold(skipAnimation: Boolean) {
        if (!isUnfolded || mAnimationInProgress) return

        // get basic views
        val contentView = getChildAt(0) ?: return
        val titleView = getChildAt(1) ?: return

        // hide title and content views
        titleView.visibility = View.GONE
        contentView.visibility = View.GONE

        // make bitmaps from title and content views
        val bitmapFromTitleView = measureViewAndGetBitmap(titleView, this.measuredWidth)
        val bitmapFromContentView = measureViewAndGetBitmap(contentView, this.measuredWidth)

        if (skipAnimation) {
            contentView.visibility = View.GONE
            titleView.visibility = View.VISIBLE
            this@FoldingKit.mAnimationInProgress = false
            this@FoldingKit.isUnfolded = false
            this.layoutParams.height = titleView.height
        } else {

            // create empty layout for folding animation
            val foldingLayout = createAndPrepareFoldingContainer()
            // add that layout to structure
            this.addView(foldingLayout)

            // calculate heights of animation parts
            val heights = calculateHeightsForAnimationParts(titleView.height, contentView.height, mAdditionalFlipsCount)
            // create list with animation parts for animation
            val foldingCellElements = prepareViewsForAnimation(heights, bitmapFromTitleView, bitmapFromContentView)
            val childCount = foldingCellElements.size
            val part90degreeAnimationDuration = mAnimationDuration / (childCount * 2)
            // start fold animation with end listener
            startFoldAnimation(foldingCellElements, foldingLayout, part90degreeAnimationDuration, object : AnimationEndListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    contentView.visibility = View.GONE
                    titleView.visibility = View.VISIBLE
                    foldingLayout.visibility = View.GONE
                    this@FoldingKit.removeView(foldingLayout)
                    this@FoldingKit.mAnimationInProgress = false
                    this@FoldingKit.isUnfolded = false
                }
            })
            startCollapseHeightAnimation(heights, part90degreeAnimationDuration * 2)
            this.mAnimationInProgress = true
        }


    }


    /**
     * Toggle current state of FoldingCellLayout
     */
    fun toggle(skipAnimation: Boolean) {
        if (this.isUnfolded) {
            this.fold(skipAnimation)
        } else {
            this.unfold(skipAnimation)
            this.requestLayout()
        }
    }

    /**
     * Create and prepare list of FoldingCellViews with different bitmap parts for fold animation
     *
     * @param titleViewBitmap   bitmap from title view
     * @param contentViewBitmap bitmap from content view
     * @return list of FoldingCellViews with bitmap parts
     */
    private fun prepareViewsForAnimation(viewHeights: List<Int>, titleViewBitmap: Bitmap, contentViewBitmap: Bitmap): List<FoldingKitView> {
        if (viewHeights.isEmpty())
            throw IllegalStateException("ViewHeights array must be not null and not empty")

        val partsList = ArrayList<FoldingKitView>()

        val partWidth = titleViewBitmap.width
        var yOffset = 0
        for (i in viewHeights.indices) {
            val partHeight = viewHeights[i]
            val partBitmap = Bitmap.createBitmap(partWidth, partHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(partBitmap)
            val srcRect = Rect(0, yOffset, partWidth, yOffset + partHeight)
            val destRect = Rect(0, 0, partWidth, partHeight)
            canvas.drawBitmap(contentViewBitmap, srcRect, destRect, null)
            val backView = createImageViewFromBitmap(partBitmap)
            var frontView: ImageView? = null
            if (i < viewHeights.size - 1) {
                frontView = if (i == 0) createImageViewFromBitmap(titleViewBitmap) else createBackSideView(viewHeights[i + 1])
            }
            frontView?.let { FoldingKitView(it, backView, context) }?.let { partsList.add(it) }
            yOffset += partHeight
        }

        return partsList
    }

    /**
     * Calculate heights for animation parts with some logic
     * TODO: Add detailed descriptions for logic
     *
     * @param titleViewHeight      height of title view
     * @param contentViewHeight    height of content view
     * @param additionalFlipsCount count of additional flips (after first one), set 0 for auto
     * @return list of calculated heights
     */
    private fun calculateHeightsForAnimationParts(titleViewHeight: Int, contentViewHeight: Int, additionalFlipsCount: Int): List<Int> {
        val partHeights = ArrayList<Int>()
        val additionalPartsTotalHeight = contentViewHeight - titleViewHeight * 2
        if (additionalPartsTotalHeight < 0)
            throw IllegalStateException("Content View height is too small")
        // add two main parts - guarantee first flip
        partHeights.add(titleViewHeight)
        partHeights.add(titleViewHeight)

        // if no space left - return
        if (additionalPartsTotalHeight == 0)
            return partHeights

        // if some space remained - use two different logic
        if (additionalFlipsCount != 0) {
            // 1 - additional parts count is specified and it is not 0 - divide remained space
            val additionalPartHeight = additionalPartsTotalHeight / additionalFlipsCount
            val remainingHeight = additionalPartsTotalHeight % additionalFlipsCount

            if (additionalPartHeight + remainingHeight > titleViewHeight)
                throw IllegalStateException("Additional flips count is too small")
            for (i in 0 until additionalFlipsCount)
                partHeights.add(additionalPartHeight + if (i == 0) remainingHeight else 0)
        } else {
            // 2 - additional parts count isn't specified or 0 - divide remained space to parts with title view size
            val partsCount = additionalPartsTotalHeight / titleViewHeight
            val restPartHeight = additionalPartsTotalHeight % titleViewHeight
            for (i in 0 until partsCount)
                partHeights.add(titleViewHeight)
            if (restPartHeight > 0)
                partHeights.add(restPartHeight)
        }

        return partHeights
    }

    /**
     * Create image view for display back side of flip view
     *
     * @param height height for view
     * @return ImageView with selected height and default background color
     */
    private fun createBackSideView(height: Int): ImageView {
        val imageView = ImageView(context)
        imageView.setBackgroundColor(mBackSideColor)
        imageView.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        return imageView
    }

    /**
     * Create image view for display selected bitmap
     *
     * @param bitmap bitmap to display in image view
     * @return ImageView with selected bitmap
     */
    private fun createImageViewFromBitmap(bitmap: Bitmap): ImageView {
        val imageView = ImageView(context)
        imageView.setImageBitmap(bitmap)
        imageView.layoutParams = RelativeLayout.LayoutParams(bitmap.width, bitmap.height)
        return imageView
    }

    /**
     * Create bitmap from specified View with specified with
     *
     * @param view        source for bitmap
     * @param parentWidth result bitmap width
     * @return bitmap from specified view
     */
    private fun measureViewAndGetBitmap(view: View, parentWidth: Int): Bitmap {
        val specW = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY)
        val specH = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(specW, specH)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.translate((-view.scrollX).toFloat(), (-view.scrollY).toFloat())
        view.draw(c)
        return b
    }

    /**
     * Create layout that will be a container for animation elements
     *
     * @return Configured container for animation elements (LinearLayout)
     */
    private fun createAndPrepareFoldingContainer(): LinearLayout {
        val foldingContainer = LinearLayout(context)
        foldingContainer.clipToPadding = false
        foldingContainer.clipChildren = false
        foldingContainer.orientation = LinearLayout.VERTICAL
        foldingContainer.layoutParams = LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        return foldingContainer
    }

    /**
     * Prepare and start height expand animation for FoldingKitLayout
     *
     * @param partAnimationDuration one part animate duration
     * @param viewHeights           heights of animation parts
     */
    private fun startExpandHeightAnimation(viewHeights: List<Int>, partAnimationDuration: Int) {
        if (viewHeights == null || viewHeights.isEmpty())
            throw IllegalArgumentException("ViewHeights array must have at least 2 elements")

        val heightAnimations = ArrayList<Animation>()
        var fromHeight = viewHeights[0]
        val delay = 0
        val animationDuration = partAnimationDuration - delay
        for (i in 1 until viewHeights.size) {
            val toHeight = fromHeight + viewHeights[i]
            val heightAnimation = HeightAnimation(this, fromHeight, toHeight, animationDuration)
                    .withInterpolator(DecelerateInterpolator())
            heightAnimation.startOffset = delay.toLong()
            heightAnimations.add(heightAnimation)
            fromHeight = toHeight
        }
        createAnimationChain(heightAnimations, this)
        this.startAnimation(heightAnimations[0])
    }

    /**
     * Prepare and start height collapse animation for FoldingKitLayout
     *
     * @param partAnimationDuration one part animate duration
     * @param viewHeights           heights of animation parts
     */
    private fun startCollapseHeightAnimation(viewHeights: List<Int>, partAnimationDuration: Int) {
        if (viewHeights == null || viewHeights.isEmpty())
            throw IllegalArgumentException("ViewHeights array must have at least 2 elements")

        val heightAnimations = ArrayList<Animation>()
        var fromHeight = viewHeights[0]
        for (i in 1 until viewHeights.size) {
            val toHeight = fromHeight + viewHeights[i]
            heightAnimations.add(HeightAnimation(this, toHeight, fromHeight, partAnimationDuration)
                    .withInterpolator(DecelerateInterpolator()))
            fromHeight = toHeight
        }

        Collections.reverse(heightAnimations)
        createAnimationChain(heightAnimations, this)
        this.startAnimation(heightAnimations[0])
    }

    /**
     * Create "animation chain" for selected view from list of animations objects
     *
     * @param animationList   collection with animations
     * @param animationObject view for animations
     */
    private fun createAnimationChain(animationList: List<Animation>, animationObject: View) {
        for (i in animationList.indices) {
            val animation = animationList[i]
            if (i + 1 < animationList.size) {
                animation.setAnimationListener(object : AnimationEndListener() {
                    override fun onAnimationEnd(animation: Animation?) {
                        animationObject.startAnimation(animationList[i + 1])
                    }
                })
            }
        }
    }

    /**
     * Start fold animation
     *
     * @param FoldingKitElements           ordered list with animation parts from top to bottom
     * @param foldingLayout                 prepared layout for animation parts
     * @param part90degreeAnimationDuration animation duration for 90 degree rotation
     * @param animationEndListener          animation end callback
     */
    private fun startFoldAnimation(FoldingKitElements: List<FoldingKitView>, foldingLayout: ViewGroup,
                                   part90degreeAnimationDuration: Int, animationEndListener: AnimationEndListener) {
        for (FoldingKitElement in FoldingKitElements)
            foldingLayout.addView(FoldingKitElement)

        Collections.reverse(FoldingKitElements)

        var nextDelay = 0
        for (i in FoldingKitElements.indices) {
            val cell = FoldingKitElements[i]
            cell.visibility = View.VISIBLE
            // not FIRST(BOTTOM) element - animate front view
            if (i != 0) {
                val foldAnimation = FoldAnimation(FoldAnimation.FoldAnimationMode.UNFOLD_UP, mCameraHeight, part90degreeAnimationDuration.toLong())
                        .withStartOffset(nextDelay)
                        .withInterpolator(DecelerateInterpolator())
                // if last(top) element - add end listener
                if (i == FoldingKitElements.size - 1) {
                    foldAnimation.setAnimationListener(animationEndListener)
                }
                cell.animateFrontView(foldAnimation)
                nextDelay += part90degreeAnimationDuration
            }
            // if not last(top) element - animate whole view
            if (i != FoldingKitElements.size - 1) {
                cell.startAnimation(FoldAnimation(FoldAnimation.FoldAnimationMode.FOLD_UP, mCameraHeight, part90degreeAnimationDuration.toLong())
                        .withStartOffset(nextDelay)
                        .withInterpolator(DecelerateInterpolator()))
                nextDelay += part90degreeAnimationDuration
            }
        }
    }

    /**
     * Start unfold animation
     *
     * @param FoldingKitElements           ordered list with animation parts from top to bottom
     * @param foldingLayout                 prepared layout for animation parts
     * @param part90degreeAnimationDuration animation duration for 90 degree rotation
     * @param animationEndListener          animation end callback
     */
    private fun startUnfoldAnimation(FoldingKitElements: List<FoldingKitView>, foldingLayout: ViewGroup,
                                     part90degreeAnimationDuration: Int, animationEndListener: AnimationEndListener) {
        var nextDelay = 0
        for (i in FoldingKitElements.indices) {
            val cell = FoldingKitElements[i]
            cell.visibility = View.VISIBLE
            foldingLayout.addView(cell)
            // if not first(top) element - animate whole view
            if (i != 0) {
                val foldAnimation = FoldAnimation(FoldAnimation.FoldAnimationMode.UNFOLD_DOWN, mCameraHeight, part90degreeAnimationDuration.toLong())
                        .withStartOffset(nextDelay)
                        .withInterpolator(DecelerateInterpolator())

                // if last(bottom) element - add end listener
                if (i == FoldingKitElements.size - 1) {
                    foldAnimation.setAnimationListener(animationEndListener)
                }

                nextDelay += part90degreeAnimationDuration
                cell.startAnimation(foldAnimation)

            }
            // not last(bottom) element - animate front view
            if (i != FoldingKitElements.size - 1) {
                cell.animateFrontView(FoldAnimation(FoldAnimation.FoldAnimationMode.FOLD_DOWN, mCameraHeight, part90degreeAnimationDuration.toLong())
                        .withStartOffset(nextDelay)
                        .withInterpolator(DecelerateInterpolator()))
                nextDelay += part90degreeAnimationDuration
            }
        }
    }

    /**
     * Initialize folding cell with parameters from attribute
     *
     * @param context context
     * @param attrs   attributes
     */
    private fun initializeFromAttributes(context: Context, attrs: AttributeSet) {
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.FoldingKit, 0, 0)
        try {
            this.mAnimationDuration = array.getInt(R.styleable.FoldingKit_animationDuration, DEF_ANIMATION_DURATION)
            this.mBackSideColor = array.getColor(R.styleable.FoldingKit_backSideColor, DEF_BACK_SIDE_COLOR)
            this.mAdditionalFlipsCount = array.getInt(R.styleable.FoldingKit_additionalFlipsCount, DEF_ADDITIONAL_FLIPS)
            this.mCameraHeight = array.getInt(R.styleable.FoldingKit_cameraHeight, DEF_CAMERA_HEIGHT)
        } finally {
            array.recycle()
        }
    }

    /**
     * Instantly change current state of cell to Folded without any animations
     */
    private fun setStateToFolded() {
        if (this.mAnimationInProgress || !this.isUnfolded) return
        // get basic views
        val contentView = getChildAt(0) ?: return
        val titleView = getChildAt(1) ?: return
        contentView.visibility = View.GONE
        titleView.visibility = View.VISIBLE
        this@FoldingKit.isUnfolded = false
        val layoutParams = this.layoutParams
        layoutParams.height = titleView.height
        this.layoutParams = layoutParams
        this.requestLayout()
    }
}
