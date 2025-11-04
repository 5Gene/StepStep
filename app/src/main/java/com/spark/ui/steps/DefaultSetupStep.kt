package com.spark.ui.steps

import android.content.Context
import android.util.Log
import com.google.android.libraries.wear.companion.setup.SetupStep
import com.google.android.libraries.wear.companion.setup.StepCompletionProvider
import com.google.android.libraries.wear.companion.setup.StepType

/**
 * 默认Setup步骤
 */
class DefaultSetupStep(
    private val context: Context,
    private val stepType: StepType
) : SetupStep {
    
    override fun isAvailable(): Boolean = true
    
    override fun onStepStarted(stepCompletionProvider: StepCompletionProvider) {
        Log.d("DefaultSetupStep", "开始执行步骤: $stepType")
        stepCompletionProvider.finish()
    }
    
    override fun onStepStopped() {
        Log.d("DefaultSetupStep", "步骤停止: $stepType")
    }
    
    override fun cleanup() {
        Log.d("DefaultSetupStep", "清理资源: $stepType")
    }
}

