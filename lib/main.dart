import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Android Camera',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Android Camera'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('camera');
  String? imagePath;

  Future<void> openCamera() async {
    bool isCameraGranted = await _requestPermission(Permission.camera);

    if (isCameraGranted) {
      try {
        final String? result = await platform.invokeMethod('openCamera');
        setState(() {
          imagePath = result;
        });
        print('Image Path Returned: $imagePath');
      } on PlatformException catch (e) {
        print("Failed to open camera: '${e.toString()}'.");
      }
    } else {
      print('Permission denied');
    }
  }

  Future<bool> _requestPermission(Permission permission) async {
    if (await permission.isGranted) {
      return true;
    }

    var status = await permission.request();
    return status == PermissionStatus.granted;
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        body: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Center(
              child: SingleChildScrollView(
                child: Padding(
                  padding: const EdgeInsets.all(32),
                  child: Column(
                    children: [
                      ElevatedButton(
                        onPressed: openCamera,
                        child: const Text('Take a Photo'),
                      ),
                      if (imagePath != null) ...[
                        const SizedBox(
                          height: 64,
                        ),
                        const Text(
                          "Your Photo",
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(
                          height: 16,
                        ),
                        SizedBox(
                          width: MediaQuery.of(context).size.width * 0.6,
                          child: Image.file(
                            File(imagePath!),
                            fit: BoxFit.contain,
                          ),
                        ),
                      ]
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
