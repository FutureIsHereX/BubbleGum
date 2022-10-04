package com.xuweilai.bubble

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.plusAssign
import com.xuweilai.bubble.internal.AnimationUtil
import com.xuweilai.bubble.internal.annotation.Dp
import com.xuweilai.bubble.internal.annotation.MillSecond
import com.xuweilai.bubble.internal.annotation.Sp
import com.xuweilai.bubble.internal.atTopHalfOfScreen
import com.xuweilai.bubble.internal.dp
import java.lang.ref.WeakReference

class BubbleGum internal constructor(
  builder: Builder
) {

  private val activity: WeakReference<Activity> = WeakReference(builder.activity)
  private val targetView: View? = builder.targetView
  private var bubbleContainer: BubbleContainer? = null
  private var bubbleView: BubbleGumView? = null
  private val popupMode: PopupMode = builder.popupMode
  private val cancelable: Boolean = builder.cancelable
  private val autoDismiss: Boolean = builder.autoDismiss
  private val showDuration: Long = builder.showDuration
  private val securityMargin: Int = builder.securityMargin
  private var isShowing: Boolean = false
  private val bubbleToTargetViewOffset: Int = builder.bubbleToTargetViewOffset

  internal val foregroundColor: Int = builder.foregroundColor
  internal val backgroundColor: Int = builder.backgroundColor
  internal val bubbleMaxWidth: Int = builder.bubbleMaxWidth
  internal val bubbleCornerRadius: Int = builder.bubbleCornerRadius
  internal val text: String? = builder.text
  internal val textSize: Float = builder.textSize
  internal val textMaxLines: Int = builder.textMaxLines
  internal val icon: Drawable? = builder.icon
  internal val tintIcon: Boolean = builder.tintIcon
  internal val showCloseIcon: Boolean = builder.showCloseIcon
  internal val disallowLimitMaxWidth: Boolean = builder.disallowLimitMaxWidth
  internal var bubbleDirection: BubbleDirection = BubbleDirection.TOP
  internal var arrowXOffset: Int? = null
  internal var onCloseClickListener: View.OnClickListener? = null

  companion object {
    private const val TAG = "BubbleGum"
  }

  fun show() = apply {
    if (isShowing) return@apply

    checkNotNull(targetView) { "Target view must not be null" }

    val targetView = this.targetView
    val rootView = targetView.rootView as ViewGroup
    bubbleContainer = BubbleContainer(activity.get()!!).also { it.cancelable = cancelable }
    bubbleView = BubbleGumView(activity.get()!!, this)
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
        if (BuildConfig.DEBUG) Log.e(TAG, "Bubble view has been removed because it is off the screen")
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

    isShowing = true
    onCloseClickListener = View.OnClickListener {
      dismiss()
    }
  }

  fun dismiss() {
    if (!isShowing) return

    checkNotNull(bubbleContainer) { "Did you call the show() before dismiss()?" }

    val rootView = targetView?.rootView as ViewGroup
    val dismissAnimation = AnimationUtil.genFadeOutAnimation(0, 200)
    rootView.removeViewInLayout(bubbleContainer!!.apply { startAnimation(dismissAnimation) })
    bubbleView = null
    bubbleContainer = null

    isShowing = false
    onCloseClickListener = null
    activity.clear()
  }

  private fun computeBubbleViewTranslationXY(rootView: ViewGroup, targetView: View, bubbleView: BubbleGumView): FloatArray {
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

  class Builder constructor(internal val activity: Activity) {

    internal val popupMode: PopupMode = PopupMode.VIEW
    /** The arrow of the bubble will point to this view */
    internal var targetView: View? = null
    internal var autoDismiss: Boolean = true
    internal var bubbleToTargetViewOffset = 4.dp
    internal var securityMargin = 4.dp
    internal var cancelable: Boolean = true
    internal var icon: Drawable? = null
    internal var text: String? = null
    @Sp internal var textSize: Float = 13f
    internal var textMaxLines: Int = 4
    internal var tintIcon: Boolean = true
    @Dp internal var bubbleCornerRadius: Int = 4.dp
    internal var foregroundColor: Int = colorWhite
    internal var backgroundColor: Int = colorGray
    @Dp internal var bubbleMaxWidth: Int = 210.dp
    internal var showCloseIcon: Boolean = false
    internal var disallowLimitMaxWidth: Boolean = false
    @MillSecond internal var showDuration: Long = 6000

    companion object {
      private const val colorGray = 0xff555555.toInt()
      private const val colorWhite = 0xffffffff.toInt()
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

    fun icon(icon: Drawable, tintIcon: Boolean = true) = apply {
      this.icon = icon
      this.tintIcon = tintIcon
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

    fun cancelable(cancelable: Boolean) = apply {
      this.cancelable = cancelable
    }

    fun build(): BubbleGum = BubbleGum(this)

    fun show(): BubbleGum = build().show()

  }

}