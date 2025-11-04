package com.spark.ui.steps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.libraries.wear.companion.setup.SetupStep
import com.google.android.libraries.wear.companion.setup.StepCompletionProvider

/**
 * 位置权限申请步骤
 */
class LocationPermissionStep(private val context: Context) : SetupStep {
    
    private var stepCompletionProvider: StepCompletionProvider? = null
    
    override fun isAvailable(): Boolean = true
    
    override fun onStepStarted(stepCompletionProvider: StepCompletionProvider) {
        this.stepCompletionProvider = stepCompletionProvider
        Log.d("LocationPermissionStep", "开始申请位置权限")
        
        // 检查权限是否已授予
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            Log.d("LocationPermissionStep", "位置权限已授予，自动进入下一步")
            stepCompletionProvider.finish()
        } else {
            Log.d("LocationPermissionStep", "等待用户授予位置权限")
            // 权限申请由Fragment处理，这里只记录日志
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
        Log.d("LocationPermissionStep", "步骤停止")
        stepCompletionProvider = null
    }
    
    override fun cleanup() {
        Log.d("LocationPermissionStep", "清理资源")
        stepCompletionProvider = null
    }
}

