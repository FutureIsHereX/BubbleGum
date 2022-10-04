package com.xuweilai.bubble.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xuweilai.bubble.BubbleGum

class MainActivity : AppCompatActivity() {

  @SuppressLint("UseCompatLoadingForDrawables")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val textHello = findViewById<TextView>(R.id.text_hello)
    val toolBar = findViewById<MaterialToolbar>(R.id.toolbar_main)
    val toolBarTitleView = toolBar[0]
    val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottom_nav_main)
    val bottomItemView = (bottomNavBar[0] as ViewGroup)[1]

    toolBar.postDelayed({
      BubbleGum.Builder(this)
        .text("You have 3 new messages")
        .icon(getDrawable(R.drawable.ic_round_mail_24)!!)
        .backgroundColor(getColor(R.color.purple_500))
        .targetView(bottomItemView)
        .show()
    }, 3000)

    BubbleGum.Builder(this)
      .text("BubbleGum allows you to create and popup bubble in Android application")
      .targetView(toolBarTitleView)
      .bubbleCornerRadius(10)
      .backgroundColor(0xfff7793f.toInt())
      .bubbleOffset(20)
      .textMaxLines(2)
      .textSize(16f)
      .edgeToEdge(true)
      .autoDismiss(false)
      .foregroundColor(0xff742500.toInt())
      .showCloseIcon()
      .bubbleMaxWidth(300)
      .cancelable(false)
      .show()

    BubbleGum.Builder(this)
      .text("This is a tip for information")
      .backgroundColor(0xff0052ff.toInt())
      .icon(getDrawable(R.drawable.ic_round_info_24)!!)
      .showCloseIcon()
      .targetView(textHello)
      .singleLine()
      .show()

  }

}