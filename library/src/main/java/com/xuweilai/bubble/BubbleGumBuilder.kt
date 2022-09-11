package com.xuweilai.bubble

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.plusAssign
import com.xuweilai.bubble.internal.*
import com.xuweilai.bubble.internal.annotation.Dp
import com.xuweilai.bubble.internal.annotation.MillSecond
import com.xuweilai.bubble.internal.annotation.Sp
import com.xuweilai.bubble.internal.atTopHalfOfScreen
import com.xuweilai.bubble.internal.dp
import java.lang.ref.WeakReference

class BubbleGumBuilder(activity: Activity) {

  private val activity: WeakReference<Activity>
  private val popupMode: PopupMode = PopupMode.VIEW

  /** The arrow of the bubble will point to this view */
  private var targetView: View? = null
  private var autoDismiss: Boolean = true
  private var bubbleContainer: BubbleContainer? = null
  private var bubbleView: BubbleView? = null
  private var bubbleToTargetViewOffset = 4.dp
  private var securityMargin = 4.dp
  private var cancelable: Boolean = true

  internal var icon: Drawable? = null
  internal var text: String? = null
  @Sp internal var textSize: Float = 13f
  internal var textMaxLines: Int = 4
  internal var iconIsTintable: Boolean = true
  @Dp internal var bubbleCornerRadius: Int = 4.dp
  internal var foregroundColor: Int = 0xffffffff.toInt()
  internal var backgroundColor: Int = 0xff555555.toInt()
  @Dp internal var bubbleMaxWidth: Int = 210.dp
  internal var showCloseIcon: Boolean = false
  internal var disallowLimitMaxWidth: Boolean = false
  internal var bubbleDirection: BubbleDirection = BubbleDirection.TOP
  /**
   * If it is not null, it means that the bubble view has reached the boundary of the screen,
   * and the position of the arrow of the bubble view can no longer be centered
   *
   * @see com.xuweilai.bubble.BubbleView.drawArrow
   */
  internal var arrowXOffset: Int? = null
  @MillSecond internal var showDuration: Long = 6000

  internal var onCloseClickListener: View.OnClickListener? = null

  companion object {
    private const val TAG = "BubbleGumBuilder"
  }

  init {
    this.activity = WeakReference(activity)
    onCloseClickListener = View.OnClickListener {
      dismiss()
    }
  }

  /** Set anchor view */
  fun targetView(targetView: View) = apply {
    this.targetView = targetView
  }

  fun bubbleMaxWidth(@Dp bubbleMaxWidth: Int) = apply {
    this.bubbleMaxWidth = bubbleMaxWidth.dp
  }

  fun bubbleOffset(@Dp bubbleOffset: Int) = apply {
    this.bubbleToTargetViewOffset = bubbleOffset.dp
  }

  fun bubbleCornerRadius(@Dp bubbleCornerRadius: Int) = apply {
    this.bubbleCornerRadius = bubbleCornerRadius.dp
  }

  fun text(text: String) = apply {
    this.text = text
  }

  fun textSize(@Sp textSize: Float) = apply {
    this.textSize = textSize
  }

  fun textMaxLines(textMaxLines: Int) = apply {
    this.textMaxLines = textMaxLines
  }

  fun singleLine(singleLine: Boolean = true) = apply {
    this.disallowLimitMaxWidth = singleLine
  }

  fun icon(icon: Drawable, tintIsTintable: Boolean = true) = apply {
    this.icon = icon
    this.iconIsTintable = tintIsTintable
  }

  fun showCloseIcon(showCloseIcon: Boolean = true) = apply {
    this.showCloseIcon = showCloseIcon
  }

  fun autoDismiss(autoDismiss: Boolean) = apply {
    this.autoDismiss = autoDismiss
  }

  fun duration(@MillSecond duration: Long) = apply {
    this.showDuration = duration
  }

  fun backgroundColor(backgroundColor: Int) = apply {
    this.backgroundColor = backgroundColor
  }

  fun foregroundColor(foregroundColor: Int) = apply {
    this.foregroundColor = foregroundColor
  }

