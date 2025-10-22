package org.spark.stepstep

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.StateFlow

/**
 * Step框架的扩展函数集合
 *
 * 为什么需要扩展函数？
 * 1. 提供便捷的API：简化常用操作，提高开发效率
 * 2. 类型安全：利用Kotlin的类型系统，避免运行时错误
 * 3. DSL支持：提供更直观的构建语法，提升代码可读性
 * 4. 框架集成：与Android生态（LiveData、协程）无缝集成
 * 5. 向后兼容：不修改原有类，通过扩展提供新功能
 */

/**
 * 将StateFlow转换为LiveData
 *
 * 为什么需要这个转换？
 * 1. 兼容性：让使用LiveData的旧代码能够使用StateFlow
 * 2. 生命周期感知：LiveData自动处理生命周期，避免内存泄漏
 * 3. 观察者模式：LiveData提供更简单的观察者模式实现
 * 4. 线程安全：LiveData确保在主线程更新UI
 */
fun <T> StateFlow<StepChange<T>?>.toLiveData(): LiveData<StepChange<T>?> {
    return this.asLiveData()
}

/**
 * 批量添加步骤的DSL支持
 *
 * 为什么需要DSL？
 * 1. 可读性：代码更接近自然语言，易于理解
 * 2. 类型安全：编译时检查，避免运行时错误
 * 3. IDE支持：自动补全和语法高亮
 * 4. 链式调用：支持流畅的API调用
 * 5. 作用域限制：防止在错误的作用域中调用方法
 *
 * 使用示例：
 * ```
 * StepApi.createStepEngineBuilder {
 *     step(Step1())
 *     step(Step2())
 *     stepAfter("Step1", Step1_5())
 * }
 * ```
 */
inline fun <T> StepApi.createStepEngineBuilder(
    builderAction: StepStepEngineBuilderScope<T>.() -> Unit
): StepEngineBuilder<T> {
    val builder = createStepEngineBuilder<T>()
    StepStepEngineBuilderScope(builder).builderAction()
    return builder
}

/**
 * Builder的DSL作用域
 *
 * 为什么需要作用域类？
 * 1. 封装：将Builder的复杂API封装成简单的DSL方法
 * 2. 类型安全：确保泛型类型正确传递
 * 3. 方法重载：提供更简洁的方法名（step vs addStep）
 * 4. 扩展性：可以轻松添加新的DSL方法而不影响原有API
 */
class StepStepEngineBuilderScope<T>(val builder: StepEngineBuilder<T>) {

    /**
     * 添加步骤
     */
    fun step(step: StepStep<T>) {
        builder.addStep(step)
    }

    /**
     * 在指定步骤之后添加步骤
     */
    fun stepAfter(
        targetStepId: String,
        step: StepStep<T>,
        allowConflict: Boolean = false
    ) {
        builder.addStepAfter(targetStepId, step, allowConflict)
    }

    /**
     * 在指定步骤之前添加步骤
     */
    fun stepBefore(
        targetStepId: String,
        step: StepStep<T>,
        allowConflict: Boolean = false
    ) {
        builder.addStepBefore(targetStepId, step, allowConflict)
    }

    /**
     * 批量添加步骤
     */
    fun steps(vararg steps: StepStep<T>) {
        builder.addSteps(*steps)
    }
}

/**
 * 快速创建并启动Step引擎
 *
 * 为什么需要这个便捷方法？
 * 1. 简化使用：一行代码完成创建和启动
 * 2. 常见场景：很多情况下只需要简单的步骤序列
 * 3. 减少样板代码：避免重复的Builder模式代码
 * 4. 快速原型：适合快速测试和原型开发
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
suspend fun <T> quickStep(
    vararg steps: StepStep<T>,
    onStepChange: (StepChange<T>) -> Unit,
    data:T
): StepEngine<T> {
    val engine = StepApi.createStepEngineBuilder(*steps).build()

    // 注意：这里需要在协程环境中收集Flow
    // 实际使用时应该在适当的协程作用域中进行

    engine.start(data)
    return engine
}

