package com.rexy.widgets.layout

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.STROKE
import android.graphics.Typeface.NORMAL
import android.os.Build
import android.os.Build.VERSION_CODES.JELLY_BEAN
import android.support.v4.util.Pair
import android.support.v4.util.Pools
import android.util.AttributeSet
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import java.util.*

class HierarchyLayout : WrapLayout {

    private var mLayoutBounds = Rect()
    private var mTempRect = Rect()
    private var mTempPointF = PointF()
    private var mSlop: Float = 0.toFloat()
    private var mDensity: Float = 0.toFloat()


    private val mOptionRect = RectF()
    private val mViewBounds = Rect()
    private val mViewBorderPaint = Paint(ANTI_ALIAS_FLAG)
    private val mCamera = Camera()
    private val mMatrix = Matrix()

    private val mIdNameArr = SparseArray<String>()

    private var mViewTextOffset = 1f
    private var mHierarchyViewEnable = true
    private var mHierarchyNodeEnable = true
    private val mHierarchySummaryEnable = true
    private val mDrawViewEnable = true

    private val mDrawViewIdEnable = false
    private var mPointerOne = INVALID_POINTER_ID
    private val mLastPointOne = PointF()
    private var mPointerTwo = INVALID_POINTER_ID
    private val mLastPointTwo = PointF()

    private val mPointDown = PointF()

    private var mMultiTouchTracking = TRACKING_UNKNOWN
    private var mRotationY = ROTATION_DEFAULT_Y.toFloat()
    private var mRotationX = ROTATION_DEFAULT_X.toFloat()
    private var mZoom = ZOOM_DEFAULT

    private var mSpacing = SPACING_DEFAULT.toFloat()
    private val mViewColor = 0xFF888888.toInt()


    private val mViewShadowColor = 0xFF000000.toInt()
    private val mHierarchyColor = 0xAA000000.toInt()

    private val mNodeLeafStrokeColor = 0xFFFFFFFF.toInt()
    private val mTreeNodeColor = 0xFF00FF00.toInt()
    private val mTreeLeafColor = 0xFFFF0000.toInt()
    private val mTreeBranchColor = 0xFFFFFFFF.toInt()

    private val mTreeBackground = 0
    private val mTreeTextSize = 4
    private val mTreeTextColor = 0xFFFF0000.toInt()
    private val mTreeSumTextSize = 15

    private val mTreeSumTextColor = 0xFFAA2A20.toInt()
    private val mMaxTreeLeafSize = -1

    private val mTreeWidthWeight = 0.95f
    private val mTreeHeightWeight = 0.85f
    private val mTreeLeafMarginWeight = 1f
    private val mTreeLevelMarginWeight = 3.5f
    private var mTreeOffsetX = 0f
    private var mTreeOffsetY = 10f

    private var mLeafSize: Float = 0.toFloat()
    private var mLeafMargin: Float = 0.toFloat()
    private var mLevelMargin: Float = 0.toFloat()
    private var mHierarchyTreeHorizontal: Boolean = false
    private var mTree: ViewHierarchyTree? = null
    private var mStringBuilder = StringBuilder()
    private var mTreePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mDensity = context.resources.displayMetrics.density
        mSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        mTreePaint.style = Paint.Style.FILL
        mTreePaint.textAlign = Paint.Align.CENTER
        mTreeOffsetX *= mDensity
        mTreeOffsetY *= mDensity

