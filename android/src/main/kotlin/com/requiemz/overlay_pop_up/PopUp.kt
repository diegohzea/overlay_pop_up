package com.requiemz.overlay_pop_up

import android.content.Context
import android.content.SharedPreferences
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
    var lastX = 0
    var lastY = 0
    var entryPointMethodName: String = ""


    fun savePreferences(context: Context) {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences(
                OverlayPopUpPlugin.OVERLAY_CHANNEL_NAME,
                Context.MODE_PRIVATE
            )
        sharedPref.edit().putInt("height", height).apply()
        sharedPref.edit().putInt("width", width).apply()
        sharedPref.edit().putInt("verticalAlignment", verticalAlignment).apply()
        sharedPref.edit().putInt("horizontalAlignment", horizontalAlignment).apply()
        sharedPref.edit().putInt("backgroundBehavior", backgroundBehavior).apply()
        sharedPref.edit().putInt("screenOrientation", screenOrientation).apply()
        sharedPref.edit().putBoolean("closeWhenTapBackButton", closeWhenTapBackButton).apply()
        sharedPref.edit().putBoolean("isDraggable", isDraggable).apply()
        sharedPref.edit().putInt("lastX", lastX).apply()
        sharedPref.edit().putInt("lastY", lastY).apply()
        sharedPref.edit().putString("entryPointName", entryPointMethodName).apply()
    }

    fun loadPreferences(context: Context) {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences(
                OverlayPopUpPlugin.OVERLAY_CHANNEL_NAME,
                Context.MODE_PRIVATE
            )
        height = sharedPref.getInt("height", height)
        width = sharedPref.getInt("width", width)
        verticalAlignment = sharedPref.getInt("verticalAlignment", verticalAlignment)
        horizontalAlignment = sharedPref.getInt("horizontalAlignment", horizontalAlignment)
        backgroundBehavior = sharedPref.getInt("backgroundBehavior", backgroundBehavior)
        screenOrientation = sharedPref.getInt("screenOrientation", screenOrientation)
        closeWhenTapBackButton =
            sharedPref.getBoolean("closeWhenTapBackButton", closeWhenTapBackButton)
        isDraggable = sharedPref.getBoolean("isDraggable", isDraggable)
        lastX = sharedPref.getInt("lastX", lastX)
        lastY = sharedPref.getInt("lastY", lastY)
        entryPointMethodName = sharedPref.getString("entryPointName", entryPointMethodName) ?: ""
    }
}
