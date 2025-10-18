package org.spark.stepstep

/**
 * 步骤完成提供者
 * 
 * 用于控制步骤的流转，提供给StepStep使用
 * 步骤可以通过该接口控制流程的进行
 */
interface StepCompletionProvider {
    
    /**
     * 完成当前步骤，进入下一个步骤
     * 
     * 调用后会触发：
     * 1. 当前步骤的onStepStopped()
     * 2. 下一个步骤的onStepStarted()或onStepResumed()
     */
    fun finish()
    
    /**
     * 返回上一个步骤
     * 
     * 调用后会触发：
     * 1. 当前步骤的onStepStopped()
     * 2. 上一个步骤的onStepResumed()
     * 
     * 注意：如果当前是第一个步骤，调用此方法会中止整个Step流程
     */
    fun navigateBack()
    
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
    fun abortStep(fromUser: Boolean = true)
}