  /** No extra margin is set if bubble reaches screen bounds */
  fun edgeToEdge(allowEdgeToEdge: Boolean = true) = apply {
    this.securityMargin = if (allowEdgeToEdge) 0.dp else 4.dp
  }

  /** Warning: If there are multiple bubbles in the screen at the same time, the setting may be invalid */
  fun cancelable(cancelable: Boolean) = apply {
    this.cancelable = cancelable
  }

  fun show() = apply {
    checkNotNull(activity.get()) { "Activity must not be null" }
    checkNotNull(targetView) { "Target view must not be null" }

    val targetView = this.targetView!!
    val activity = this.activity.get()!!
    val rootView = targetView.rootView as ViewGroup
    bubbleContainer = BubbleContainer(activity).also { it.cancelable = cancelable }
    bubbleView = BubbleView(activity, builder = this)
    rootView += bubbleContainer!!

    var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
      val xy = computeBubbleViewTranslationXY(rootView, targetView, bubbleView!!)
      // If the bubble is within the visible range of the container, show it
      if (xy.first() in bubbleContainer!!.x..bubbleContainer!!.width.toFloat()
        && xy.last() in bubbleContainer!!.y..bubbleContainer!!.height.toFloat()
      ) {
        bubbleView!!.translationX = xy.first()
        bubbleView!!.translationY = xy.last()
      }
      // Else dismiss it
      else {
        dismiss()
        Log.e(TAG, "Bubble view has been removed because it is off the screen")
      }
      targetView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }
    targetView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

    if (popupMode == PopupMode.VIEW) {
      val showAnimation = AnimationUtil.genFadeInAnimation(0, 280)
      bubbleContainer!!.addView(bubbleView!!.apply { startAnimation(showAnimation) })
    }

    if (autoDismiss && showDuration > 0) {
      targetView.postDelayed({ dismiss() }, showDuration)
    }
  }

  fun dismiss() {
    checkNotNull(bubbleContainer) { "Did you call the show() before dismiss()?" }

    val rootView = targetView?.rootView as ViewGroup
    val dismissAnimation = AnimationUtil.genFadeOutAnimation(0, 200)
    rootView.removeViewInLayout(bubbleContainer!!.apply { startAnimation(dismissAnimation) })

    onCloseClickListener = null
    activity.clear()
  }

  private fun computeBubbleViewTranslationXY(rootView: ViewGroup, targetView: View, bubbleView: BubbleView): FloatArray {
    val targetViewOnTopHalfOfScreen = targetView.atTopHalfOfScreen()
    bubbleDirection = if (targetViewOnTopHalfOfScreen) BubbleDirection.BOTTOM else BubbleDirection.TOP

    val bubbleViewLocationOnScreen = IntArray(2)
    val targetViewLocationOnScreen = IntArray(2)
    bubbleView.getLocationOnScreen(bubbleViewLocationOnScreen)
    targetView.getLocationOnScreen(targetViewLocationOnScreen)

    val targetViewCenterX = targetViewLocationOnScreen.first() + targetView.width / 2
    val bubbleViewLeftOffset = targetViewCenterX - bubbleView.width / 2
    val bubbleViewRightBoundPosition = bubbleViewLeftOffset + bubbleView.width

    val xPosition = bubbleViewLeftOffset
    val yPosition = if (targetViewOnTopHalfOfScreen) {
      targetViewLocationOnScreen.last() + targetView.height + bubbleToTargetViewOffset
    } else {
      targetViewLocationOnScreen.last() - bubbleView.height - bubbleToTargetViewOffset
    }

    // Set a safe margin from the edge of the screen to prevent sticking
    val xy = FloatArray(2)
    xy[0] = if (bubbleViewRightBoundPosition >= rootView.width) {
      val bubbleViewMaxLeftOffset = rootView.width - bubbleView.width - securityMargin
      arrowXOffset = targetViewCenterX - bubbleViewMaxLeftOffset
      bubbleViewMaxLeftOffset.toFloat()
    } else if (bubbleViewLeftOffset <= 0) {
      arrowXOffset = targetViewCenterX - securityMargin
      securityMargin.toFloat()
    } else {
      xPosition.toFloat()
    }
    xy[1] = yPosition.toFloat()

    return xy
  }

}