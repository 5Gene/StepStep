package org.spark.stepstep

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Step引擎
 * 
 * 负责管理整个Step流程的执行
 * 维护步骤的执行顺序和状态
 */
class StepEngine internal constructor(
    private val steps: List<StepStep>
) {
    
    companion object {
        private const val TAG = "StepEngine"
    }
    
    // 当前步骤索引，-1表示未开始
    private var currentStepIndex: Int = -1
    
    // 执行历史栈，用于navigateBack
    private val executionStack = mutableListOf<Int>()
    
    // 步骤变化的数据流
    private val _stepChangeFlow = MutableStateFlow<StepChange?>(null)
    
    /**
     * 获取步骤变化的数据流
     * 外部可以通过此数据流监听步骤的变化
     */
    fun getStepChangeFlow(): StateFlow<StepChange?> = _stepChangeFlow.asStateFlow()
    
    /**
     * 获取当前步骤
     */
    fun getCurrentStep(): StepStep? {
        return if (currentStepIndex in steps.indices) {
            steps[currentStepIndex]
        } else {
            null
        }
    }
    
    /**
     * 获取所有步骤
     */
    fun getAllSteps(): List<StepStep> = steps.toList()
    
    /**
     * 启动Step流程
     */
    fun start() {
        if (currentStepIndex != -1) {
            throw IllegalStateException("Step engine already started")
        }
        
        if (steps.isEmpty()) {
            // 没有步骤，直接完成
            notifyCompleted()
            return
        }
        
        // 开始第一个步骤
        moveToNextAvailableStep(ChangeType = StepChange.ChangeType.STARTED)
    }
    
    /**
     * 中止Step流程
     * 
     * @param fromUser 是否由用户主动触发
     */
    fun abort(fromUser: Boolean = true) {
        // 清理所有步骤
        steps.forEach { it.cleanup() }
        
        val previousStep = getCurrentStep()
        currentStepIndex = -1
        executionStack.clear()
        
        // 通知流程已中止
        _stepChangeFlow.value = StepChange(
            currentStep = null,
            previousStep = previousStep,
            currentIndex = -1,
            totalSteps = steps.size,
            changeType = StepChange.ChangeType.ABORTED
        )
    }
    
    /**
     * 完成当前步骤，进入下一步
     */
    internal fun finishCurrentStep() {
        val current = getCurrentStep()
        if (current == null) {
            return
        }
        
        // 停止当前步骤
        current.onStepStopped()
        
        // 记录执行历史
        executionStack.add(currentStepIndex)
        
        // 移动到下一个可用步骤
        moveToNextAvailableStep(ChangeType = StepChange.ChangeType.FORWARD)
    }
    
    /**
     * 返回上一步
     */
    internal fun navigateBackToPreviousStep() {
        val current = getCurrentStep()
        if (current == null) {
            return
        }
        
        // 停止当前步骤
        current.onStepStopped()
        
        if (executionStack.isEmpty()) {
            // 已经是第一个步骤，中止流程
            abort(fromUser = true)
            return
        }
        
        // 从历史栈中获取上一个步骤
        val previousIndex = executionStack.removeAt(executionStack.lastIndex)
        val previousStep = steps[previousIndex]
        
        val oldStep = getCurrentStep()
        currentStepIndex = previousIndex
        
        // 恢复上一个步骤
        previousStep.onStepResumed(StepCompletionProviderImpl())
        
        // 通知步骤变化
        _stepChangeFlow.value = StepChange(
            currentStep = previousStep,
            previousStep = oldStep,
            currentIndex = currentStepIndex,
            totalSteps = steps.size,
            changeType = StepChange.ChangeType.BACKWARD
        )
    }
    
    /**
     * 移动到下一个可用的步骤
     */
    private fun moveToNextAvailableStep(ChangeType: StepChange.ChangeType) {
        val previousStep = getCurrentStep()
        var nextIndex = currentStepIndex + 1
        
        // 查找下一个可用的步骤
        while (nextIndex < steps.size) {
            val nextStep = steps[nextIndex]
            if (nextStep.isAvailable()) {
                // 找到可用步骤
                currentStepIndex = nextIndex
                
                // 启动步骤
                nextStep.onStepStarted(StepCompletionProviderImpl())
                
                // 通知步骤变化
                _stepChangeFlow.value = StepChange(
                    currentStep = nextStep,
                    previousStep = previousStep,
                    currentIndex = currentStepIndex,
                    totalSteps = steps.size,
                    changeType = ChangeType
                )
                return
            }
            nextIndex++
        }
        
        // 没有更多可用步骤，流程完成
        notifyCompleted()
    }
    
    /**
     * 通知流程完成
     */
    private fun notifyCompleted() {
        val previousStep = getCurrentStep()
        
        // 清理所有步骤
        steps.forEach { it.cleanup() }
        
        currentStepIndex = -1
        executionStack.clear()
        
        // 通知流程已完成
        _stepChangeFlow.value = StepChange(
            currentStep = null,
            previousStep = previousStep,
            currentIndex = -1,
            totalSteps = steps.size,
            changeType = StepChange.ChangeType.COMPLETED
        )
    }
    
    /**
     * StepCompletionProvider的实现
     */
    private inner class StepCompletionProviderImpl : StepCompletionProvider {
        override fun finish() {
            finishCurrentStep()
        }
        
        override fun navigateBack() {
            navigateBackToPreviousStep()
        }
        
        override fun abortStep(fromUser: Boolean) {
            abort(fromUser)
        }
    }
}

