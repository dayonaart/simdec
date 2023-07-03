import 'package:flutter/services.dart';

class Simdec {
  static const EventChannel eventChannel = EventChannel('simListen');

  Stream<dynamic> get simState {
    return eventChannel.receiveBroadcastStream();
  }
}
