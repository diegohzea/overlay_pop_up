import 'dart:async';

import 'package:flutter/material.dart';
import 'package:overlay_pop_up/overlay_pop_up.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool isActive = false;
  bool permissionStatus = false;
  String overlayPosition = '';

  @override
  void initState() {
    super.initState();
    overlayStatus();
  }

  Future<void> overlayStatus() async {
    isActive = await OverlayPopUp.isActive();
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
            title: const Text(
              'Flutter overlay pop up',
              style: TextStyle(color: Colors.white),
            ),
            backgroundColor: Colors.red[900]),
        body: SizedBox(
          width: double.maxFinite,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              const SizedBox(height: 30),
              Text(
                'Permission status: ${permissionStatus ? 'enabled' : 'disabled'}',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              MaterialButton(
                onPressed: () async {
                  permissionStatus = await OverlayPopUp.requestPermission();
                  setState(() {});
                },
                color: Colors.red[900],
                child: const Text(
                  'Request overlay permission',
                  style: TextStyle(color: Colors.white),
                ),
              ),
              const SizedBox(height: 20),
              Text(
                'Is active: $isActive',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              MaterialButton(
                onPressed: () async {
                  final permission = await OverlayPopUp.checkPermission();
                  if (permission) {
                    if (!await OverlayPopUp.isActive()) {
                      isActive = await OverlayPopUp.showOverlay(
                        width: 300,
                        height: 350,
                        screenOrientation: ScreenOrientation.portrait,
                        closeWhenTapBackButton: true,
                        isDraggable: true,
                      );
                      setState(() {
                        isActive = isActive;
                      });
                      return;
                    } else {
                      final result = await OverlayPopUp.closeOverlay();
                      setState(() {
                        isActive = (result == true) ? false : true;
                      });
                    }
                  } else {
                    permissionStatus = await OverlayPopUp.requestPermission();
                    setState(() {});
                  }
                },
                color: Colors.red[900],
                child: const Text(
                  'Show overlay',
                  style: TextStyle(color: Colors.white),
                ),
              ),
              MaterialButton(
                onPressed: () async {
                  if (await OverlayPopUp.isActive()) {
                    await OverlayPopUp.sendToOverlay(
                        {'mssg': 'Hello from dart!'});
                  }
                },
                color: Colors.red[900],
                child: const Text('Send data',
                    style: TextStyle(color: Colors.white)),
              ),
              MaterialButton(
                onPressed: () async {
                  if (await OverlayPopUp.isActive()) {
                    await OverlayPopUp.updateOverlaySize(
                        width: 500, height: 500);
                  }
                },
                color: Colors.red[900],
                child: const Text('Update overlay size',
                    style: TextStyle(color: Colors.white)),
              ),
              MaterialButton(
                onPressed: () async {
                  if (await OverlayPopUp.isActive()) {
                    final position = await OverlayPopUp.getOverlayPosition();
                    setState(() {
                      overlayPosition = (position?['overlayPosition'] != null)
                          ? position!['overlayPosition'].toString()
                          : '';
                    });
                  }
                },
                color: Colors.red[900],
                child: const Text('Get overlay position',
                    style: TextStyle(color: Colors.white)),
              ),
              Text('Current position: $overlayPosition'),
            ],
          ),
        ),
      ),
    );
  }
}

///
/// the name is required to be `overlayPopUp` and has `@pragma("vm:entry-point")`
///
@pragma("vm:entry-point")
void overlayPopUp() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MaterialApp(
    debugShowCheckedModeBanner: false,
    home: OverlayWidget(),
  ));
}

class OverlayWidget extends StatelessWidget {
  const OverlayWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: Container(
        padding: const EdgeInsets.all(15),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizedBox(
              child: StreamBuilder(
                stream: OverlayPopUp.dataListener,
                initialData: null,
                builder: (BuildContext context, AsyncSnapshot snapshot) {
                  return Text(
                    snapshot.data?['mssg'] ?? '',
                    style: const TextStyle(fontSize: 14),
                    textAlign: TextAlign.center,
                  );
                },
              ),
            ),
            const SizedBox(height: 10),
            FloatingActionButton(
              shape: const CircleBorder(),
              backgroundColor: Colors.red[900],
              elevation: 12,
              onPressed: () async => await OverlayPopUp.closeOverlay(),
              child: const Text('X',
                  style: TextStyle(color: Colors.white, fontSize: 20)),
            ),
          ],
        ),
      ),
    );
  }
}
