package com.chemscanner.omniscient.marrow.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsManager @Inject constructor() {

    sealed class PermissionStatus {
        object Granted : PermissionStatus()
        object Denied : PermissionStatus()
        data class PermanentlyDenied(val permissions: List<String>) : PermissionStatus()
    }

    fun checkPermissions(context: Context, permissions: List<String>): PermissionStatus {
        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        return when {
            deniedPermissions.isEmpty() -> PermissionStatus.Granted
            else -> {
                val shouldShowRationale = deniedPermissions.any {
                    (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true
                }
                if (shouldShowRationale) {
                    PermissionStatus.Denied
                } else {
                    PermissionStatus.PermanentlyDenied(deniedPermissions)
                }
            }
        }
    }

    fun requestPermissions(
        fragment: Fragment,
        permissions: List<String>,
        onResult: (Map<String, Boolean>) -> Unit
    ): ActivityResultLauncher<Array<String>> {
        Timber.d("Registering permission launcher for: $permissions")
        return fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            onResult(results)
        }
    }

    fun getRequiredPermissions(): List<String> {
        return listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun getCameraPermissions(): List<String> {
        return listOf(Manifest.permission.CAMERA)
    }

    fun getStoragePermissions(): List<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }
}
