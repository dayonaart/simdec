package id.bni46.simdec

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings.Secure
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import java.io.*
import java.util.*


/** SimdecPlugin */
class SimdecPlugin : FlutterPlugin, ActivityAware, EventChannel.StreamHandler,
    BroadcastReceiver(), RequestPermissionsResultListener {
    private lateinit var channel: MethodChannel
    var context: Context? = null
    private lateinit var activity: Activity
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventSink? = null
    private var simObject = HashMap<String, String?>()
    private lateinit var subscriptionInfo: SubscriptionInfo
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "simListen")
        eventChannel.setStreamHandler(this)
        IntentFilter("android.intent.action.SIM_STATE_CHANGED").also {
            context?.registerReceiver(this, it)
        }

    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    @SuppressLint("HardwareIds")
    private fun getSimData(): HashMap<String, String?> {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                simObject["message"] = "Success"
                simObject["sim_id"] = telephonyManager.simSerialNumber
                simObject
            } else {
                val unix = "${
                    Secure.getString(
                        context?.contentResolver,
                        Secure.ANDROID_ID
                    )
                }${subscriptionInfo.subscriptionId}"
                simObject["message"] = "Success"
                simObject["sim_id"] = String(unix.toByteArray())
            }
            simObject
        } catch (e: java.lang.Exception) {
            simObject = HashMap<String, String?>()
            simObject["error"] = e.toString()
            simObject
        }
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            666
        )
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (telephonyManager.simState == TelephonyManager.SIM_STATE_ABSENT) {
                simObject = HashMap<String, String?>()
                simObject["message"] = "please insert your sim card"
                eventSink?.success(simObject)
            } else {
                if (ActivityCompat.checkSelfPermission(
                       activity,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0)
                    eventSink?.success(getSimData())
                }
            }
        } catch (e: Exception) {
            simObject = HashMap<String, String?>()
            simObject["message"] = "Please wait....."
            eventSink?.success(simObject)
        }
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        subscriptionManager = activity.getSystemService(SubscriptionManager::class.java)
        telephonyManager =
            activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        eventSink = events
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (telephonyManager.simState == TelephonyManager.SIM_STATE_ABSENT) {
                simObject = HashMap<String, String?>()
                simObject["message"] = "please insert your sim card"
                eventSink?.success(simObject)
            } else {
                subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0)
                eventSink?.success(getSimData())
            }
        } else {
            requestPermission()
        }
    }

    override fun onCancel(arguments: Any?) {
//        eventSink = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == 666) {
            return if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0)
                    eventSink?.success(getSimData())
                }
                true
            } else {
                requestPermission()
                false
            }
        }
        return false
    }
}

