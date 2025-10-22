package org.spark.stepstep

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CompletableDeferred

/**
 * Step引擎
 * 
 * 负责管理整个Step流程的执行
 * 维护步骤的执行顺序和状态
 * 支持协程和泛型数据传递
 */
class StepEngine<T> internal constructor(
    private val steps: MutableList<StepStep<T>>
) {
    
    companion object {
        private const val TAG = "StepEngine"
    }
    
    // 当前步骤索引，-1表示未开始
    private var currentStepIndex: Int = -1
    
    // 执行历史栈，用于navigateBack
    private val executionStack = mutableListOf<Int>()
    
    // 步骤变化的数据流
    private val _stepChangeFlow = MutableStateFlow<StepChange<T>?>(null)
    
    // 数据传递容器
    private val dataContainer = mutableMapOf<String, Any?>()
    
    // 泛型数据
    private var genericData: T? = null
    
    // 协程互斥锁，确保线程安全
    private val mutex = Mutex()
    
    // 流程结果回调
    private var onSuccess: ((T?) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    
    // 流程完成信号
    private var completionDeferred: CompletableDeferred<T?>? = null
    
    /**
     * 获取步骤变化的数据流
     * 外部可以通过此数据流监听步骤的变化
     */
    fun getStepChangeFlow(): StateFlow<StepChange<T>?> = _stepChangeFlow.asStateFlow()
    
    /**
     * 获取当前步骤
     */
    fun getCurrentStep(): StepStep<T>? = 
        steps.getOrNull(currentStepIndex)
    
    /**
     * 获取所有步骤
     */
    fun getAllSteps(): List<StepStep<T>> = steps.toList()
    
    /**
     * 获取步骤总数
     */
    fun getStepCount(): Int = steps.size
    
    /**
     * 设置数据
     */
    fun setData(key: String, value: Any?) {
        dataContainer[key] = value
    }
    
    /**
     * 获取数据
     */
    fun getData(key: String): Any? = dataContainer[key]
    
    /**
     * 获取泛型数据
     */
    fun getGenericData(): T? = genericData
    
    /**
     * 设置泛型数据
     */
    fun setGenericData(data: T?) {
        genericData = data
    }
    
    /**
     * 动态添加步骤
     */
    suspend fun addStep(step: StepStep<T>) = mutex.withLock {
        steps.add(step)
    }
    
    /**
     * 动态移除步骤
     */
    suspend fun removeStep(stepId: String) = mutex.withLock {
        steps.removeAll { it.getStepId() == stepId }
    }
    
    /**
     * 动态插入步骤到指定位置
     */
    suspend fun insertStep(index: Int, step: StepStep<T>) = mutex.withLock {
        if (index in 0..steps.size) {
            steps.add(index, step)
        }
    }
    
    /**
     * 设置成功回调
     */
    fun onSuccess(callback: (T?) -> Unit): StepEngine<T> {
        this.onSuccess = callback
        return this
    }
    
    /**
     * 设置错误回调
     */
    fun onError(callback: (Throwable) -> Unit): StepEngine<T> {
        this.onError = callback
        return this
    }
    
    /**
     * 启动Step流程（协程版本）
     * 
     * @param initialData 初始数据
     */
    suspend fun start(initialData: T? = null) = withContext(Dispatchers.Main) {
        // 设置初始数据
        if (initialData != null) {
            setGenericData(initialData)
        }
        
        if (currentStepIndex != -1) {
            throw IllegalStateException("Step engine already started")
        }
        
        if (steps.isEmpty()) {
            // 没有步骤，直接完成
            notifyCompleted()
            return@withContext
        }
        
        try {
            // 创建完成信号
            completionDeferred = CompletableDeferred()
            
            // 开始第一个步骤
            moveToNextAvailableStep(StepChange.ChangeType.STARTED)
            
            // 等待流程完成
            completionDeferred?.await()
        } catch (e: Exception) {
            handleError(e)
            throw e
        }
    }
    
    /**
     * 处理错误
     */
    private suspend fun handleError(throwable: Throwable) {
        onError?.invoke(throwable)
        abort(fromUser = false)
    }
    
    /**
     * 清理所有步骤
     */
    private suspend fun cleanupAllSteps() {
        steps.forEach { 
            it.cleanup()
        }
    }
    
    /**
     * 中止Step流程
     * 
     * @param fromUser 是否由用户主动触发
     */
    suspend fun abort(fromUser: Boolean = true) {
        cleanupAllSteps()
        
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
        
        // 完成等待
        completionDeferred?.complete(null)
    }
    
    /**
     * 完成当前步骤，进入下一步
     */
    internal suspend fun finishCurrentStep() {
        val current = getCurrentStep()
        if (current == null) {
            return
        }
        
        // 停止当前步骤
        current.onStepStopped()
        
        // 记录执行历史
        executionStack.add(currentStepIndex)
        
        // 移动到下一个可用步骤
        moveToNextAvailableStep(StepChange.ChangeType.FORWARD)
    }
    
    /**
     * 返回上一步
     */
    internal suspend fun navigateBackToPreviousStep() {
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
     * 
     * 只负责找到并启动下一个可用步骤，不循环执行
     * 步骤的执行由StepCompletionProvider控制
     */
    private suspend fun moveToNextAvailableStep(changeType: StepChange.ChangeType) {
        val previousStep = getCurrentStep()
        var nextIndex = currentStepIndex + 1
        
        // 查找下一个可用的步骤
        while (nextIndex < steps.size) {
            val nextStep = steps[nextIndex]
            if (nextStep.isAvailable()) {
                // 找到可用步骤，启动它
                startStep(nextStep, nextIndex, previousStep, changeType)
                return
            }
            nextIndex++
        }
        
        // 没有更多可用步骤，流程完成
        notifyCompleted()
    }
    
    /**
     * 启动指定步骤
     */
    private suspend fun startStep(
        step: StepStep<T>, 
        index: Int, 
        previousStep: StepStep<T>?, 
        changeType: StepChange.ChangeType
    ) {
        currentStepIndex = index
        
        // 启动步骤
        step.onStepStarted(StepCompletionProviderImpl())
        
        // 通知步骤变化
        _stepChangeFlow.value = StepChange(
            currentStep = step,
            previousStep = previousStep,
            currentIndex = currentStepIndex,
            totalSteps = steps.size,
            changeType = changeType
        )
    }
    
    /**
     * 通知流程完成
     */
    private suspend fun notifyCompleted() {
        val previousStep = getCurrentStep()
        
        cleanupAllSteps()
        
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
        
        // 调用成功回调
        onSuccess?.invoke(null)
        
        // 完成等待
        completionDeferred?.complete(null)
    }
    
    /**
     * StepCompletionProvider的实现
     */
    private inner class StepCompletionProviderImpl : StepCompletionProvider<T> {
        override suspend fun finish() {
            finishCurrentStep()
        }
        
        override suspend fun navigateBack() {
            navigateBackToPreviousStep()
        }
        
        override suspend fun abortStep(fromUser: Boolean) {
            abort(fromUser)
        }
        
        override suspend fun error(exception: Throwable) {
            handleError(exception)
        }
        
        override fun getData(): T? = genericData
        
        override fun setData(data: T?) {
            genericData = data
        }
        
        override suspend fun addStepAfter(targetStepId: String, step: StepStep<T>) {
            mutex.withLock {
                val targetIndex = steps.indexOfFirst { it.getStepId() == targetStepId }
                if (targetIndex >= 0) {
                    steps.add(targetIndex + 1, step)
                } else {
                    // 如果找不到目标步骤，添加到末尾
                    steps.add(step)
                }
            }
        }
        
        override suspend fun addStepBefore(targetStepId: String, step: StepStep<T>) {
            mutex.withLock {
                val targetIndex = steps.indexOfFirst { it.getStepId() == targetStepId }
                if (targetIndex >= 0) {
                    steps.add(targetIndex, step)
                } else {
                    // 如果找不到目标步骤，添加到开头
                    steps.add(0, step)
                }
            }
        }
        
        override suspend fun addStep(step: StepStep<T>) {
            mutex.withLock {
                steps.add(step)
            }
        }
    }
}