import 'package:flutter/services.dart';

class Simdec {
  final MethodChannel _methodChannel = const MethodChannel("simdec");
  Future<dynamic> getSimCard() async {
    return await _methodChannel.invokeMethod("getSimcard");
  }
}
