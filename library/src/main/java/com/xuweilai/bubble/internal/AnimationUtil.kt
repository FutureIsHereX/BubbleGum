package com.xuweilai.bubble.internal

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator

object AnimationUtil {

  fun genFadeInAnimation(offset: Int, duration: Int): Animation {
    return AlphaAnimation(0f, 1f).apply {
      startOffset = offset.toLong()
      interpolator = DecelerateInterpolator()
      this.duration = duration.toLong()
    }
  }

  fun genFadeOutAnimation(offset: Int, duration: Int): Animation {
    return AlphaAnimation(1f, 0f).apply {
      startOffset = offset.toLong()
      interpolator = DecelerateInterpolator()
      this.duration = duration.toLong()
    }
  }

}