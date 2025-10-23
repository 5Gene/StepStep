package org.spark.stepstep

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers

/**
 * Step引擎 - 核心执行引擎
 * 
 * 负责管理整个Step流程的执行，是整个框架的核心组件
 * 维护步骤的执行顺序和状态，支持协程和泛型数据传递
 * 
 * 设计理念：
 * 1. 线程安全：使用Mutex确保多线程环境下的安全性
 * 2. 状态管理：通过StateFlow提供响应式的状态变化通知
 * 3. 生命周期：完整管理步骤的生命周期（开始、恢复、停止、清理）
 * 4. 数据传递：支持泛型数据和键值对数据两种传递方式
 * 5. 动态管理：支持运行时动态添加、移除步骤
 * 
 * 为什么使用internal constructor？
 * 防止外部直接创建StepEngine，必须通过StepEngineBuilder构建，
 * 确保步骤顺序的正确性和DAG验证的完整性
 */
class StepEngine<T> internal constructor(
    private val steps: MutableList<StepStep<T>>
) {
    
    companion object {
        private const val TAG = "StepEngine"
    }

    init {
        println("steps 顺序：${steps.joinToString { it.getStepId() }}")
    }
    
    // 当前步骤索引，-1表示未开始
    // 为什么使用-1？因为0是第一个步骤的索引，-1表示未开始状态
    private var currentStepIndex: Int = -1
    
    // 执行历史栈，用于navigateBack功能
    // 为什么需要历史栈？因为用户可能多次前进后退，需要记录完整的执行路径
    private val executionStack = mutableListOf<Int>()
    
    // 步骤变化的数据流，使用StateFlow提供响应式状态
    // 为什么使用StateFlow而不是Flow？StateFlow有初始值，更适合状态管理
    private val _stepChangeFlow = MutableStateFlow<StepChange<T>?>(null)
    private var stepChangeListener:(StepChange<T>)-> Unit = { change ->
        _stepChangeFlow.value = change
    }
    
    // 键值对数据容器，用于存储任意类型的数据
    // 为什么需要这个？除了泛型数据，还需要存储配置、状态等额外信息
    private val dataContainer = mutableMapOf<String, Any?>()
    
    // 泛型数据，类型安全的数据传递
    // 为什么需要泛型？提供类型安全，避免类型转换错误
    private var genericData: T? = null
    
    // 协程互斥锁，确保线程安全
    // 为什么需要Mutex？动态添加步骤和状态变更可能并发执行，需要同步
    private val mutex = Mutex()
    
    // 流程结果回调，链式调用设计
    // 为什么使用链式调用？提供更好的API体验，符合现代Kotlin风格
    private var onSuccess: ((T?) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    
    // 流程完成信号，用于等待流程完成
    // 为什么使用CompletableDeferred？提供协程友好的等待机制
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
     * 设置错误回调
     */
    fun onStepChange(callback: (StepChange<T>) -> Unit): StepEngine<T> {
        this.stepChangeListener = callback
        return this
    }
    
    /**
     * 启动Step流程（协程版本）
     * 
     * @param initialData 初始数据
     */
    suspend fun start(initialData: T? = null) {
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
            return
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
        steps.clear()
    }
    
    /**
     * 中止Step流程
     * 
     * @param fromUser 是否由用户主动触发
     */
    suspend fun abort(fromUser: Boolean = true) {
        val previousStep = getCurrentStep()
        cleanupAllSteps()
        currentStepIndex = -1
        executionStack.clear()
        // 通知流程已中止
        val stepChange = StepChange(
            currentStep = null,
            previousStep = previousStep,
            currentIndex = -1,
            totalSteps = steps.size,
            changeType = StepChange.ChangeType.ABORTED
        )
        stepChangeListener(stepChange)
        _stepChangeFlow.value = stepChange

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

        // 通知步骤变化
        val stepChange = StepChange(
            currentStep = previousStep,
            previousStep = oldStep,
            currentIndex = currentStepIndex,
            totalSteps = steps.size,
            changeType = StepChange.ChangeType.BACKWARD
        )
        stepChangeListener(stepChange)
        _stepChangeFlow.value = stepChange
        // 恢复上一个步骤
        previousStep.onStepResumed(StepCompletionProviderImpl())

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
        // 通知步骤变化
        val stepChange = StepChange(
            currentStep = step,
            previousStep = previousStep,
            currentIndex = currentStepIndex,
            totalSteps = steps.size,
            changeType = changeType
        )
        stepChangeListener(stepChange)
        _stepChangeFlow.value = stepChange
        // 启动步骤
        step.onStepStarted(StepCompletionProviderImpl())

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
        val stepChange = StepChange(
            currentStep = null,
            previousStep = previousStep,
            currentIndex = -1,
            totalSteps = steps.size,
            changeType = StepChange.ChangeType.COMPLETED
        )
        stepChangeListener(stepChange)
        _stepChangeFlow.value = stepChange
        
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
                println("steps 顺序：${steps.joinToString { it.getStepId() }}")
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
                println("steps 顺序：${steps.joinToString { it.getStepId() }}")
            }
        }
        
        override suspend fun addStep(step: StepStep<T>) {
            mutex.withLock {
                steps.add(step)
                println("steps 顺序：${steps.joinToString { it.getStepId() }}")
            }
        }
    }
}