        mViewTextOffset *= mDensity
        mViewBorderPaint.style = STROKE
        mViewBorderPaint.textSize = 6 * mDensity
        if (Build.VERSION.SDK_INT >= JELLY_BEAN) {
            mViewBorderPaint.typeface = Typeface.create("sans-serif-condensed", NORMAL)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (mHierarchyViewEnable) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                requestDisallowInterceptTouchEvent(true)
            }
        } else {
            val action = ev.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                mPointDown.set(ev.x, ev.y)
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                handleClickUp(ev.x, ev.y)
            }
        }
        return mHierarchyViewEnable || super.onInterceptTouchEvent(ev)
    }

    private fun handleClickUp(endX: Float, endY: Float) {
        val x = mPointDown.x
        val y = mPointDown.y
        if (Math.abs(x - endX) < mSlop && Math.abs(y - endY) < mSlop) {
            if (x >= mOptionRect.left - mSlop && x <= mOptionRect.right + mSlop && y >= mOptionRect.top - mSlop && y <= mOptionRect.bottom + mSlop) {
                if (x > mOptionRect.centerX()) {
                    mHierarchyViewEnable = !mHierarchyViewEnable
                } else {
                    mHierarchyNodeEnable = !mHierarchyNodeEnable
                }
                invalidate()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (!mHierarchyViewEnable) {
            if (action == MotionEvent.ACTION_DOWN) {
                mPointDown.set(event.x, event.y)
                if (mPointDown.x >= mOptionRect.left - mSlop && mPointDown.x <= mOptionRect.right + mSlop && mPointDown.y >= mOptionRect.top - mSlop && mPointDown.y <= mOptionRect.bottom + mSlop) {
                    return true
                }
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                handleClickUp(event.x, event.y)
            }
            return super.onTouchEvent(event)
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mPointDown.set(event.x, event.y)
                val index = if (action == ACTION_DOWN) 0 else event.actionIndex
                if (mPointerOne == INVALID_POINTER_ID) {
                    mPointerOne = event.getPointerId(index)
                    mLastPointOne.set(event.getX(index), event.getY(index))
                } else if (mPointerTwo == INVALID_POINTER_ID) {
                    mPointerTwo = event.getPointerId(index)
                    mLastPointTwo.set(event.getX(index), event.getY(index))
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = if (action == ACTION_DOWN) 0 else event.actionIndex
                if (mPointerOne == INVALID_POINTER_ID) {
                    mPointerOne = event.getPointerId(index)
                    mLastPointOne.set(event.getX(index), event.getY(index))
                } else if (mPointerTwo == INVALID_POINTER_ID) {
                    mPointerTwo = event.getPointerId(index)
                    mLastPointTwo.set(event.getX(index), event.getY(index))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mPointerTwo == INVALID_POINTER_ID) {
                    // Single pointer controlling 3D rotation.
                    var i = 0
                    val count = event.pointerCount
                    while (i < count) {
                        if (mPointerOne == event.getPointerId(i)) {
                            val eventX = event.getX(i)
                            val eventY = event.getY(i)
                            val drx = 90 * ((eventX - mLastPointOne.x) / width)
                            val dry = 90 * (-(eventY - mLastPointOne.y) / height) // Invert Y-axis.
                            // An 'x' delta affects 'y' rotation and vise versa.
                            if (drx != 0f || dry != 0f) {
                                mRotationY = Math.min(Math.max(mRotationY + drx, ROTATION_MIN.toFloat()), ROTATION_MAX.toFloat())
                                mRotationX = Math.min(Math.max(mRotationX + dry, ROTATION_MIN.toFloat()), ROTATION_MAX.toFloat())
                                mLastPointOne.set(eventX, eventY)
                                invalidate()
                            }
                        }
                        i++
                    }
                } else {
                    val pointerOneIndex = event.findPointerIndex(mPointerOne)
                    val pointerTwoIndex = event.findPointerIndex(mPointerTwo)
                    val xOne = event.getX(pointerOneIndex)
                    val yOne = event.getY(pointerOneIndex)
                    val xTwo = event.getX(pointerTwoIndex)
                    val yTwo = event.getY(pointerTwoIndex)
                    val dxOne = xOne - mLastPointOne.x
                    val dyOne = yOne - mLastPointOne.y
                    val dxTwo = xTwo - mLastPointTwo.x
                    val dyTwo = yTwo - mLastPointTwo.y
                    if (mMultiTouchTracking == TRACKING_UNKNOWN) {
                        val adx = Math.abs(dxOne) + Math.abs(dxTwo)
                        val ady = Math.abs(dyOne) + Math.abs(dyTwo)
                        if (adx > mSlop * 2 || ady > mSlop * 2) {
                            if (adx > ady) {
                                // Left/right movement wins. Track horizontal.
                                mMultiTouchTracking = TRACKING_HORIZONTALLY
                            } else {
                                // Up/down movement wins. Track vertical.
                                mMultiTouchTracking = TRACKING_VERTICALLY
                            }
                        }
                    }
                    if (mMultiTouchTracking != TRACKING_UNKNOWN) {
                        if (dyOne != dyTwo) {
                            if (mMultiTouchTracking == TRACKING_VERTICALLY) {
                                if (yOne >= yTwo) {
                                    mZoom += dyOne / height - dyTwo / height
                                } else {
                                    mZoom += dyTwo / height - dyOne / height
                                }
                                mZoom = Math.min(Math.max(mZoom, ZOOM_MIN), ZOOM_MAX)

                            }
                            if (mMultiTouchTracking == TRACKING_HORIZONTALLY) {
                                if (xOne >= xTwo) {
                                    mSpacing += dxOne / width * SPACING_MAX - dxTwo / width * SPACING_MAX
                                } else {
                                    mSpacing += dxTwo / width * SPACING_MAX - dxOne / width * SPACING_MAX
                                }
                                mSpacing = Math.min(Math.max(mSpacing, SPACING_MIN.toFloat()), SPACING_MAX.toFloat())
                            }
                            invalidate()
                        }
                        mLastPointOne.set(xOne, yOne)
                        mLastPointTwo.set(xTwo, yTwo)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                handleClickUp(event.x, event.y)
                val index = if (action != ACTION_POINTER_UP) 0 else event.actionIndex
                val pointerId = event.getPointerId(index)
                if (mPointerOne == pointerId) {
                    // Shift pointer two (real or invalid) up to pointer one.
                    mPointerOne = mPointerTwo
                    mLastPointOne.set(mLastPointTwo)
                    // Clear pointer two and tracking.
                    mPointerTwo = INVALID_POINTER_ID
                    mMultiTouchTracking = TRACKING_UNKNOWN
                } else if (mPointerTwo == pointerId) {
                    mPointerTwo = INVALID_POINTER_ID
                    mMultiTouchTracking = TRACKING_UNKNOWN
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val index = if (action != ACTION_POINTER_UP) 0 else event.actionIndex
                val pointerId = event.getPointerId(index)
                if (mPointerOne == pointerId) {
                    mPointerOne = mPointerTwo
                    mLastPointOne.set(mLastPointTwo)
                    mPointerTwo = INVALID_POINTER_ID
                    mMultiTouchTracking = TRACKING_UNKNOWN
                } else if (mPointerTwo == pointerId) {
                    mPointerTwo = INVALID_POINTER_ID
                    mMultiTouchTracking = TRACKING_UNKNOWN
                }
            }
        }
        return true
    }

    override fun doAfterMeasure(measuredWidth: Int, measuredHeight: Int, contentWidth: Int, contentHeight: Int) {
        super.doAfterMeasure(measuredWidth, measuredHeight, contentWidth, contentHeight)
        mLayoutBounds.set(0, 0, measuredWidth, measuredHeight)
        mOptionRect.setEmpty()
    }

    override fun doAfterLayout(firstAttachLayout: Boolean) {
        super.doAfterLayout(firstAttachLayout)
        mTree?.destroy()
        mLeafSize = -1f
        mTree = ViewHierarchyTree.create(this)
        val width = mLayoutBounds.width()
        val height = mLayoutBounds.height()
        mHierarchyTreeHorizontal = width > height
        val longSize = Math.max(width, height)
        val shortSize = Math.min(width, height)
        calculateHierarchyLayoutRadius(longSize * mTreeWidthWeight, shortSize * mTreeHeightWeight, mHierarchyTreeHorizontal)
    }

    override fun doAfterDraw(canvas: Canvas, inset: Rect) {
        super.doAfterDraw(canvas, inset)
        if (mTree != null && mLeafSize > 0) {
            if (mHierarchyNodeEnable || mHierarchyViewEnable) {
                if (mHierarchyColor != 0) {
                    canvas.drawColor(mHierarchyColor)
                }
                if (mHierarchyViewEnable) {
                    drawHierarchyView(canvas)
                }
                if (mHierarchyNodeEnable) {
                    drawHierarchyTree(canvas)
                }
            }
            mTreePaint.color = 0xAA00FF00.toInt()
            canvas.drawRect(0f, mOptionRect.top - mDensity * 3, mLayoutBounds.right.toFloat(), mOptionRect.bottom + mDensity * 3, mTreePaint)
            mTreePaint.color = mTreeSumTextColor
            mTreePaint.textSize = mTreeSumTextSize * mDensity
            mTreePaint.textAlign = Paint.Align.LEFT
            if (mHierarchySummaryEnable && mTreeSumTextColor != 0 && mTreeSumTextSize > 0) {
                val treeBounds = mTree!!.getTag<RectF>()
                drawTreeSummaryInfo(canvas, treeBounds, mHierarchyTreeHorizontal)
            }
            drawOptionBar(canvas)
        }
    }

    private fun drawHierarchyView(canvas: Canvas) {
        val applyChangeVisible = mHierarchyColor != 0 && 255 == mHierarchyColor.ushr(24)
        var location = mTree!!.windowLocation
        val saveCount = canvas.save()
        val x = location.left.toFloat()
        val y = location.top.toFloat()
        val translateShowX = mSpacing * mDensity * mRotationY / ROTATION_MAX
        val translateShowY = mSpacing * mDensity * mRotationX / ROTATION_MAX
        val cx = width / 2f
        val cy = height / 2f
        mCamera.save()
        mCamera.rotate(mRotationX, mRotationY, 0f)
        mCamera.getMatrix(mMatrix)
        mCamera.restore()
        mMatrix.preTranslate(-cx, -cy)
        mMatrix.postTranslate(cx, cy)
        canvas.concat(mMatrix)
        canvas.scale(mZoom, mZoom, cx, cy)
        val nodes = mTree!!.hierarchyNodeArray
        mViewBorderPaint.color = mViewColor
        mViewBorderPaint.setShadowLayer(0f, 1f, -1f, mViewShadowColor)
        for (i in 1 until nodes.size()) {
            val node = nodes.get(i)
            val view = node.view
            val layer = node.level
            val viewSaveCount = canvas.save()
            val tx = layer * translateShowX
            val ty = layer * translateShowY
            location = node.windowLocation
            canvas.translate(tx, -ty)
            canvas.translate(location.left - x, location.top - y)
            mViewBounds.set(0, 0, view!!.width, view.height)
            canvas.drawRect(mViewBounds, mViewBorderPaint)
            if (mDrawViewEnable) {
                if (applyChangeVisible) {
                    changeChildVisible(view, true)
                    view.draw(canvas)
                    changeChildVisible(view, false)
                } else {
                    val viewGroupType = view is ViewGroup
                    if (viewGroupType) {
                        if (view.background != null) {
                            view.background.draw(canvas)
                        }
                    } else {
                        view.draw(canvas)
                    }
                }
            }
            if (mDrawViewIdEnable) {
                val id = view.id
                if (id != View.NO_ID) {
                    canvas.drawText(nameForId(id), mViewTextOffset, mViewBorderPaint.textSize, mViewBorderPaint)
                }
            }
            canvas.restoreToCount(viewSaveCount)
        }
        canvas.restoreToCount(saveCount)
    }

    private fun drawOptionBar(canvas: Canvas) {
        val textHeight = mTreePaint.descent() - mTreePaint.ascent()
        if (mOptionRect.isEmpty) {
            mOptionRect.set(mLayoutBounds)
            mOptionRect.top = mOptionRect.bottom - textHeight
            mOptionRect.left = mOptionRect.right - textHeight * 5
            mOptionRect.offset(-mDensity * 3, -textHeight)
            postInvalidate()
        }
        val midX = mOptionRect.centerX()
        val d = mTreePaint.descent()
        val a = mTreePaint.ascent()
        val baseY = mOptionRect.centerY() + (d - a) / 2 - d
        canvas.drawLine(midX, mOptionRect.top, midX, mOptionRect.bottom, mViewBorderPaint)
        canvas.drawText("NODE", mOptionRect.left, baseY, mTreePaint)
        canvas.drawText("VIEW", mOptionRect.right - calculateTextBounds("VIEW", mTreePaint, mTempRect).width().toFloat() - mDensity, baseY, mTreePaint)

        mTreePaint.color = if (mHierarchyNodeEnable) 0x880000FF.toInt() else 0x220000FF
        canvas.drawRect(mOptionRect.left, mOptionRect.top, midX - mDensity, mOptionRect.bottom, mTreePaint)

        mTreePaint.color = if (mHierarchyViewEnable) 0x880000FF.toInt() else 0x220000FF
        canvas.drawRect(midX + mDensity, mOptionRect.top, mOptionRect.right, mOptionRect.bottom, mTreePaint)
    }

    private fun drawTreeSummaryInfo(canvas: Canvas, treeBounds: RectF, horizontal: Boolean) {
        mStringBuilder.delete(0, mStringBuilder.length)
        mStringBuilder.append("层级(").append(mTree!!.hierarchyCount).append(',').append(String.format("%.1f", mTree!!.argHierarchyCount)).append(")").append(',')
        mStringBuilder.append("结点(").append(mTree!!.countOfNode).append(',').append(mTree!!.countOfViewGroup).append(',').append(mTree!!.countOfView).append(")").append(',')
        mStringBuilder.append("测绘(").append(mLastMeasureCost).append(',').append(mLastLayoutCost).append(',').append(mLastDrawCost).append(")")
        val textHeight = mTreePaint.descent() - mTreePaint.ascent()
        val d = mTreePaint.descent()
        val a = mTreePaint.ascent()
        canvas.drawText(mStringBuilder.toString(), textHeight / 2, mOptionRect.centerY() + (d - a) / 2 - d, mTreePaint)
    }

    protected fun drawTreeNode(canvas: Canvas, info: ViewHierarchyInfo, radius: Float) {
        val tempPoint = mTempPointF
        val parent = info.parent
        getNodePosition(info.getTag<Any>() as RectF, tempPoint, radius)
        val x = tempPoint.x
        val y = tempPoint.y
        if (parent != null && mTreeBranchColor != 0) {
            getNodePosition(parent.getTag<Any>() as RectF, tempPoint, radius)
            val px = tempPoint.x
            val py = tempPoint.y
            mTreePaint.color = mTreeBranchColor
            canvas.drawLine(x, y, px, py, mTreePaint)
        }
        mTreePaint.color = mNodeLeafStrokeColor
        canvas.drawCircle(x, y, radius + mDensity, mTreePaint)

        mTreePaint.color = if (info.isLeaf) mTreeLeafColor else mTreeNodeColor
        canvas.drawCircle(x, y, radius, mTreePaint)

        if (mTreeTextColor != 0) {
            val sb = StringBuilder()
            sb.append(info.markName).append('[').append(info.level).append(',').append(info.levelIndex).append(']')
            canvas.drawText(sb.toString(), x, y - radius - radius, mTreePaint)
        }
    }

    protected fun drawHierarchyTree(canvas: Canvas) {
        if (mLeafSize > 0) {
            mTreePaint.textAlign = Paint.Align.CENTER
            val treeBounds = mTree!!.getTag<RectF>()
            if (mTreeBackground != 0) {
                mTreePaint.color = mTreeBackground
                canvas.drawRect(treeBounds, mTreePaint)
            }
            if (mTreeTextColor != 0) {
                mTreePaint.textSize = mTreeTextSize * mDensity
            }
            val radius = mLeafSize / 2f
            val infoArr = mTree!!.hierarchyNodeArray
            val size = infoArr.size()
            for (i in size - 1 downTo 0) {
                drawTreeNode(canvas, infoArr.get(i), radius)
            }
        }
    }

    private val mTempInt = SparseArray<View>()

    private fun changeChildVisible(view: View, hide: Boolean) {
        if (view is ViewGroup) {
            var size = view.childCount
            if (size > 0) {
                if (hide) {
                    mTempInt.clear()
                    for (i in 0..size - 1) {
                        val child = view.getChildAt(i)
                        if (child.visibility == View.VISIBLE) {
                            mTempInt.put(i, child)
                            child.visibility = View.INVISIBLE
                        }
                    }
                } else {
                    size = mTempInt.size()
                    for (i in 0 until size) {
                        mTempInt.valueAt(i).setVisibility(View.VISIBLE)
                    }
                    mTempInt.clear()
                }
            }
        }
    }

    private fun calculateTextBounds(text: String?, paint: Paint, result: Rect): Rect {
        result.setEmpty()
        if (text != null) {
            paint.getTextBounds(text, 0, text.length, result)
        }
        return result
    }

    private fun calculateHierarchyLayoutRadius(longSize: Float, shortSize: Float, horizontal: Boolean) {
        val leafCount = mTree!!.leafCount
        val levelCount = mTree!!.hierarchyCount
        if (leafCount > 0 && levelCount > 0) {
            //leafCount*leafSize+(leafCount-1)*(leafSize*mLeafMarginHorizontalFactor)=w;
            mLeafSize = longSize / (leafCount + (leafCount - 1) * mTreeLeafMarginWeight)
            if (mMaxTreeLeafSize > 0 && mLeafSize > mMaxTreeLeafSize) {
                mLeafSize = mMaxTreeLeafSize.toFloat()
            }
            mLeafMargin = mTreeLeafMarginWeight * mLeafSize
            mLevelMargin = mTreeLevelMarginWeight * mLeafSize
            //leafLevel*leafSize+(leafCount-1)*maxMarginVertical=h;
            if (levelCount > 0) {
                mLevelMargin = Math.min(mLevelMargin, (shortSize - levelCount * mLeafSize) / (levelCount - 1))
            }
            val hierarchyWidth = leafCount * mLeafSize + (leafCount - 1) * mLeafMargin
            val hierarchyHeight = levelCount * mLeafSize + (levelCount - 1) * mLevelMargin
            calculateHierarchyLayoutPosition(mLayoutBounds, hierarchyWidth, hierarchyHeight, horizontal)
        }
    }

    private fun calculateHierarchyLayoutPosition(canvasBounds: Rect, hierarchyWidth: Float, hierarchyHeight: Float, horizontal: Boolean) {
        val rootBounds = RectF()
        if (horizontal) {
            rootBounds.left = (canvasBounds.left + (1 - mTreeWidthWeight) * canvasBounds.width() / 2).toInt().toFloat()
            rootBounds.top = (canvasBounds.top + (1 - mTreeHeightWeight) * canvasBounds.height() / 2).toInt().toFloat()
            rootBounds.right = rootBounds.left + hierarchyWidth
            rootBounds.bottom = rootBounds.top + hierarchyHeight
        } else {
            rootBounds.left = (canvasBounds.left + (1 - mTreeHeightWeight) * canvasBounds.width() / 2).toInt().toFloat()
            rootBounds.top = (canvasBounds.top + (1 - mTreeWidthWeight) * canvasBounds.height() / 2).toInt().toFloat()
            rootBounds.right = rootBounds.left + hierarchyHeight
            rootBounds.bottom = rootBounds.top + hierarchyWidth
        }
        rootBounds.offset(mTreeOffsetX, mTreeOffsetY)
        val lines = mTree!!.hierarchyCountArray
        val list = mTree!!.hierarchyNodeArray

        val lineCount = lines.size()
        var startIndex: Int
        var endIndex = 0
        val levelMargin = mLevelMargin + mLeafSize
        var usedWeight = 0f
        list.get(endIndex++).setTag(rootBounds)
        var prevParent: ViewHierarchyInfo? = null
        var parent: ViewHierarchyInfo?
        var child: ViewHierarchyInfo
        for (line in 1 until lineCount) {
            startIndex = endIndex
            endIndex = startIndex + lines.get(line)
            for (i in startIndex until endIndex) {
                child = list.get(i)
                parent = child.parent
                if (parent !== prevParent) {
                    usedWeight = 0f
                    prevParent = parent
                }
                usedWeight += buildAndSetHierarchyBounds(child, parent, usedWeight, levelMargin, horizontal).toFloat()
            }
        }
        invalidate()
    }

    private fun buildAndSetHierarchyBounds(child: ViewHierarchyInfo, parent: ViewHierarchyInfo?, usedWeight: Float, levelMargin: Float, horizontal: Boolean): Int {
        val bounds = RectF(parent!!.getTag<Any>() as RectF)
        val currentWeight = child.leafCount
        val weightSum = parent.leafCount.toFloat()
        val weightEnd = usedWeight + currentWeight
        val start: Float
        val end: Float
        val size: Float
        if (horizontal) {
            size = bounds.width()
            start = bounds.left + size * usedWeight / weightSum
            end = bounds.left + size * weightEnd / weightSum
            bounds.left = start
            bounds.right = end
            bounds.top = bounds.top + levelMargin
        } else {
            size = bounds.height()
            start = bounds.bottom - size * usedWeight / weightSum
            end = bounds.bottom - size * weightEnd / weightSum
            bounds.top = end
            bounds.bottom = start
            bounds.left = bounds.left + levelMargin
        }
        child.setTag(bounds)
        return currentWeight
    }

    private fun getNodePosition(rect: RectF, point: PointF, radius: Float) {
        if (mHierarchyTreeHorizontal) {
            point.set(rect.centerX(), rect.top + radius)
        } else {
            point.set(rect.left + radius, rect.centerY())
        }
    }

    private fun nameForId(id: Int): String? {
        var name: String? = mIdNameArr.get(id)
        if (name == null) {
            try {
                name = resources.getResourceEntryName(id)
            } catch (e: Resources.NotFoundException) {
                name = String.format("0x%8x", id)
            }

            mIdNameArr.put(id, name)
        }
        return name
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mTree != null) {
            mTree?.destroy()
            mTree = null
        }
        mIdNameArr.clear()
    }

    open class ViewHierarchyInfo protected constructor(view: View, index: Int, levelIndex: Int, parent: ViewHierarchyInfo?) {
        var level = -1
            private set
        var levelIndex = -1
            private set
        var layoutIndex = -1
            private set
        var leafCount = 0
            private set
        var view: View? = null
            private set
        private var mTag: Any? = null
        var name: String? = null
            private set
        val windowLocation = Rect()
        var parent: ViewHierarchyInfo? = null
            private set
        var mChildArr: LinkedList<ViewHierarchyInfo>? = null

        init {
            analyzeHierarchyInfo(view, index, levelIndex, parent)
        }

        private fun analyzeHierarchyInfo(view: View, index: Int, levelIndex: Int, parent: ViewHierarchyInfo?) {
            this.view = view
            this.name = view.javaClass.name
            windowLocation.set(0, 0, this.view!!.measuredWidth, this.view!!.measuredHeight)
            this.view!!.getLocationInWindow(LOCATION)
            windowLocation.offset(LOCATION[0], LOCATION[1])
            if (parent == null) {
                this.level = 0
            } else {
                this.level = parent.level + 1
                this.parent = parent
                parent.mChildArr!!.add(this)
            }
            this.layoutIndex = index
            this.levelIndex = levelIndex
            if (view is ViewGroup) {
                mChildArr = LinkedList()
                if (index == -1) {
                    if (view.getParent() is ViewGroup) {
                        this.layoutIndex = (view.getParent() as ViewGroup).indexOfChild(view)
                    } else {
                        this.layoutIndex = 0
                    }
                }
            }
            computeWeightIfNeed(view, parent)
        }

        private fun computeWeightIfNeed(view: View, parent: ViewHierarchyInfo?) {
            var parent = parent
            var calculateWeight = view !is ViewGroup
            if (!calculateWeight) {
                val p = view as ViewGroup
                val count = p.childCount
                calculateWeight = true
                for (i in 0 until count) {
                    if (View.GONE != p.getChildAt(i).visibility) {
                        calculateWeight = false
                        break
                    }
                }
            }
            if (calculateWeight) {
                leafCount = 1
                while (parent != null) {
                    parent.leafCount = parent.leafCount + 1
                    parent = parent.parent
                }
            }
        }

        fun setTag(tag: Any) {
            this.mTag = tag
        }

        fun <CAST : Any> getTag(): CAST {
            return mTag as CAST
        }

        val isLeaf: Boolean
            get() = mChildArr == null

        val isRoot: Boolean
            get() = parent == null

        val markName: String?
            get() {
                val name = simpleName
                if (name != null && name.length > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until name.length) {
                        val c = name[i]
                        if (c >= 'A' && c <= 'Z') {
                            sb.append(c)
                        }
                    }
                    if (sb.length == 0) {
                        sb.append(name)
                    }
                    return sb.toString()
                }
                return name
            }

        val simpleName: String?
            get() {
                var result = name
                var point = if (result == null) -1 else 0
                if (point == 0) {
                    point = result!!.lastIndexOf('$')
                    if (point == -1) {
                        point = result.lastIndexOf('.')
                    }
                }
                if (point > 0 && point < result!!.length) {
                    result = result.substring(point + 1)
                }
                return result
            }

        val parentIndex: Int
            get() {
                var result = layoutIndex
                if (parent != null && parent!!.mChildArr != null) {
                    result = parent!!.mChildArr!!.indexOf(this)
                }
                return result
            }

        val childArr: List<ViewHierarchyInfo>?
            get() = mChildArr

        val childCount: Int
            get() = if (mChildArr != null) {
                mChildArr!!.size
            } else 0

        private fun recycle() {
            destroy(true)
            level = -1
            levelIndex = -1
            layoutIndex = -1
            leafCount = 0
            mPool.release(this)
        }

        protected fun destroy(recycle: Boolean) {
            view = null
            name = null
            windowLocation.setEmpty()
            mTag = null
            parent = null
            if (mChildArr != null) {
                val its = mChildArr!!.iterator()
                while (its.hasNext()) {
                    if (recycle) {
                        its.next().recycle()
                    } else {
                        its.next().destroy(recycle)
                    }
                    its.remove()
                }
                mChildArr = null
            }
        }

        override fun toString(): String {
            return toString(true)
        }

        fun toString(fullInfo: Boolean): String {
            val sb = StringBuilder()
            if (isRoot) {
                sb.append("[root ")
            } else if (isLeaf) {
                sb.append("[leaf ")
            } else {
                sb.append("[node ")
            }
            sb.append(level).append(',').append(levelIndex).append(']')
            sb.append(' ').append(if (fullInfo) simpleName else markName).append('{')
            sb.append("index=").append(parentIndex).append(',')
            sb.append("location=").append(windowLocation).append(',')
            sb.append("count=").append(childCount).append(',')
            sb.append("leaf=").append(leafCount)
            return sb.append('}').toString()
        }

        companion object {
            private val MAX_POOL_SIZE = 500
            private val mPool = Pools.SimplePool<ViewHierarchyInfo>(MAX_POOL_SIZE)
            private val LOCATION = intArrayOf(0, 0)

            fun obtain(view: View, index: Int, levelIndex: Int, parent: ViewHierarchyInfo?): ViewHierarchyInfo {
                var data: ViewHierarchyInfo? = mPool.acquire()
                if (data == null) {
                    data = ViewHierarchyInfo(view, index, levelIndex, parent)
                } else {
                    data.analyzeHierarchyInfo(view, index, levelIndex, parent)
                }
                return data
            }
        }
    }

    internal class ViewHierarchyTree private constructor(root: View, index: Int, levelIndex: Int, parent: ViewHierarchyInfo?) : ViewHierarchyInfo(root, index, levelIndex, parent) {
        val hierarchyCountArray = SparseIntArray()
        val hierarchyNodeArray: SparseArray<ViewHierarchyInfo> = SparseArray()

        init {
            hierarchyCountArray.put(level, 1)
            hierarchyNodeArray.put(0, this)
            if (childArr != null) {
                analyzeViewHierarchy(Pair.create(this, root as ViewGroup))
            }
        }

        private fun analyzeViewHierarchy(root: Pair<out ViewHierarchyInfo, ViewGroup>) {
            val queue = LinkedList<Pair<out ViewHierarchyInfo, ViewGroup>>()
            queue.offer(root)
            var arrayIndex = hierarchyNodeArray.size()
            var levelIndex = 0
            var level = 0
            var pair = queue.poll()
            while (pair != null) {
                val parent = pair.first
                val layout = pair.second
                val size = layout.childCount
                for (i in 0 until size) {
                    val child = layout.getChildAt(i)
                    if (child.visibility != View.GONE) {
                        val curLevel = if (parent == null) 0 else parent.level + 1
                        if (curLevel != level) {
                            level = curLevel
                            levelIndex = 0
                        }
                        val node = ViewHierarchyInfo.obtain(child, i, levelIndex++, parent)
                        hierarchyCountArray.put(node.level, hierarchyCountArray.get(curLevel, 0) + 1)
                        hierarchyNodeArray.put(arrayIndex++, node)
                        if (node.mChildArr != null) {
                            queue.offer(Pair.create(node, child as ViewGroup))
                        }
                    }
                }
                pair = queue.poll()
            }
        }

        fun getViewHierarchyInfo(level: Int, levelIndex: Int): ViewHierarchyInfo? {
            var sum = levelIndex
            for (i in 0 until level) {
                sum += hierarchyCountArray.get(i, 0)
            }
            return if (sum >= 0 && sum < hierarchyNodeArray.size()) {
                hierarchyNodeArray.get(sum)
            } else null
        }

        val countOfViewGroup: Int
            get() = countOfNode - countOfView

        val countOfView: Int
            get() = leafCount

        val countOfNode: Int
            get() = hierarchyNodeArray.size()

        fun getCountOfNode(level: Int): Int {
            return hierarchyCountArray.get(level)
        }

        fun getCountOfView(level: Int): Int {
            var start = 0
            var leafCount = 0
            while (start < level) {
                start += hierarchyCountArray.get(start)
            }
            val end = start + getCountOfNode(level)
            while (start < end) {
                if (hierarchyNodeArray.get(start).isLeaf) {
                    leafCount++
                }
                start++
            }
            return leafCount
        }

        fun getCountOfViewGroup(level: Int): Int {
            return getCountOfNode(level) - getCountOfView(level)
        }

        val hierarchyCount: Int
            get() = hierarchyCountArray.size()

        val argHierarchyCount: Float
            get() {
                val sum = countOfNode.toFloat()
                var result = 0f
                val hierarchyCount = hierarchyCount
                for (i in 0 until hierarchyCount) {
                    result += getCountOfNode(i) * (i + 1) / sum
                }
                return result
            }

        fun dumpNodeWeight(sb: StringBuilder?): StringBuilder {
            var sb = sb
            sb = if (sb == null) StringBuilder() else sb
            val size = hierarchyCountArray.size()
            var weight = 0
            for (i in 0 until size) {
                val value = hierarchyCountArray.get(i)
                weight += value * (i + 1)
                sb.append(" | ").append(value)
            }
            sb.insert(0, weight)
            return sb
        }

        fun destroy() {
            super.destroy(true)
            hierarchyCountArray.clear()
            hierarchyNodeArray.clear()
        }

        companion object {

            fun create(root: View): ViewHierarchyTree {
                return ViewHierarchyTree(root, -1, 0, null)
            }

            protected fun create(root: View, index: Int, levelIndex: Int, parent: ViewHierarchyInfo): ViewHierarchyTree {
                return ViewHierarchyTree(root, index, levelIndex, parent)
            }
        }
    }

    companion object {
        private val TRACKING_UNKNOWN = 0
        private val TRACKING_VERTICALLY = 1
        private val TRACKING_HORIZONTALLY = -1
        private val ROTATION_MAX = 55
        private val ROTATION_MIN = -ROTATION_MAX
        private val ROTATION_DEFAULT_X = 6
        private val ROTATION_DEFAULT_Y = -12
        private val ZOOM_DEFAULT = 0.75f
        private val ZOOM_MIN = 0.5f
        private val ZOOM_MAX = 1.5f
        private val SPACING_DEFAULT = 25
        private val SPACING_MIN = 10
        private val SPACING_MAX = 100


        fun isHierarchyInstalled(activity: Activity): Boolean {
            val window = activity.window
            val decorView = window.decorView as ViewGroup
            val parent = decorView.findViewById(android.R.id.content) as ViewGroup
            return parent is HierarchyLayout
        }

        fun hierarchy(activity: Activity, install: Boolean) {
            val window = activity.window
            val decorView = window.decorView as ViewGroup
            val parent = decorView.findViewById(android.R.id.content) as ViewGroup
            if (install) {
                if (parent !is HierarchyLayout) {
                    val childs = SparseArray<View>()
                    val count = parent.childCount
                    for (i in 0 until count) {
                        childs.put(i, parent.getChildAt(i))
                    }
                    parent.removeAllViews()
                    parent.id = View.NO_ID
                    val hierarchy = HierarchyLayout(activity)
                    hierarchy.id = android.R.id.content
                    for (i in 0..count - 1) {
                        hierarchy.addView(childs.get(i))
                    }
                    parent.addView(hierarchy)
                }
            } else {
                if (parent is HierarchyLayout) {
                    val childs = SparseArray<View>()
                    val count = parent.getChildCount()
                    for (i in 0 until count) {
                        childs.put(i, parent.getChildAt(i))
                    }
                    parent.removeAllViews()
                    parent.setId(View.NO_ID)
                    val realParent = parent.getParent() as ViewGroup
                    realParent.id = android.R.id.content
                    realParent.removeAllViews()
                    for (i in 0 until count) {
                        realParent.addView(childs.get(i))
                    }
                }
            }
        }
    }
}