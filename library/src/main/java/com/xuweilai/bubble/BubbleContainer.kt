package com.xuweilai.bubble

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Region
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.get
import com.xuweilai.bubble.internal.match_parent

internal class BubbleContainer(context: Context) : FrameLayout(context) {

  private val bubbleView by lazy { get(0) as BubbleView }

  var cancelable: Boolean = true

  init {
    layoutParams = LayoutParams(match_parent, match_parent)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (cancelable) {
      when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
          val region = getBubbleViewRegion(bubbleView)
          return if (region.contains(event.x.toInt(), event.y.toInt())) false else {
            bubbleView.dismiss()
            true
          }
        }
      }
    }
    return super.onTouchEvent(event)
  }

  private fun getBubbleViewRegion(bubbleView: BubbleView): Region {
    val translationX = bubbleView.translationX
    val translationY = bubbleView.translationY
    val width = bubbleView.width
    val height = bubbleView.height
    return Region(
      translationX.toInt(),
      translationY.toInt(),
      (translationX + width).toInt(),
      (translationY + height).toInt()
    )
  }

}