package com.spark.ui.steps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.libraries.wear.companion.setup.SetupStep
import com.google.android.libraries.wear.companion.setup.StepCompletionProvider

/**
 * 蓝牙权限申请步骤
 */
class BluetoothPermissionStep(private val context: Context) : SetupStep {
    
    private var stepCompletionProvider: StepCompletionProvider? = null
    
    override fun isAvailable(): Boolean = true
    
    override fun onStepStarted(stepCompletionProvider: StepCompletionProvider) {
        this.stepCompletionProvider = stepCompletionProvider
        Log.d("BluetoothPermissionStep", "开始申请蓝牙权限")
        
        // 检查权限是否已授予
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            Log.d("BluetoothPermissionStep", "蓝牙权限已授予，自动进入下一步")
            stepCompletionProvider.finish()
        } else {
            Log.d("BluetoothPermissionStep", "等待用户授予蓝牙权限")
            // 权限申请由Fragment处理
        }
    }
    
    /**
     * 进入下一步
     */
    fun goToNext() {
        stepCompletionProvider?.finish()
    }
    
    /**
     * 返回上一步
     */
    fun goToPrevious(): Boolean {
        return stepCompletionProvider?.navigateBack() ?: false
    }
    
    /**
     * 获取StepCompletionProvider（用于ViewModel）
     */
    fun getStepCompletionProvider(): StepCompletionProvider? = stepCompletionProvider
    
    override fun onStepStopped() {
        Log.d("BluetoothPermissionStep", "步骤停止")
        stepCompletionProvider = null
    }
    
    override fun cleanup() {
        Log.d("BluetoothPermissionStep", "清理资源")
        stepCompletionProvider = null
    }
}

