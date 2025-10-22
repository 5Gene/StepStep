package org.spark.stepstep

/**
 * 步骤完成提供者
 * 
 * 用于控制步骤的流转，提供给StepStep使用
 * 步骤可以通过该接口控制流程的进行
 * 支持协程和错误处理
 */
interface StepCompletionProvider<T> {
    
    /**
     * 完成当前步骤，进入下一个步骤
     * 
     * 调用后会触发：
     * 1. 当前步骤的onStepStopped()
     * 2. 下一个步骤的onStepStarted()或onStepResumed()
     */
    suspend fun finish()
    
    /**
     * 返回上一个步骤
     * 
     * 调用后会触发：
     * 1. 当前步骤的onStepStopped()
     * 2. 上一个步骤的onStepResumed()
     * 
     * 注意：如果当前是第一个步骤，调用此方法会中止整个Step流程
     */
    suspend fun navigateBack()
    
    /**
     * 中止整个Step流程
     * 
     * 调用后会：
     * 1. 触发所有步骤的cleanup()
     * 2. 结束整个Step流程
     * 3. 通知监听者流程已中止
     * 
     * @param fromUser 是否由用户主动触发（true-用户取消，false-系统错误）
     */
    suspend fun abortStep(fromUser: Boolean = true)
    
    /**
     * 报告错误并中止流程
     * 
     * 调用后会：
     * 1. 触发错误回调
     * 2. 清理所有步骤
     * 3. 中止整个Step流程
     * 
     * @param exception 错误异常
     */
    suspend fun error(exception: Throwable)
    
    /**
     * 获取泛型数据
     */
    fun getData(): T?
    
    /**
     * 设置泛型数据
     */
    fun setData(data: T?)
    
    /**
     * 动态添加步骤到指定ID的步骤之后
     */
    suspend fun addStepAfter(targetStepId: String, step: StepStep<T>)
    
    /**
     * 动态添加步骤到指定ID的步骤之前
     */
    suspend fun addStepBefore(targetStepId: String, step: StepStep<T>)
    
    /**
     * 动态添加步骤（添加到步骤列表末尾）
     */
    suspend fun addStep(step: StepStep<T>)
}

