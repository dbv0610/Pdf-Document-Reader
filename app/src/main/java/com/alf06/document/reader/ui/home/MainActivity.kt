package com.alf06.document.reader.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivityMainBinding
import com.alf06.document.reader.ui.dialog.DialogPermission
import com.alf06.document.reader.utils.PreferenceHelper
import com.alf06.document.reader.viewmodel.DocumentViewModel
import com.ui.baselib.base.BaseActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private val preferenceHelper : PreferenceHelper by  inject()
    private var isCheckedUpdate = false
    override val fullStatus: Boolean
        get() = true

    private val viewModel: DocumentViewModel by viewModel()
    private var storagePermissionDialog: DialogPermission? = null
    private val storagePermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
    private val notificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { showStoragePermissionDialogIfNeeded() }
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        showStoragePermissionDialogIfNeeded()
    }
    private val storageSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        showStoragePermissionDialogIfNeeded()
    }

    override fun backPressed() {
    }

    override fun initialize() {
    }

    override fun ActivityMainBinding.setData() {
        requestNotificationPermission()
        setupNavigation()
    }

    fun setupNavigation() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHost.navController
        binding.navBar.setOnItemSelectedListener { item, _ ->
            val currentDestinationId = navController.currentDestination?.id
            if (currentDestinationId == item.id) {
                return@setOnItemSelectedListener
            }
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.findStartDestination().id, inclusive = false, saveState = true)
                .build()
            navController.navigate(item.id, null, options)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.navBar.isVisible = when (destination.id) {
                R.id.homeFragment,
                R.id.storageFragment,
                R.id.toolsFragment,-> true
                else -> false
            }
            binding.navBar.selectItemById(destination.id, triggerListener = false)
        }
    }

    override fun ActivityMainBinding.onClick() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("InlinedApi")
    private fun requestNotificationPermission() {
        if (!isNotificationGranted()) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showStoragePermissionDialogIfNeeded()
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            storagePermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun showStoragePermissionDialogIfNeeded() {
        if (isFinishing || isDestroyed) return
        if (hasStoragePermission()) {
            viewModel.loadDocuments(this)
            return
        }
        if (storagePermissionDialog?.isShowing == true) return

        storagePermissionDialog = DialogPermission(this) {
            requestStoragePermission()
        }.also { dialog ->
            dialog.setOnDismissListener { storagePermissionDialog = null }
            dialog.show()
        }
    }

    private fun requestStoragePermission() {
        if (hasStoragePermission()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                storageSettingsLauncher.launch(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    },
                )
            } catch (_: ActivityNotFoundException) {
                storageSettingsLauncher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else if (shouldOpenStorageSettings()) {
            storageSettingsLauncher.launch(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")),
            )
        } else {
            storagePermissionLauncher.launch(storagePermissions)
        }
    }

    private fun shouldOpenStorageSettings(): Boolean {
        return storagePermissions.any { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED &&
                !shouldShowRequestPermissionRationale(permission)
        }
    }

    override fun onDestroy() {
        storagePermissionDialog?.setOnDismissListener(null)
        storagePermissionDialog?.dismiss()
        storagePermissionDialog = null
        super.onDestroy()
    }


    private fun isNotificationGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        return true
    }
}
