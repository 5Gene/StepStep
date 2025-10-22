package org.spark.stepstep

import androidx.annotation.CallSuper

/**
 * Step步骤的基类 - 提供便捷的步骤实现
 * 
 * 为什么需要BaseStep？
 * 1. 减少样板代码：提供通用的生命周期管理
 * 2. 统一日志：内置日志功能，便于调试
 * 3. 状态管理：自动管理步骤的启动/停止状态
 * 4. 便捷方法：提供常用的数据操作和流程控制方法
 * 5. 错误处理：统一的错误处理机制
 * 
 * 设计原则：
 * 1. 模板方法模式：定义步骤的生命周期模板
 * 2. 开闭原则：对扩展开放，对修改封闭
 * 3. 单一职责：每个方法只负责一个功能
 * 4. 协程友好：所有方法都支持协程
 */
abstract class BaseStep<T> : StepStep<T> {
    
    // 日志标签，包含步骤ID便于调试
    // 为什么需要TAG？Android日志系统需要标签来过滤和分类日志
    protected val TAG: String = "StepStep#${getStepId()}"
    
    /**
     * StepCompletionProvider实例
     * 在onStepStarted时初始化
     * 
     * 为什么使用lateinit？
     * 因为只有在onStepStarted时才能获得provider实例，
     * 使用lateinit避免空值传递，提供更好的类型安全
     */
    protected lateinit var stepCompletionProvider: StepCompletionProvider<T>
    
    /**
     * 步骤是否已启动
     * 
     * 为什么需要这个状态？
     * 1. 防止重复调用onStepStarted
     * 2. 提供状态查询功能
     * 3. 便于调试和状态检查
     */
    protected var isStepStarted: Boolean = false
        private set
    
    /**
     * 步骤是否已停止
     * 
     * 为什么需要这个状态？
     * 1. 区分onStepStopped和cleanup的调用时机
     * 2. 防止在停止状态下执行某些操作
     * 3. 提供状态查询功能
     */
    protected var isStepStopped: Boolean = false
        private set
    
    /**
     * 检查StepCompletionProvider是否已初始化
     * 
     * 为什么需要这个检查？
     * 防止在provider未初始化时调用相关方法，避免运行时异常
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
        isStepStopped = true
        logD("Step cleanup completed")
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
    override suspend fun isAvailable(): Boolean = true
    
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

