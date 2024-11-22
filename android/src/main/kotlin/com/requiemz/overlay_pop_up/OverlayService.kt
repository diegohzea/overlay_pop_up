package com.requiemz.overlay_pop_up

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import io.flutter.embedding.android.FlutterTextureView
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.JSONMessageCodec

class OverlayService : Service(), BasicMessageChannel.MessageHandler<Any?>, View.OnTouchListener {
    companion object {
        var isActive: Boolean = false
        var windowManager: WindowManager? = null
        lateinit var flutterView: FlutterView
        var lastX = 0f
        var lastY = 0f
    }

    private lateinit var overlayMessageChannel: BasicMessageChannel<Any?>

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        PopUp.loadPreferences(applicationContext)
        val engine = FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)
        if (engine == null) {
            println("[OverlayPopUp] FlutterEngine not available in cache. Stopping service.")
            stopSelf()
            return
        }
        engine.lifecycleChannel.appIsResumed()
        flutterView = object : FlutterView(applicationContext, FlutterTextureView(applicationContext)) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                return if (event.keyCode == KeyEvent.KEYCODE_BACK && PopUp.closeWhenTapBackButton) {
                    windowManager?.removeView(flutterView)
                    stopService(Intent(baseContext, OverlayService::class.java))
                    isActive = false
                    true
                } else super.dispatchKeyEvent(event)
            }
        }
        flutterView.attachToFlutterEngine(engine)
        flutterView.fitsSystemWindows = true
        flutterView.setBackgroundColor(Color.TRANSPARENT)
        flutterView.setOnTouchListener(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager?
        val windowConfig = WindowManager.LayoutParams(
            PopUp.width,
            PopUp.height,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            if (PopUp.backgroundBehavior == 1) WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN else
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSPARENT
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            windowConfig.flags = windowConfig.flags or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        }
        windowConfig.gravity = PopUp.verticalAlignment or PopUp.horizontalAlignment
        windowConfig.screenOrientation = PopUp.screenOrientation
        windowManager?.addView(flutterView, windowConfig)
        loadLastPosition()
        isActive = true
        println("[OverlayPopUp] Overlay successfully initialized.")
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.removeView(flutterView)
        isActive = false
    }

    override fun onMessage(message: Any?, reply: BasicMessageChannel.Reply<Any?>) {
        val engine = FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)
        if (engine != null) {
            val overlayMessageChannel = BasicMessageChannel(
                engine.dartExecutor,
                OverlayPopUpPlugin.OVERLAY_MESSAGE_CHANNEL_NAME,
                JSONMessageCodec.INSTANCE
            )
            overlayMessageChannel.send(message, reply)
        } else {
            println("[OverlayPopUp] FlutterEngine not available in cache.")
            reply.reply(null)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (!PopUp.isDraggable) return false
        val windowConfig = flutterView.layoutParams as LayoutParams
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY
                if (dx * dx + dy * dy < 25) {
                    return false
                }
                lastX = event.rawX
                lastY = event.rawY
                val finalX = windowConfig.x + dx.toInt()
                val finalY = windowConfig.y + dy.toInt()
                windowConfig.x = finalX
                windowConfig.y = finalY
                windowManager?.updateViewLayout(flutterView, windowConfig)
                saveLastPosition(finalX, finalY)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                windowManager?.updateViewLayout(flutterView, windowConfig)
                return false
            }

            else -> return false
        }
        return false
    }

    private fun saveLastPosition(x: Int, y: Int) {
        PopUp.lastX = x
        PopUp.lastY = y
        PopUp.savePreferences(applicationContext)
    }

    private fun loadLastPosition() {
        if (PopUp.lastY == 0 && PopUp.lastX == 0) return
        val windowConfig = flutterView.layoutParams as LayoutParams
        windowConfig.x = PopUp.lastX
        windowConfig.y = PopUp.lastY
        windowManager?.updateViewLayout(flutterView, windowConfig)
    }
}