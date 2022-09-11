package com.xuweilai.bubble.internal

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup

internal val Int.dp: Int
  get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

internal const val match_parent = ViewGroup.LayoutParams.MATCH_PARENT
internal const val wrap_content = ViewGroup.LayoutParams.WRAP_CONTENT

internal fun View.atTopHalfOfScreen(): Boolean {
  val location = IntArray(2)
  getLocationOnScreen(location)
  return location[1] < (Resources.getSystem().displayMetrics.heightPixels / 2)
}