package com.margelo.nitro.brightness

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise
import kotlin.math.max
import kotlin.math.min

@DoNotStrip
class NitroBrightness : HybridNitroBrightnessSpec() {
  companion object {
    private const val TAG = "NitroBrightness"
    private const val MIN_BRIGHTNESS = 0.0
    private const val MAX_BRIGHTNESS = 1.0
    private const val DEFAULT_BRIGHTNESS = 128
    private const val BRIGHTNESS_SCALE = 255f
  }

  private val context = NitroModules.applicationContext ?: throw Exception("Context is null")
  private val activity: Activity?
    get() = context.currentActivity as? Activity

  private var originalBrightness: Float = try {
    Settings.System.getInt(
      context.contentResolver,
      Settings.System.SCREEN_BRIGHTNESS,
      DEFAULT_BRIGHTNESS
    ) / BRIGHTNESS_SCALE
  } catch (e: Exception) {
    Log.w(TAG, "Failed to get original brightness", e)
    DEFAULT_BRIGHTNESS / BRIGHTNESS_SCALE
  }

  private val listeners = mutableListOf<(Double) -> Unit>()
  private val listenerIds = mutableMapOf<Int, (Double) -> Unit>()
  private var nextListenerId = 1

  // MARK: - Brightness Listener Management

  override fun addBrightnessListener(listener: (Double) -> Unit): Double {
    synchronized(listeners) {
      val id = nextListenerId++
      listenerIds[id] = listener
      listeners.add(listener)
      return id.toDouble()
    }
  }

  override fun removeBrightnessListener(listenerId: Double) {
    synchronized(listeners) {
      val id = listenerId.toInt()
      listenerIds[id]?.let { listener ->
        listeners.remove(listener)
        listenerIds.remove(id)
      }
    }
  }

  private fun notifyListeners(value: Double) {
    synchronized(listeners) {
      listeners.forEach { listener ->
        try {
          listener(value)
        } catch (e: Exception) {
          Log.e(TAG, "Error notifying brightness listener", e)
        }
      }
    }
  }

  // MARK: - Brightness Control Methods

  override fun getBrightness(): Double {
    return try {
      val currentActivity = activity
      if (currentActivity != null) {
        val lp = currentActivity.window?.attributes
        val brightness = lp?.screenBrightness ?: -1f
        if (brightness >= 0) {
          brightness.toDouble()
        } else {
          // Fallback to system brightness
          getSystemBrightness()
        }
      } else {
        getSystemBrightness()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error getting brightness", e)
      -1.0
    }
  }

  override fun getBrightnessPermissions(): Boolean {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.System.canWrite(context)
      } else {
        true // Pre-M devices don't need this permission
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error checking brightness permissions", e)
      false
    }
  }

  override fun requestBrightnessPermissions(): Promise<Boolean> {
    return Promise.async {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          if (!Settings.System.canWrite(context)) {
            // Launch settings to request permission
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
              data = Uri.parse("package:${context.packageName}")
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
          }
          Settings.System.canWrite(context)
        } else {
          true
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error requesting brightness permissions", e)
        false
      }
    }
  }

  override fun getSystemBrightness(): Double {
    return try {
      val value = Settings.System.getInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS,
        DEFAULT_BRIGHTNESS
      )
      (value / BRIGHTNESS_SCALE).toDouble()
    } catch (e: Exception) {
      Log.e(TAG, "Error getting system brightness", e)
      (DEFAULT_BRIGHTNESS / BRIGHTNESS_SCALE).toDouble()
    }
  }

  override fun getSystemBrightnessMode(): String {
    return try {
      val mode = Settings.System.getInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS_MODE,
        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
      )
      if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
        "automatic"
      } else {
        "manual"
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error getting brightness mode", e)
      "manual"
    }
  }

  override fun isAvailable(): Boolean = true

  override fun restoreSystemBrightness() {
    try {
      setSystemBrightness(originalBrightness.toDouble())
    } catch (e: Exception) {
      Log.e(TAG, "Error restoring system brightness", e)
    }
  }

  override fun setBrightness(value: Double) {
    try {
      val clampedValue = max(MIN_BRIGHTNESS, min(MAX_BRIGHTNESS, value))
      val currentActivity = activity

      if (currentActivity != null) {
        currentActivity.runOnUiThread {
          try {
            val lp = currentActivity.window.attributes
            lp.screenBrightness = clampedValue.toFloat()
            currentActivity.window.attributes = lp
            notifyListeners(clampedValue)
          } catch (e: Exception) {
            Log.e(TAG, "Error setting window brightness", e)
          }
        }
      } else {
        Log.w(TAG, "No activity available to set brightness")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error setting brightness", e)
    }
  }

  override fun setSystemBrightness(value: Double) {
    try {
      val clampedValue = max(MIN_BRIGHTNESS, min(MAX_BRIGHTNESS, value))

      if (getBrightnessPermissions()) {
        Settings.System.putInt(
          context.contentResolver,
          Settings.System.SCREEN_BRIGHTNESS,
          (clampedValue * BRIGHTNESS_SCALE).toInt()
        )
        notifyListeners(clampedValue)
      } else {
        Log.w(TAG, "No permission to set system brightness")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error setting system brightness", e)
    }
  }

  // MARK: - Memory Management

//  override fun getMemorySize(): Long {
//    return super.getMemorySize() + (listeners.size * 32L) // Approximate size per listener
//  }

  override fun dispose() {
    synchronized(listeners) {
      listeners.clear()
      listenerIds.clear()
    }
    super.dispose()
  }
}
