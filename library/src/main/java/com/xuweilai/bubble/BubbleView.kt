package com.xuweilai.bubble

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.xuweilai.bubble.internal.dp
import com.xuweilai.bubble.internal.wrap_content

@SuppressLint("ViewConstructor")
internal class BubbleView constructor(
  context: Context,
  private val builder: BubbleGumBuilder
) : LinearLayout(context) {

  private val contentGap = 8.dp
  private val rectRoundRadius = builder.bubbleCornerRadius
  private val rhombusBorderLength = 6.dp

  private var infoColor = builder.foregroundColor
  private var bubbleColor = builder.backgroundColor
  private var bubbleMaxWidth = builder.bubbleMaxWidth

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

  private val iconView = AppCompatImageView(context).apply {
    val iconViewSize = 21.dp
    layoutParams = MarginLayoutParams(iconViewSize, iconViewSize).also {
      it.setMargins(contentGap, 0, contentGap, 0)
    }
  }

  private val titleTextView = AppCompatTextView(context).apply {
    layoutParams = LayoutParams(wrap_content, wrap_content).apply {
      weight = 1f
      setMargins(0, rhombusBorderLength, 0, rhombusBorderLength)
    }
    text = builder.text
    textSize = builder.textSize
    maxLines = builder.textMaxLines
    ellipsize = TextUtils.TruncateAt.END
    setTextColor(infoColor)
  }

  private val closeView = AppCompatImageView(context).apply {
    val closeViewSize = 20.dp
    layoutParams = MarginLayoutParams(closeViewSize, closeViewSize).also {
      it.setMargins(contentGap, 0, contentGap, 0)
    }
    imageAlpha = 150
    setImageResource(R.drawable.ic_round_close_24)
  }

  init {
    layoutParams = ViewGroup.LayoutParams(wrap_content, wrap_content)
    orientation = HORIZONTAL
    setVerticalGravity(Gravity.CENTER_VERTICAL)

    val layoutVerticalPadding = 10.dp
    setPadding(0, layoutVerticalPadding, 0, layoutVerticalPadding)

    setWillNotDraw(false)

    addView(titleTextView)

    initAttribute()
    initListener()
  }

  private fun initAttribute() {
    with(builder) {
      if (icon != null) {
        iconView.setImageDrawable(icon)
        if (builder.iconIsTintable) {
          DrawableCompat.setTint(iconView.drawable, infoColor)
        }
        addView(iconView, 0)
      } else {
        updatePadding(left = 10.dp)
      }
      if (showCloseIcon) {
        addView(closeView)
        DrawableCompat.setTint(closeView.drawable, infoColor)
      } else {
        updatePadding(right = 10.dp)
      }
    }
  }

  private fun initListener() {
    if (builder.showCloseIcon) {
      closeView.setOnClickListener {
        builder.onCloseClickListener?.onClick(it)
      }
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    if (!builder.disallowLimitMaxWidth && measuredWidth > bubbleMaxWidth) {
      updateLayoutParams<ViewGroup.LayoutParams> {
        width = bubbleMaxWidth
      }
    }
  }

  override fun onDraw(canvas: Canvas) {
    paint.color = bubbleColor
    drawBubble(canvas)
  }

  private fun drawBubble(canvas: Canvas) {
    drawArrow(canvas, builder.bubbleDirection)
    drawRoundRectangle(canvas)
  }

  private fun drawRoundRectangle(canvas: Canvas) {
    val rect = RectF(
      0f,
      rhombusBorderLength.toFloat(),
      width.toFloat(),
      (height - rhombusBorderLength).toFloat()
    )
    canvas.drawRoundRect(rect, rectRoundRadius.toFloat(), rectRoundRadius.toFloat(), paint)
  }

  private fun drawArrow(canvas: Canvas, direction: BubbleDirection) {
    val yPosition: Int = when (direction) {
      BubbleDirection.TOP -> height - rhombusBorderLength
      BubbleDirection.BOTTOM -> rhombusBorderLength
    }
    val xPosition = if (builder.arrowXOffset != null) {
      builder.arrowXOffset!!
    } else {
      width / 2
    }
    drawRhombus(canvas, Point(xPosition, yPosition))
  }

  private fun drawRhombus(canvas: Canvas, point: Point) {
    val path = Path()
    with(point) {
      path.moveTo(x.toFloat(), (y + rhombusBorderLength).toFloat())
      path.lineTo((x - rhombusBorderLength).toFloat(), y.toFloat())
      path.lineTo(x.toFloat(), (y - rhombusBorderLength).toFloat())
      path.lineTo((x + rhombusBorderLength).toFloat(), y.toFloat())
      path.lineTo(x.toFloat(), (y + rhombusBorderLength).toFloat())
      path.close()
    }
    canvas.drawPath(path, paint)
  }

  fun dismiss() {
    builder.dismiss()
  }

}