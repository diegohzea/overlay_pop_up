package com.requiemz.overlay_pop_up

import android.content.pm.ActivityInfo
import android.view.WindowManager
import android.view.Gravity

object PopUp {
    var height: Int = WindowManager.LayoutParams.MATCH_PARENT
    var width: Int = WindowManager.LayoutParams.MATCH_PARENT
    var verticalAlignment = Gravity.CENTER
    var horizontalAlignment = Gravity.CENTER
    var backgroundBehavior = 1
    var screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    var closeWhenTapBackButton = false
    var isDraggable = false
}