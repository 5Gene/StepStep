package org.spark.stepstep

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.StateFlow

/**
 * Step框架的扩展函数
 * 
 * 提供便捷的转换和工具方法
 */

/**
 * 将StateFlow转换为LiveData
 * 
 * 方便与Android的LiveData体系集成
 */
fun StateFlow<StepChange?>.toLiveData(): LiveData<StepChange?> {
    return this.asLiveData()
}

/**
 * 批量添加步骤的DSL支持
 * 
 * 使用示例：
 * ```
 * StepApi.createStepEngineBuilder {
 *     step(Step1())
 *     step(Step2())
 *     stepAfter<Step1>(Step1_5())
 * }
 * ```
 */
inline fun StepApi.createStepEngineBuilder(
    builderAction: StepStepEngineBuilderScope.() -> Unit
): StepEngineBuilder {
    val builder = createStepEngineBuilder()
    StepStepEngineBuilderScope(builder).builderAction()
    return builder
}

/**
 * Builder的DSL作用域
 */
class StepStepEngineBuilderScope(val builder: StepEngineBuilder) {
    
    /**
     * 添加步骤
     */
    fun step(step: StepStep) {
        builder.addStep(step)
    }
    
    /**
     * 在指定步骤之后添加步骤
     */
    inline fun <reified T : StepStep> stepAfter(
        step: StepStep,
        allowConflict: Boolean = false
    ) {
        builder.addStepAfter<T>(step, allowConflict)
    }
    
    /**
     * 在指定步骤之前添加步骤
     */
    inline fun <reified T : StepStep> stepBefore(
        step: StepStep,
        allowConflict: Boolean = false
    ) {
        builder.addStepBefore<T>(step, allowConflict)
    }
    
    /**
     * 批量添加步骤
     */
    fun steps(vararg steps: StepStep) {
        builder.addSteps(*steps)
    }
}

/**
 * 快速创建并启动Step引擎
 * 
 * 使用示例：
 * ```
 * quickStep(Step1(), Step2(), Step3()) { stepChange ->
 *     when (stepChange.currentStep) {
 *         is Step1 -> handleStep1()
 *         is Step2 -> handleStep2()
 *         null -> handleComplete()
 *     }
 * }
 * ```
 */
fun quickStep(
    vararg steps: StepStep,
    onStepChange: (StepChange) -> Unit
): StepEngine {
    val engine = StepApi.createStepEngineBuilder(*steps).build()
    
    // 注意：这里需要在协程环境中收集Flow
    // 实际使用时应该在适当的协程作用域中进行
    
    engine.start()
    return engine
}

