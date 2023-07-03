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
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject
import java.io.*
import java.util.*


/** SimdecPlugin */
class SimdecPlugin : FlutterPlugin, ActivityAware, EventChannel.StreamHandler,
    BroadcastReceiver() {
    private lateinit var channel: MethodChannel
    var context: Context? = null
    private lateinit var activity: Activity
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventSink? = null
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "simListen")

        eventChannel.setStreamHandler(this)
        IntentFilter("android.intent.action.SIM_STATE_CHANGED").also {
            context?.registerReceiver(this, it)
        }

    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
       try {
           if (checkPermission()) {
               if (telephonyManager.simState==TelephonyManager.SIM_STATE_ABSENT){
                   eventSink?.success("Please insert your sim card")
               }else{
                   eventSink?.success(getSimData())
               }
           } else {
               requestPermission()
               if (checkPermission()) {
                   if (telephonyManager.simState == TelephonyManager.SIM_STATE_ABSENT) {
                       eventSink?.success("Please insert your sim card")
                   } else {
                       eventSink?.success(getSimData())
                   }
               }
           }
       }catch (e:Exception){
           eventSink?.success("${e.message}")
       }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    @SuppressLint("HardwareIds")
    private fun getSimData(): String {
        val jsonObject = JSONObject()
        return try {
            subscriptionManager = activity.getSystemService(SubscriptionManager::class.java)
            telephonyManager = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val s = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0)
            // jsonObject.put("operator_name", s.carrierName)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                jsonObject.put("ssn", telephonyManager.simSerialNumber)
            } else {
                val unix = "${
                    Secure.getString(
                        context?.contentResolver,
                        Secure.ANDROID_ID
                    )
                }${s?.subscriptionId}"
                jsonObject.put(
                    "sim_id",
                    String(unix.toByteArray())
                )
            }
            jsonObject.toString()
        } catch (se: SecurityException) {
            jsonObject.put("error_security", se.toString()).toString()

        } catch (e: java.lang.Exception) {
            jsonObject.put("error", e.toString()).toString()
        }
    }


    private fun requestPermission() {
        val perm = arrayOf(Manifest.permission.READ_PHONE_STATE)
        ActivityCompat.requestPermissions(activity, perm, 666)
    }

    private fun checkPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_PHONE_STATE,
        )
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        eventSink = events
        events?.success(getSimData())
    }

    override fun onCancel(arguments: Any?) {
//        eventSink = null
    }
}

