package org.spark.stepstep

import androidx.annotation.CallSuper

/**
 * Step步骤的基类
 * 
 * 提供一些通用的功能和便捷方法
 * 业务可以继承此类实现自己的Step步骤
 * 支持协程和泛型数据传递
 */
abstract class BaseStep<T> : StepStep<T> {
    
    protected val TAG: String = "StepStep#${getStepId()}"
    
    /**
     * StepCompletionProvider实例
     * 在onStepStarted时初始化
     */
    protected lateinit var stepCompletionProvider: StepCompletionProvider<T>
    
    /**
     * 步骤是否已启动
     */
    protected var isStepStarted: Boolean = false
        private set
    
    /**
     * 步骤是否已停止
     */
    protected var isStepStopped: Boolean = false
        private set
    
    /**
     * 检查StepCompletionProvider是否已初始化
     */
    private fun checkProviderInitialized(): Boolean {
        if (!::stepCompletionProvider.isInitialized) {
            logE("StepCompletionProvider not initialized. Call onStepStarted first.")
            return false
        }
        return true
    }
    
    @CallSuper
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        this.stepCompletionProvider = stepCompletionProvider
        this.isStepStarted = true
        this.isStepStopped = false
        logD("onStepStarted")
    }
    
    @CallSuper
    override suspend fun onStepResumed(stepCompletionProvider: StepCompletionProvider<T>) {
        this.stepCompletionProvider = stepCompletionProvider
        this.isStepStopped = false
        logD("onStepResumed")
    }
    
    @CallSuper
    override suspend fun onStepStopped() {
        this.isStepStopped = true
        logD("onStepStopped")
    }
    
    @CallSuper
    override suspend fun cleanup() {
        logD("cleanup")
    }
    
    /**
     * 步骤是否可用
     * 
     * 默认返回true，表示步骤总是会执行
     * 子类可以重写此方法，根据业务条件动态决定是否执行该步骤
     * 
     * 使用示例：
     * ```
     * override fun isAvailable(): Boolean {
     *     // 只在首次设置时执行此步骤
     *     return isFirstTimeStep()
     * }
     * ```
     * 
     * @return true-步骤会执行，false-步骤会被跳过
     */
    override fun isAvailable(): Boolean = true
    
    /**
     * 完成当前步骤
     */
    protected suspend fun finish() {
        logD("finish")
        if (checkProviderInitialized()) {
            stepCompletionProvider.finish()
        }
    }
    
    /**
     * 返回上一步
     */
    protected suspend fun navigateBack() {
        logD("navigateBack")
        if (checkProviderInitialized()) {
            stepCompletionProvider.navigateBack()
        }
    }
    
    /**
     * 中止Step流程
     * 
     * @param fromUser 是否由用户主动触发
     */
    protected suspend fun abortStep(fromUser: Boolean = true) {
        logD("abortStep(fromUser=$fromUser)")
        if (checkProviderInitialized()) {
            stepCompletionProvider.abortStep(fromUser)
        }
    }
    
    /**
     * 报告错误并中止流程
     * 
     * @param exception 错误异常
     */
    protected suspend fun error(exception: Throwable) {
        logE("error: ${exception.message}")
        if (checkProviderInitialized()) {
            stepCompletionProvider.error(exception)
        }
    }
    
    /**
     * 获取泛型数据
     */
    protected fun getData(): T? {
        return if (checkProviderInitialized()) {
            stepCompletionProvider.getData()
        } else {
            null
        }
    }
    
    /**
     * 设置泛型数据
     */
    protected fun setData(data: T?) {
        if (checkProviderInitialized()) {
            stepCompletionProvider.setData(data)
        }
    }
    
    /**
     * 动态添加步骤到指定ID的步骤之后
     */
    protected suspend fun addStepAfter(targetStepId: String, step: StepStep<T>) {
        if (checkProviderInitialized()) {
            stepCompletionProvider.addStepAfter(targetStepId, step)
        }
    }
    
    /**
     * 动态添加步骤到指定ID的步骤之前
     */
    protected suspend fun addStepBefore(targetStepId: String, step: StepStep<T>) {
        if (checkProviderInitialized()) {
            stepCompletionProvider.addStepBefore(targetStepId, step)
        }
    }
    
    /**
     * 动态添加步骤（添加到步骤列表末尾）
     */
    protected suspend fun addStep(step: StepStep<T>) {
        if (checkProviderInitialized()) {
            stepCompletionProvider.addStep(step)
        }
    }
    
    /**
     * 动态添加多个步骤到指定ID的步骤之后
     */
    protected suspend fun addStepsAfter(targetStepId: String, vararg steps: StepStep<T>) {
        steps.forEach { addStepAfter(targetStepId, it) }
    }
    
    /**
     * 动态添加多个步骤到指定ID的步骤之前
     */
    protected suspend fun addStepsBefore(targetStepId: String, vararg steps: StepStep<T>) {
        steps.forEach { addStepBefore(targetStepId, it) }
    }
    
    /**
     * 动态添加多个步骤到末尾
     */
    protected suspend fun addSteps(vararg steps: StepStep<T>) {
        steps.forEach { addStep(it) }
    }
    
    /**
     * 安全地获取数据，如果获取失败返回默认值
     */
    protected fun getDataOrDefault(defaultValue: T): T = getData() ?: defaultValue
    
    /**
     * 检查步骤是否已启动
     */
    protected fun isStarted(): Boolean = isStepStarted
    
    /**
     * 检查步骤是否已停止
     */
    protected fun isStopped(): Boolean = isStepStopped
    
    /**
     * 日志方法 - Debug级别
     * 子类可以重写以使用自己的日志系统
     */
    protected open fun logD(message: String) {
        println("[$TAG] $message")
    }
    
    /**
     * 日志方法 - Info级别
     */
    protected open fun logI(message: String) {
        println("[$TAG] $message")
    }
    
    /**
     * 日志方法 - Warning级别
     */
    protected open fun logW(message: String) {
        println("[$TAG] WARNING: $message")
    }
    
    /**
     * 日志方法 - Error级别
     */
    protected open fun logE(message: String) {
        println("[$TAG] ERROR: $message")
    }
}

