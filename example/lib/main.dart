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
  var data = SimCard();
  @override
  void initState() {
    // TODO: implement initState
    _simDec.simState.listen((event) {
      setState(() {
        data = event;
      });
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: SafeArea(child: Scaffold(
      body: Builder(builder: (context) {
        return Center(
          child: Padding(
            padding: const EdgeInsets.all(15.0),
            child: Text("${data.toJson()}"),
          ),
        );
      }),
    )));
  }
}
