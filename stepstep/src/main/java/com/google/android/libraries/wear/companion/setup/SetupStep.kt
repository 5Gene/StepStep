package com.google.android.libraries.wear.companion.setup

/**
 * Setup Step - 表示Setup流程中的一个步骤
 */
interface SetupStep {
    
    /**
     * 检查步骤是否可用
     */
    fun isAvailable(): Boolean

    /**
     * 当步骤开始时调用
     * @param stepCompletionProvider 步骤完成提供者
     */
    fun onStepStarted(stepCompletionProvider: StepCompletionProvider)

    /**
     * 当步骤停止时调用
     */
    fun onStepStopped() {
        // 默认空实现
    }

    /**
     * 清理步骤资源
     */
    fun cleanup() {
        // 默认空实现
    }
}

