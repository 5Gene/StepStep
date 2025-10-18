package org.spark.stepstep

import androidx.annotation.CallSuper

/**
 * Step步骤的基类
 * 
 * 提供一些通用的功能和便捷方法
 * 业务可以继承此类实现自己的Step步骤
 */
abstract class BaseStep : StepStep {
    
    protected val TAG: String = "StepStep#${getStepId()}"
    
    /**
     * StepCompletionProvider实例
     * 在onStepStarted时初始化
     */
    protected lateinit var stepCompletionProvider: StepCompletionProvider
    
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
    
    @CallSuper
    override fun onStepStarted(stepCompletionProvider: StepCompletionProvider) {
        this.stepCompletionProvider = stepCompletionProvider
        this.isStepStarted = true
        this.isStepStopped = false
        logD("onStepStarted")
    }
    
    @CallSuper
    override fun onStepResumed(stepCompletionProvider: StepCompletionProvider) {
        this.stepCompletionProvider = stepCompletionProvider
        this.isStepStopped = false
        logD("onStepResumed")
    }
    
    @CallSuper
    override fun onStepStopped() {
        this.isStepStopped = true
        logD("onStepStopped")
    }
    
    @CallSuper
    override fun cleanup() {
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
    protected fun finish() {
        logD("finish")
        if (::stepCompletionProvider.isInitialized) {
            stepCompletionProvider.finish()
        } else {
            logE("finish() called before onStepStarted")
        }
    }
    
    /**
     * 返回上一步
     */
    protected fun navigateBack() {
        logD("navigateBack")
        if (::stepCompletionProvider.isInitialized) {
            stepCompletionProvider.navigateBack()
        } else {
            logE("navigateBack() called before onStepStarted")
        }
    }
    
    /**
     * 中止Step流程
     * 
     * @param fromUser 是否由用户主动触发
     */
    protected fun abortStep(fromUser: Boolean = true) {
        logD("abortStep(fromUser=$fromUser)")
        if (::stepCompletionProvider.isInitialized) {
            stepCompletionProvider.abortStep(fromUser)
        } else {
            logE("abortStep() called before onStepStarted")
        }
    }
    
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

