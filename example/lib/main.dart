import 'package:flutter/material.dart';
import 'package:simdec/simdec.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _simDec = Simdec();
  String? data;
  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: SafeArea(child: Scaffold(
      body: Builder(builder: (context) {
        if (data == null) {
          return Center(
              child: ElevatedButton(
                  onPressed: () async {
                    data = await _simDec.getSimCard();
                    print(data);
                    setState(() {});
                  },
                  child: const Text("Get sim info")));
        } else {
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(15.0),
              child: Text("$data"),
            ),
          );
        }
      }),
    )));
  }
}
