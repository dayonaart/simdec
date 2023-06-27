package id.bni46.simdec

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONObject


/** SimdecPlugin */
class SimdecPlugin: FlutterPlugin, MethodCallHandler,ActivityAware {
  private lateinit var channel : MethodChannel
  var context: Context? = null
  var activity: Activity? = null
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "simdec")
    channel.setMethodCallHandler(this)
    context=flutterPluginBinding.applicationContext
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getSimcard") {
      if (checkPermission()) {
        result.success(getSimData())
      } else {
        requestPermission()
        result.success(getSimData())
      }
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
  @SuppressLint("HardwareIds")
  private fun getSimData(): String {
    val jsonObject = JSONObject()
    val manager: TelephonyManager =
      context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
     try {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        val subscriptionManager = activity?.getSystemService(SubscriptionManager::class.java)
//        val subIds=(0..subscriptionManager?.activeSubscriptionInfoCount.let { 0 }).map {
//          subscriptionManager?.getSubscriptionIds(it)
//        }
//        jsonObject.put("subId", subIds)
        jsonObject.put("uniq_id",getDeviceId())
        jsonObject.put("simState", manager.simState)
        jsonObject.put("simOperator", manager.simOperator)
        jsonObject.put("simCarrierId", manager.simCarrierId)
        jsonObject.put("simOperatorName", manager.simCarrierIdName)
        jsonObject.put("phoneNumber", manager.line1Number)
      }else{
        jsonObject.put("simSerialNumber", manager.simSerialNumber)
        jsonObject.put("simOperator", manager.simOperator)
        jsonObject.put("phoneNumber", manager.line1Number)
      }
       return jsonObject.toString()
     }catch (se:SecurityException){
       return  jsonObject.put("error_security",se.toString()).toString()

     }
     catch (e: java.lang.Exception) {
    return  jsonObject.put("error",e.toString()).toString()
    }
  }

  private fun getManifestPermission(): List<String> {
return if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
  listOf(
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.READ_SMS,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.READ_PHONE_NUMBERS
  )
}else{
  listOf(
    Manifest.permission.READ_SMS,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION,
  )
}
  }

  private fun requestPermission() {
    val perm: Array<String> = if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
      arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_NUMBERS
      )
    }else{
      arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
    ActivityCompat.requestPermissions(activity!!, perm, 0)
  }

  private fun checkPermission(): Boolean {
    val b= mutableListOf<Boolean>()
    for(p in getManifestPermission()) {
     val c=  PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
        activity!!,
        p
      )
      b.add(c)
    }
    return !b.contains(false)
  }

  @SuppressLint("HardwareIds")
  fun getDeviceId(): String {
    val deviceId: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
    } else {
      val mTelephony = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (context?.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
          return ""
        }
      }
      if (mTelephony.deviceId != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          mTelephony.imei
        } else {
          mTelephony.deviceId
        }
      } else {
        Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
      }
    }
    return deviceId
  }
  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
   activity=binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
  }

  override fun onDetachedFromActivity() {
  }
}
