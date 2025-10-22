package org.spark.stepstep

/**
 * 自定义Step步骤接口
 * 
 * 参考Google StepStep设计，提供完整的步骤生命周期管理
 * 支持协程和泛型数据传递
 * 
 * 生命周期流程：
 * 1. isAvailable() - 检查步骤是否可用
 * 2. onStepStarted() - 步骤开始执行
 * 3. onStepResumed() - 步骤恢复（从其他步骤返回时）
 * 4. onStepStopped() - 步骤停止（进入下一步或返回上一步）
 * 5. cleanup() - 清理资源（步骤彻底结束时）
 */
interface StepStep<T> {
    
    /**
     * 步骤是否可用
     * 
     * 当返回false时，该步骤会被跳过
     * 可用于根据条件动态决定是否执行某个步骤
     * 
     * @return true-步骤可用，false-跳过该步骤
     */
    fun isAvailable(): Boolean = true
    
    /**
     * 步骤开始执行
     * 
     * 当步骤第一次被执行时调用
     * 在这里进行步骤的初始化和业务逻辑处理
     * 
     * @param stepCompletionProvider 步骤完成提供者，用于控制步骤流转
     */
    suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>)
    
    /**
     * 步骤恢复
     * 
     * 当从后续步骤返回到当前步骤时调用
     * 可用于恢复步骤的UI状态或重新加载数据
     * 
     * @param stepCompletionProvider 步骤完成提供者
     */
    suspend fun onStepResumed(stepCompletionProvider: StepCompletionProvider<T>) {
        // 默认空实现，子类可选择性重写
    }
    
    /**
     * 步骤停止
     * 
     * 当步骤结束（进入下一步或返回上一步）时调用
     * 可用于暂停当前操作、保存状态等
     * 
     * 注意：这不是最终清理，步骤可能会被恢复（onStepResumed）
     */
    suspend fun onStepStopped() {
        // 默认空实现，子类可选择性重写
    }
    
    /**
     * 清理资源
     * 
     * 当步骤彻底结束时调用（如abort或整个流程完成）
     * 在这里释放资源、取消监听、关闭连接等
     * 
     * 调用cleanup()后，该步骤不会再被使用
     */
    suspend fun cleanup() {
        // 默认空实现，子类可选择性重写
    }
    
    /**
     * 获取步骤的唯一标识
     * 
     * 用于识别步骤，调试日志等
     * 默认返回类名
     */
    fun getStepId(): String = this::class.java.simpleName
}

