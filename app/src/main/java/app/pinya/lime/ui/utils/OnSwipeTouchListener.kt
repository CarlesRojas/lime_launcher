package app.pinya.lime.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs

private const val SWIPE_THRESHOLD: Int = 100
private const val SWIPE_VELOCITY_THRESHOLD: Int = 100

internal open class OnSwipeTouchListener(c: Context) :
    OnTouchListener {
    private val gestureDetector: GestureDetector

    private val context: Context

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        onAnyTouch()
        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {

                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) onFlingRight()
                        else onFlingLeft()
                    }
                } else {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY < 0) onFlingUp()
                        else onFlingDown()
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return false
        }
    }

    open fun onAnyTouch() {}
    open fun onFlingRight() {}
    open fun onFlingLeft() {}
    open fun onFlingUp() {}
    open fun onFlingDown() {}
    open fun onClick() {}
    open fun onDoubleClick() {}
    open fun onLongClick() {}

    init {
        context = c
        gestureDetector = GestureDetector(c, GestureListener())
    }
}
