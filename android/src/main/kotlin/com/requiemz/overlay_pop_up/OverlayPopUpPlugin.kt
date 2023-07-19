package com.requiemz.overlay_pop_up

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.JSONMessageCodec
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


class OverlayPopUpPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    BasicMessageChannel.MessageHandler<Any?> {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var messageChannel: BasicMessageChannel<Any?>

    companion object {
        const val OVERLAY_CHANNEL_NAME = "overlay_pop_up"
        const val OVERLAY_MESSAGE_CHANNEL_NAME = "overlay_pop_up_mssg"
        const val CACHE_ENGINE_ID = "overlay_pop_up_engine_id"
        const val OVERLAY_POP_UP_ENTRY = "overlayPopUp"
        const val PERMISSION_CODE = 1996
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, OVERLAY_CHANNEL_NAME)
        channel.setMethodCallHandler(this)
        this.context = flutterPluginBinding.applicationContext
        messageChannel = BasicMessageChannel<Any?>(
            flutterPluginBinding.binaryMessenger, OVERLAY_MESSAGE_CHANNEL_NAME,
            JSONMessageCodec.INSTANCE
        )
        messageChannel.setMessageHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "requestPermission" -> requestOverlayPermission(result)
            "checkPermission" -> result.success(checkPermission())
            "showOverlay" -> showOverlay(call, result)
            "closeOverlay" -> closeOverlay(result)
            "isActive" -> result.success(OverlayService.isActive)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        messageChannel.setMessageHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        val engineGroup = FlutterEngineGroup(context)
        val dartEntry = DartExecutor.DartEntrypoint(
            FlutterInjector.instance().flutterLoader().findAppBundlePath(),
            OVERLAY_POP_UP_ENTRY
        )
        val engine = engineGroup.createAndRunEngine(context, dartEntry)
        FlutterEngineCache.getInstance().put(CACHE_ENGINE_ID, engine)
    }

    private fun requestOverlayPermission(result: Result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            i.data = Uri.parse("package:${activity.packageName}")
            activity.startActivityForResult(i, PERMISSION_CODE)
        } else result.success(true)
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    private fun showOverlay(call: MethodCall, result: Result) {
        val i = Intent(context, OverlayService::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_SINGLE_TOP
        PopUp.width = call.argument<Int>("width") ?: PopUp.width
        PopUp.height = call.argument<Int>("height") ?: PopUp.height
        PopUp.alignment = call.argument<Int>("alignment") ?: PopUp.alignment
        PopUp.backgroundBehavior =
            call.argument<Int>("backgroundBehavior") ?: PopUp.backgroundBehavior
        activity.startService(i)
        result.success(true)
    }

    private fun closeOverlay(result: Result) {
        if (OverlayService.isActive) {
            val i = Intent(context, OverlayService::class.java)
            i.putExtra("closeOverlay", true)
            context.stopService(i)
            OverlayService.isActive = false
            result.success(true)
            return
        }
        result.success(true)
    }

    override fun onMessage(message: Any?, reply: BasicMessageChannel.Reply<Any?>) {
        val overlayMessageChannel = BasicMessageChannel(
            FlutterEngineCache.getInstance().get(OverlayPopUpPlugin.CACHE_ENGINE_ID)!!.dartExecutor,
            OVERLAY_MESSAGE_CHANNEL_NAME,
            JSONMessageCodec.INSTANCE
        )
        overlayMessageChannel.send(message, reply)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
    }
}
