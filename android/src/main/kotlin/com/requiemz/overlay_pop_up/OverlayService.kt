package com.requiemz.overlay_pop_up

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.KeyEvent
import android.view.WindowManager
import io.flutter.FlutterInjector
import io.flutter.embedding.android.FlutterTextureView
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.JSONMessageCodec
import io.flutter.plugin.common.MethodChannel


class OverlayService : Service(), BasicMessageChannel.MessageHandler<Any?> {
    companion object {
        var isActive: Boolean = false
    }

    private var channel: MethodChannel? = null
    private var windowManager: WindowManager? = null
    private lateinit var flutterView: FlutterView
    private lateinit var overlayMessageChannel: BasicMessageChannel<Any?>

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        validateDartExecutor()
        val engine = FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)!!
        engine.lifecycleChannel.appIsResumed()
        flutterView =
            object : FlutterView(applicationContext, FlutterTextureView(applicationContext)) {
                override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                    return if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                        stopService(Intent(baseContext, OverlayService::class.java))
                        windowManager?.removeView(flutterView)
                        isActive = false
                        true
                    } else super.dispatchKeyEvent(event)
                }
            }
        flutterView.attachToFlutterEngine(
            FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)!!
        )
        flutterView.fitsSystemWindows = true
        flutterView.setBackgroundColor(Color.TRANSPARENT)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager?
        val windowConfig = WindowManager.LayoutParams(
            PopUp.width,
            PopUp.height,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            if (PopUp.backgroundBehavior == 1) WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE else WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS and
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE and WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSPARENT
        )
        windowConfig.gravity = PopUp.alignment
        windowManager!!.addView(flutterView, windowConfig)
        isActive = true
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.removeView(flutterView)
        isActive = false
    }

    private fun validateDartExecutor() {
        val dartExecutor = FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)
        if (dartExecutor == null) {
            val engineGroup = FlutterEngineGroup(applicationContext)
            val dartEntry = DartExecutor.DartEntrypoint(
                FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                OverlayPopUpPlugin.OVERLAY_POP_UP_ENTRY
            )
            val engine = engineGroup.createAndRunEngine(applicationContext, dartEntry)
            FlutterEngineCache.getInstance().put(OverlayPopUpPlugin.CACHE_ENGINE_ID, engine)
        }
        channel = MethodChannel(
            FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)!!.dartExecutor,
            OverlayPopUpPlugin.OVERLAY_CHANNEL_NAME
        )
        overlayMessageChannel = BasicMessageChannel(
            FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)!!.dartExecutor,
            OverlayPopUpPlugin.OVERLAY_MESSAGE_CHANNEL_NAME,
            JSONMessageCodec.INSTANCE
        )
    }

    override fun onMessage(message: Any?, reply: BasicMessageChannel.Reply<Any?>) {
        val overlayMessageChannel = BasicMessageChannel(
            FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)!!.dartExecutor,
            OverlayPopUpPlugin.OVERLAY_MESSAGE_CHANNEL_NAME,
            JSONMessageCodec.INSTANCE
        )
        overlayMessageChannel.send(message, reply)
    }
}