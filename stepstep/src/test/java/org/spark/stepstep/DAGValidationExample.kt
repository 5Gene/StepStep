package org.spark.stepstep

/**
 * DAG循环依赖检测示例
 * 
 * 演示StepEngineBuilder如何检测和防止循环依赖
 */
object DAGValidationExample {
    
    /**
     * 示例1：正常的DAG结构（无循环依赖）
     */
    fun validDAGExample() {
        println("=== 正常DAG结构示例 ===")
        
        try {
            val engine = StepApi.createStepEngineBuilder<String>()
                .addStep(StepA())
                .addStep(StepB())
                .addStepAfter("StepA", StepC()) // C在A之后
                .addStepBefore("StepB", StepD()) // D在B之前
                .build()
            
            println("✅ DAG验证通过，无循环依赖")
        } catch (e: IllegalStateException) {
            println("❌ DAG验证失败: ${e.message}")
        }
    }
    
    /**
     * 示例2：检测循环依赖
     */
    fun circularDependencyExample() {
        println("\n=== 循环依赖检测示例 ===")
        
        try {
            val engine = StepApi.createStepEngineBuilder<String>()
                .addStep(StepA())
                .addStep(StepB())
                .addStepAfter("StepA", StepC()) // C在A之后
                .addStepAfter("StepC", StepA()) // A在C之后 -> 循环依赖！
                .build()
            
            println("❌ 应该检测到循环依赖，但没有")
        } catch (e: IllegalStateException) {
            println("✅ 正确检测到循环依赖: ${e.message}")
        }
    }
    
    /**
     * 示例3：复杂的循环依赖
     */
    fun complexCircularDependencyExample() {
        println("\n=== 复杂循环依赖检测示例 ===")
        
        try {
            val engine = StepApi.createStepEngineBuilder<String>()
                .addStep(StepA())
                .addStep(StepB())
                .addStep(StepC())
                .addStepAfter("StepA", StepD()) // D在A之后
                .addStepAfter("StepB", StepE()) // E在B之后
                .addStepAfter("StepC", StepF()) // F在C之后
                .addStepAfter("StepD", StepE()) // E在D之后
                .addStepAfter("StepE", StepF()) // F在E之后
                .addStepAfter("StepF", StepD()) // D在F之后 -> 循环依赖！
                .build()
            
            println("❌ 应该检测到循环依赖，但没有")
        } catch (e: IllegalStateException) {
            println("✅ 正确检测到复杂循环依赖: ${e.message}")
        }
    }
    
    /**
     * 示例4：重复步骤检测
     */
    fun duplicateStepExample() {
        println("\n=== 重复步骤检测示例 ===")
        
        try {
            val engine = StepApi.createStepEngineBuilder<String>()
                .addStep(StepA())
                .addStep(StepB())
                .addStep(StepA()) // 重复添加StepA
                .build()
            
            println("❌ 应该检测到重复步骤，但没有")
        } catch (e: IllegalStateException) {
            println("✅ 正确检测到重复步骤: ${e.message}")
        }
    }
    
    /**
     * 示例5：复杂的DAG结构
     */
    fun complexDAGExample() {
        println("\n=== 复杂DAG结构示例 ===")
        
        try {
            val engine = StepApi.createStepEngineBuilder<String>()
                .addStep(StepA())
                .addStep(StepB())
                .addStep(StepC())
                .addStepAfter("StepA", StepD()) // D在A之后
                .addStepAfter("StepA", StepE()) // E在A之后
                .addStepBefore("StepB", StepF()) // F在B之前
                .addStepBefore("StepC", StepG()) // G在C之前
                .addStepAfter("StepD", StepH()) // H在D之后
                .addStepAfter("StepE", StepI()) // I在E之后
                .build()
            
            println("✅ 复杂DAG验证通过，无循环依赖")
        } catch (e: IllegalStateException) {
            println("❌ 复杂DAG验证失败: ${e.message}")
        }
    }
}

// 测试步骤类
class StepA: BaseStep<String>() {
    override fun getStepId(): String = "StepA"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepA")
        finish()
    }
}

class StepB: BaseStep<String>() {
    override fun getStepId(): String = "StepB"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepB")
        finish()
    }
}

class StepC: BaseStep<String>() {
    override fun getStepId(): String = "StepC"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepC")
        finish()
    }
}

class StepD: BaseStep<String>() {
    override fun getStepId(): String = "StepD"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepD")
        finish()
    }
}

class StepE: BaseStep<String>() {
    override fun getStepId(): String = "StepE"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepE")
        finish()
    }
}

class StepF: BaseStep<String>() {
    override fun getStepId(): String = "StepF"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepF")
        finish()
    }
}

class StepG: BaseStep<String>() {
    override fun getStepId(): String = "StepG"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepG")
        finish()
    }
}

class StepH: BaseStep<String>() {
    override fun getStepId(): String = "StepH"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepH")
        finish()
    }
}

class StepI: BaseStep<String>() {
    override fun getStepId(): String = "StepI"
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行StepI")
        finish()
    }
}
