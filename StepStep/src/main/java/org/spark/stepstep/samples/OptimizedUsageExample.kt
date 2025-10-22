package org.spark.stepstep.samples

import org.spark.stepstep.BaseStep
import org.spark.stepstep.StepApi
import org.spark.stepstep.StepCompletionProvider
import kotlinx.coroutines.delay

/**
 * 优化后的使用示例
 * 展示所有新功能和便捷方法
 */
object OptimizedUsageExample {
    
    /**
     * 演示优化后的API使用
     */
    suspend fun demonstrateOptimizedUsage() {
        println("=== 优化后的使用示例 ===")
        
        // 1. 使用便捷的Builder方法
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStepsAfter("WelcomeStep", PermissionStep(), ValidationStep())
            .addStep(CompleteStep())
            .build()
        
        // 2. 链式调用启动
        engine
            .onError { error ->
                println("❌ 流程失败: ${error.message}")
            }
            .onSuccess { data ->
                println("✅ 流程成功完成: $data")
            }
            .start("优化后的数据")
    }
    
    /**
     * 演示便捷方法
     */
    suspend fun demonstrateConvenienceMethods() {
        println("=== 便捷方法示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(ConvenienceStep())
            .build()
        
        engine.start("便捷方法测试")
    }
}

/**
 * 欢迎步骤
 */
class WelcomeStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "WelcomeStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("欢迎步骤开始")
        
        // 使用便捷方法检查状态
        if (isStarted()) {
            logI("步骤已启动")
        }
        
        // 使用默认值方法
        val data = getDataOrDefault("默认欢迎数据")
        logI("当前数据: $data")
        
        delay(500)
        
        setData("欢迎完成")
        finish()
    }
}

/**
 * 权限步骤
 */
class PermissionStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "PermissionStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("权限步骤开始")
        
        val data = getDataOrDefault("默认权限数据")
        logI("当前数据: $data")
        
        delay(800)
        
        setData("权限检查完成")
        finish()
    }
}

/**
 * 验证步骤
 */
class ValidationStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "ValidationStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("验证步骤开始")
        
        val data = getDataOrDefault("默认验证数据")
        logI("当前数据: $data")
        
        delay(600)
        
        setData("验证完成")
        finish()
    }
}

/**
 * 便捷方法演示步骤
 */
class ConvenienceStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "ConvenienceStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("便捷方法演示步骤开始")
        
        // 演示状态检查
        logI("步骤状态 - 已启动: ${isStarted()}, 已停止: ${isStopped()}")
        
        // 演示默认值获取
        val data = getDataOrDefault("默认数据")
        logI("获取数据: $data")
        
        // 演示批量添加步骤
        val additionalSteps = arrayOf(
            AdditionalStep1<T>(),
            AdditionalStep2<T>(),
            AdditionalStep3<T>()
        )
        
        addSteps(*additionalSteps)
        logI("批量添加了 ${additionalSteps.size} 个步骤")
        
        delay(1000)
        
        setData("便捷方法演示完成")
        finish()
    }
}

/**
 * 额外步骤1
 */
class AdditionalStep1<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "AdditionalStep1"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("额外步骤1开始")
        delay(300)
        logI("额外步骤1完成")
        finish()
    }
}

/**
 * 额外步骤2
 */
class AdditionalStep2<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "AdditionalStep2"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("额外步骤2开始")
        delay(300)
        logI("额外步骤2完成")
        finish()
    }
}

/**
 * 额外步骤3
 */
class AdditionalStep3<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "AdditionalStep3"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("额外步骤3开始")
        delay(300)
        logI("额外步骤3完成")
        finish()
    }
}

/**
 * 完成步骤
 */
class CompleteStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "CompleteStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("完成步骤开始")
        
        val data = getDataOrDefault("默认完成数据")
        logI("最终数据: $data")
        
        delay(500)
        
        setData("所有步骤完成")
        finish()
    }
}
