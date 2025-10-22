package org.spark.stepstep

/**
 * Step API
 * 
 * 提供创建Step引擎的入口
 * 这是整个Step框架的入口点
 * 支持协程和泛型数据传递
 */
object StepApi {
    
    /**
     * 创建Step引擎构建器
     * 
     * @return Step引擎构建器实例
     */
    fun <T> createStepEngineBuilder(): StepEngineBuilder<T> {
        return StepEngineBuilder()
    }
    
    /**
     * 创建Step引擎构建器（带初始步骤）
     * 
     * @param initialSteps 初始步骤列表
     * @return Step引擎构建器实例
     */
    fun <T> createStepEngineBuilder(vararg initialSteps: StepStep<T>): StepEngineBuilder<T> {
        return StepEngineBuilder<T>().addSteps(*initialSteps)
    }
    
    /**
     * 创建Step引擎构建器（带初始步骤）
     * 
     * @param initialSteps 初始步骤列表
     * @return Step引擎构建器实例
     */
    fun <T> createStepEngineBuilder(initialSteps: List<StepStep<T>>): StepEngineBuilder<T> {
        return StepEngineBuilder<T>().addSteps(initialSteps)
    }
}

