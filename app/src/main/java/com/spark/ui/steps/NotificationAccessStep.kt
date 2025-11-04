package com.spark.ui.steps

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.android.libraries.wear.companion.setup.SetupStep
import com.google.android.libraries.wear.companion.setup.StepCompletionProvider

/**
 * 通知访问权限步骤
 */
class NotificationAccessStep(private val context: Context) : SetupStep {
    
    private var stepCompletionProvider: StepCompletionProvider? = null
    
    override fun isAvailable(): Boolean = true
    
    override fun onStepStarted(stepCompletionProvider: StepCompletionProvider) {
        this.stepCompletionProvider = stepCompletionProvider
        Log.d("NotificationAccessStep", "开始设置通知访问权限")
        
        // 检查通知访问权限是否已授予
        val hasAccess = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )?.contains(context.packageName) == true
        
        if (hasAccess) {
            Log.d("NotificationAccessStep", "通知访问权限已授予，自动进入下一步")
            stepCompletionProvider.finish()
        } else {
            Log.d("NotificationAccessStep", "等待用户授予通知访问权限")
            // 权限设置由Fragment处理
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
        Log.d("NotificationAccessStep", "步骤停止")
        stepCompletionProvider = null
    }
    
    override fun cleanup() {
        Log.d("NotificationAccessStep", "清理资源")
        stepCompletionProvider = null
    }
}

