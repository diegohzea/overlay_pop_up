import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class OverlayPopUp {
  OverlayPopUp._();

  static final StreamController _mssgController = StreamController.broadcast();

  static const _methodChannel = MethodChannel('overlay_pop_up');
  static const _messageChannel = BasicMessageChannel('overlay_pop_up_mssg', JSONMessageCodec());

  ///
  /// returns true when overlay permission is alreary granted
  /// if permission is not granted then open app settings
  ///
  static Future<bool> requestPermission() async {
    final result = await _methodChannel.invokeMethod<bool?>('requestPermission');
    return result ?? false;
  }

  ///
  /// returns true or false according to permission status
  ///
  static Future<bool> checkPermission() async {
    final result = await _methodChannel.invokeMethod<bool?>('checkPermission');
    return result ?? false;
  }

  ///
  /// display your overlay and return true if is showed
  /// [height] is not required by default is MATCH_PARENT
  /// [width] is not required by default is MATCH_PARENT
  /// [alignment] is not required by default is CENTER for more info see: https://developer.android.com/reference/android/view/WindowManager.LayoutParams
  /// [backgroundBehavior] by default is focusable flag that is you can take focus inside a overlay for example inside a textfield and [tapThrough] you can tap through the overlay background even if has MATCH_PARENT sizes.
  /// [screenOrientation] by default is portrait its param define the overlay orientation.
  ///
  static Future<bool> showOverlay({
    int? height,
    int? width,
    Gravity? verticalAlignment,
    Gravity? horizontalAlignment,
    OverlayFlag? backgroundBehavior,
    ScreenOrientation? screenOrientation,
    bool? closeWhenTapBackButton = false,
    bool? isDraggable = false,
  }) async {
    final result = await _methodChannel.invokeMethod<bool?>('showOverlay', {
      /// is not required by default is MATCH_PARENT
      'height': height,

      /// is not required by default is MATCH_PARENT
      'width': width,

      /// is not required by default is CENTER for more info see: https://developer.android.com/reference/android/view/Gravity
      'verticalAlignment': verticalAlignment?.value,

      /// is not required by default is CENTER for more info see: https://developer.android.com/reference/android/view/Gravity
      'horizontalAlignment': horizontalAlignment?.value,

      /// by default is focusable flag that is you can take focus inside a overlay for example inside a textfield and [tapThrough] you can tap through the overlay background even if has MATCH_PARENT sizes.
      'backgroundBehavior': backgroundBehavior?.value,

      /// by default is portrait its param define the overlay orientation.
      'screenOrientation': screenOrientation?.value,

      /// by default is false its param define if overlay will close when user tap back button.
      'closeWhenTapBackButton': closeWhenTapBackButton,

      /// by default is false therefore the overlay can´t be dragged.
      'isDraggable': isDraggable,
    });
    return result ?? false;
  }

  ///
  /// returns true if overlay closed correctly or already is closed
  ///
  static Future<bool?> closeOverlay() async {
    final result = await _methodChannel.invokeMethod<bool?>('closeOverlay');
    return result;
  }

  ///
  /// returns the overlay status true = open, false = closed
  ///
  static Future<bool> isActive() async {
    final result = await _methodChannel.invokeMethod<bool?>('isActive');
    return result ?? false;
  }

  ///
  /// returns the current overlay position if enable drag is enabled
  ///
  static Future<Map?> getOverlayPosition() async {
    final result = await _methodChannel.invokeMethod<Map?>('getOverlayPosition');
    return result;
  }

  ///
  /// update overlay layout size
  ///
  static Future<bool> updateOverlaySize({
    int? height,
    int? width,
  }) async {
    final result = await _methodChannel.invokeMethod<bool?>('updateOverlaySize', {
      /// the new value for layout height
      'height': height,

      /// the new value for layout width
      'width': width,
    });
    return result ?? false;
  }

  ///
  /// share dynamic data to overlay
  ///
  static Future<void> sendToOverlay(dynamic data) async {
    await _messageChannel.send(data);
  }

  ///
  /// receive the data from flutter
  ///
  static Stream<dynamic>? get dataListener {
    _messageChannel.setMessageHandler((mssg) async {
      if (_mssgController.isClosed) return '';
      _mssgController.add(mssg);
      return mssg;
    });

    if (_mssgController.isClosed) return null;
    return _mssgController.stream;
  }

  ///
  /// drain and close data stream controller
  ///
  static void stopDataLIstener() {
    try {
      _mssgController.stream.drain();
      _mssgController.close();
    } catch (e) {
      debugPrint('[OverlayPopUp] Something wen wrong when close overlay pop up: $e');
    }
  }
}

enum Gravity {
  axisClip(8),
  axisPullAfter(4),
  axisPullBefore(2),
  axisSpecified(1),
  axisxShift(0),
  axisyShift(4),
  bottom(80),
  center(17),
  centerHorizontal(1),
  centerVertical(16),
  clipHorizontal(8),
  clipVertical(128),
  displayClipHorizontal(16777216),
  displayClipVertical(268435456),
  end(8388613),
  fill(119),
  fillHorizontal(7),
  fillVertical(112),
  horizontalGravityMask(7),
  left(3),
  noGravity(0),
  relativeHorizontalGravityMask(8388615),
  relativeLayoutDirection(8388608),
  right(5),
  start(8388611),
  top(48),
  verticalGravityMask(112);

  final int value;
  const Gravity(this.value);
}

enum OverlayFlag {
  focusable(1),
  tapThrough(0);

  final int value;
  const OverlayFlag(this.value);
}

enum ScreenOrientation {
  landscape(0),
  portrait(1);

  final int value;
  const ScreenOrientation(this.value);
}
