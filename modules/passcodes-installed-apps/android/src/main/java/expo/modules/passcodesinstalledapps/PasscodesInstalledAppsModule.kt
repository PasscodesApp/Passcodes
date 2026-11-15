package expo.modules.passcodesinstalledapps

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Base64
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import java.io.ByteArrayOutputStream

/** One installed app, as sent to JavaScript. */
class InstalledApplication : Record {
  @Field
  var packageName: String = ""

  /** The name shown under the app's icon, e.g. "Instagram". */
  @Field
  var label: String = ""

  @Field
  var isSystemApp: Boolean = false
}

/**
 * Lets the app list the installed apps (name + package name) and read one
 * app's name or icon, so a password can be linked to an app.
 *
 * Only apps with a launcher icon are visible (see AndroidManifest.xml). Every
 * function is asynchronous and runs off the JavaScript thread.
 */
class PasscodesInstalledAppsModule : Module() {
  private val packageManager: PackageManager
    get() = (appContext.reactContext ?: throw Exceptions.ReactContextLost()).packageManager

  override fun definition() = ModuleDefinition {
    Name("PasscodesInstalledApps")

    // All apps with a launcher icon, sorted by name (case-insensitive).
    AsyncFunction("getInstalledApplications") {
      val pm = packageManager

      launchableActivities(pm)
        .mapNotNull { info ->
          val activity = info.activityInfo ?: return@mapNotNull null

          InstalledApplication().apply {
            packageName = activity.packageName
            label = info.loadLabel(pm).toString().ifBlank { activity.packageName }
            isSystemApp = (activity.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
          }
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
    }

    // The name of one app, or null when it isn't installed / isn't visible.
    AsyncFunction("getApplicationLabel") { packageName: String ->
      val pm = packageManager

      try {
        pm.getApplicationLabel(applicationInfoOf(pm, packageName)).toString()
      } catch (_: PackageManager.NameNotFoundException) {
        null
      }
    }

    // One app's icon as a PNG data URI (usable as an Image source), or null.
    AsyncFunction("getApplicationIcon") { packageName: String, sizePx: Int ->
      val pm = packageManager

      try {
        drawableToPngDataUri(pm.getApplicationIcon(packageName), sizePx.coerceIn(16, 256))
      } catch (_: PackageManager.NameNotFoundException) {
        null
      }
    }
  }

  private fun launchableActivities(pm: PackageManager): List<ResolveInfo> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
    } else {
      @Suppress("DEPRECATION")
      pm.queryIntentActivities(intent, 0)
    }
  }

  private fun applicationInfoOf(pm: PackageManager, packageName: String): ApplicationInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
    } else {
      @Suppress("DEPRECATION")
      pm.getApplicationInfo(packageName, 0)
    }
  }

  /** Works for every drawable type, including adaptive icons. */
  private fun drawableToPngDataUri(drawable: Drawable, size: Int): String {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    try {
      drawable.setBounds(0, 0, size, size)
      drawable.draw(Canvas(bitmap))

      val output = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)

      return "data:image/png;base64," + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    } finally {
      bitmap.recycle()
    }
  }
}
