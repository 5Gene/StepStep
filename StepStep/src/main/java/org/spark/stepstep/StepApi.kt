package org.spark.stepstep

/**
 * Step API
 * 
 * 提供创建Step引擎的入口
 * 这是整个Step框架的入口点
 */
object StepApi {
    
    /**
     * 创建Step引擎构建器
     * 
     * @return Step引擎构建器实例
     */
    fun createStepEngineBuilder(): StepEngineBuilder {
        return StepEngineBuilder()
    }
    
    /**
     * 创建Step引擎构建器（带初始步骤）
     * 
     * @param initialSteps 初始步骤列表
     * @return Step引擎构建器实例
     */
    fun createStepEngineBuilder(vararg initialSteps: StepStep): StepEngineBuilder {
        return StepEngineBuilder().addSteps(*initialSteps)
    }
    
    /**
     * 创建Step引擎构建器（带初始步骤）
     * 
     * @param initialSteps 初始步骤列表
     * @return Step引擎构建器实例
     */
    fun createStepEngineBuilder(initialSteps: List<StepStep>): StepEngineBuilder {
        return StepEngineBuilder().addSteps(initialSteps)
    }
}

