package org.spark.stepstep

import org.spark.stepstep.BaseStep
import org.spark.stepstep.StepApi
import org.spark.stepstep.StepStep
import org.spark.stepstep.StepCompletionProvider
import kotlinx.coroutines.delay

/**
 * ID-based动态步骤管理示例
 * 展示如何通过步骤ID来动态添加步骤
 */
object IdBasedDynamicStepExample {
    
    /**
     * 演示通过ID动态添加步骤
     */
    suspend fun demonstrateIdBasedDynamicSteps() {
        println("=== ID-based动态步骤管理示例 ===")
        
        // 1. 在Builder中使用ID添加步骤
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStepAfter("WelcomeStep", PermissionStep())
            .addStepBefore("LastStep", ValidationStep())
            .addStep(LastStep())
            .build()
        
        // 2. 启动流程
        engine
            .onSuccess { data ->
                println("✅ 流程成功完成: $data")
            }
            .onError { error ->
                println("❌ 流程失败: ${error.message}")
            }
            .start("初始数据")
    }
    
    /**
     * 演示在运行时通过ID动态添加步骤
     */
    suspend fun demonstrateRuntimeIdBasedSteps() {
        println("=== 运行时ID-based动态步骤管理示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStep(LastStep())
            .build()
        
        engine
            .onSuccess { data ->
                println("✅ 流程成功完成: $data")
            }
            .onError { error ->
                println("❌ 流程失败: ${error.message}")
            }
            .start("初始数据")
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
        
        val currentData = getData()
        logI("当前数据: $currentData")
        
        // 模拟处理
        delay(500)
        
        setData("WelcomeStep处理后的数据")
        
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
        
        val currentData = getData()
        logI("当前数据: $currentData")
        
        // 模拟权限检查
        delay(1000)
        
        setData("PermissionStep处理后的数据")
        
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
        
        val currentData = getData()
        logI("当前数据: $currentData")
        
        // 模拟数据验证
        delay(800)
        
        setData("ValidationStep处理后的数据")
        
        finish()
    }
}

/**
 * 最后一个步骤
 */
class LastStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "LastStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("最后一个步骤开始")
        
        val currentData = getData()
        logI("最终数据: $currentData")
        
        // 模拟最终处理
        delay(500)
        
        setData("最终处理完成的数据")
        
        finish()
    }
}

/**
 * 运行时动态步骤示例
 */
class RuntimeDynamicStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "RuntimeDynamicStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("运行时动态步骤开始")
        
        // 根据条件动态添加步骤
        val data = getData()
        
        when {
            data.toString().contains("permission") -> {
                // 通过ID在指定位置添加步骤
                addStepAfter("WelcomeStep", PermissionStep<T>())
                logI("在WelcomeStep之后添加了PermissionStep")
            }
            
            data.toString().contains("validation") -> {
                // 通过ID在指定位置添加步骤
                addStepBefore("LastStep", ValidationStep<T>())
                logI("在LastStep之前添加了ValidationStep")
            }
            
            data.toString().contains("network") -> {
                // 通过ID在指定位置添加步骤
                addStepAfter("PermissionStep", NetworkStep<T>())
                logI("在PermissionStep之后添加了NetworkStep")
            }
        }
        
        finish()
    }
}

/**
 * 网络步骤
 */
class NetworkStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "NetworkStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("网络步骤开始")
        
        val currentData = getData()
        logI("当前数据: $currentData")
        
        // 模拟网络连接
        delay(1200)
        
        setData("NetworkStep处理后的数据")
        
        finish()
    }
}
