package org.spark.stepstep

/**
 * 步骤变化数据类
 * 
 * 用于通知外部观察者当前步骤的变化
 * 支持泛型数据传递
 */
data class StepChange<T>(
    /**
     * 当前步骤
     * null表示Step流程已结束
     */
    val currentStep: StepStep<T>?,

    /**
     * 上一个步骤
     * null表示这是第一个步骤
     */
    val previousStep: StepStep<T>?,

    /**
     * 当前步骤在所有步骤中的索引（从0开始）
     * -1表示流程已结束
     */
    val currentIndex: Int,

    /**
     * 步骤总数
     */
    val totalSteps: Int,

    /**
     * 变化类型
     */
    val changeType: ChangeType
) {
    /**
     * 步骤变化类型
     */
    enum class ChangeType {
        /** 前进到下一个步骤 */
        FORWARD,
        
        /** 返回到上一个步骤 */
        BACKWARD,
        
        /** 流程开始 */
        STARTED,
        
        /** 流程完成 */
        COMPLETED,
        
        /** 流程中止 */
        ABORTED
    }
    
    /**
     * 是否是最后一个步骤
     */
    fun isLastStep(): Boolean = currentIndex == totalSteps - 1
    
    /**
     * 是否是第一个步骤
     */
    fun isFirstStep(): Boolean = currentIndex == 0
    
    /**
     * 是否已完成
     */
    fun isCompleted(): Boolean = changeType == ChangeType.COMPLETED
    
    /**
     * 是否已中止
     */
    fun isAborted(): Boolean = changeType == ChangeType.ABORTED
}

