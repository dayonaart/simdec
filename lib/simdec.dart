import 'package:flutter/services.dart';

class Simdec {
  static const EventChannel eventChannel = EventChannel('simListen');

  Stream<SimCard> get simState {
    return eventChannel.receiveBroadcastStream().map<SimCard>(
        (event) => SimCard.fromJson(Map<String, dynamic>.from(event)));
  }
}

class SimCard {
  String? message;

  /// If the calling app's target SDK is API level 28 or lower return SSN instead, or if the calling app is targeting API level 29 or higher return resetable id.
  String? simId;

  SimCard({
    this.message,
    this.simId,
  });
  SimCard.fromJson(Map<String, dynamic> json) {
    message = json['message']?.toString();
    simId = json['sim_id']?.toString();
  }
  Map<String, dynamic> toJson() {
    final data = <String, dynamic>{};
    data['message'] = message;
    if (simId != null) data['sim_id'] = simId;
    return data;
  }
}
