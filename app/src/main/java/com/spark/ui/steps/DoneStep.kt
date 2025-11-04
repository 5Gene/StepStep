package com.spark.ui.steps

import android.content.Context
import android.util.Log
import com.google.android.libraries.wear.companion.setup.SetupStep
import com.google.android.libraries.wear.companion.setup.StepCompletionProvider

/**
 * 完成步骤
 */
class DoneStep(private val context: Context) : SetupStep {
    
    private var stepCompletionProvider: StepCompletionProvider? = null
    
    override fun isAvailable(): Boolean = true
    
    override fun onStepStarted(stepCompletionProvider: StepCompletionProvider) {
        this.stepCompletionProvider = stepCompletionProvider
        Log.d("DoneStep", "Setup流程完成！")
        // 完成步骤，自动结束
        stepCompletionProvider.finish()
    }
    
    /**
     * 进入下一步（已完成，无需操作）
     */
    fun goToNext() {
        // 已完成，无需操作
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
        Log.d("DoneStep", "步骤停止")
        stepCompletionProvider = null
    }
    
    override fun cleanup() {
        Log.d("DoneStep", "清理资源")
        stepCompletionProvider = null
    }
}

