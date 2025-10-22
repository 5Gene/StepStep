package org.spark.stepstep

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.spark.stepstep.StepApi
import org.spark.stepstep.StepChange
import org.spark.stepstep.BaseStep
import org.spark.stepstep.StepCompletionProvider

/**
 * 完整的使用示例
 * 
 * 展示所有新功能的使用方法
 */
object CompleteUsageExample {
    
    /**
     * 基本使用示例
     */
    suspend fun basicUsageExample() {
        println("=== 基本使用示例 ===")
        
        // 1. 创建Step引擎，支持String类型的数据传递
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStep(DataProcessStep())
            .addStep(CompleteStep())
            .build()
        
        // 2. 设置初始数据并启动流程（链式调用）
        engine
            .onError { error ->
                println("❌ 流程失败: ${error.message}")
            }
            .onSuccess { data ->
                println("✅ 流程成功完成")
            }
            .start("初始数据")
    }
    
    /**
     * 数据传递示例
     */
    suspend fun dataPassingExample() {
        println("\n=== 数据传递示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(DataStep1())
            .addStep(DataStep2())
            .addStep(DataStep3())
            .addStep(CompleteStep())
            .build()
        
        // 链式调用启动流程
        engine.start("Hello")
    }
    
    /**
     * 动态步骤管理示例
     */
    suspend fun dynamicStepExample() {
        println("\n=== 动态步骤管理示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(FirstStep())
            .addStep(LastStep())
            .build()
        
        engine.start("初始数据")
    }
    
    /**
     * 条件性动态步骤示例
     */
    suspend fun conditionalDynamicExample() {
        println("\n=== 条件性动态步骤示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(ConditionalDynamicStep())
            .addStep(CompleteStep())
            .build()
        
        engine.start("条件测试数据")
    }
    
    /**
     * 完整生命周期示例
     */
    suspend fun fullLifecycleExample(lifecycleOwner: LifecycleOwner) {
        println("\n=== 完整生命周期示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStep(DataProcessStep())
            .addStep(CompleteStep())
            .build()
        
        // 监听步骤变化
        lifecycleOwner.lifecycleScope.launch {
            engine.getStepChangeFlow().collect { stepChange ->
                stepChange?.let { handleStepChange(it) }
            }
        }
        
        engine.start("生命周期测试数据")
    }
    
    /**
     * 处理步骤变化
     */
    private fun handleStepChange(stepChange: StepChange<String>) {
        when (stepChange.changeType) {
            StepChange.ChangeType.STARTED -> {
                println("🚀 Step流程开始")
            }
            StepChange.ChangeType.FORWARD -> {
                println("➡️ 进入步骤: ${stepChange.currentStep?.getStepId()}")
                println("📊 进度: ${stepChange.currentIndex + 1}/${stepChange.totalSteps}")
            }
            StepChange.ChangeType.BACKWARD -> {
                println("⬅️ 返回到步骤: ${stepChange.currentStep?.getStepId()}")
            }
            StepChange.ChangeType.COMPLETED -> {
                println("✅ Step流程完成！")
            }
            StepChange.ChangeType.ABORTED -> {
                println("❌ Step流程已中止")
            }
        }
    }
}

/**
 * 示例步骤实现
 */

class WelcomeStep<T> : BaseStep<T>() {
    override fun getStepId(): String = "WelcomeStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("显示欢迎页面")
        
        // 获取数据
        val currentData = getData()
        logI("当前数据: $currentData")
        
        // 修改数据
        val newData = "$currentData + 欢迎"
        setData(newData)
        
        // 模拟处理时间
        kotlinx.coroutines.delay(1000)
        
        finish()
    }
}

class DataProcessStep : BaseStep<String>() {
    override fun getStepId(): String = "DataProcessStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("处理数据")
        
        val currentData = getData()
        logI("接收到的数据: $currentData")
        
        // 处理数据
        val processedData = "$currentData + 已处理"
        setData(processedData)
        
        kotlinx.coroutines.delay(1500)
        
        finish()
    }
}

class CompleteStep : BaseStep<String>() {
    override fun getStepId(): String = "CompleteStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("显示完成页面")
        
        val finalData = getData()
        logI("最终数据: $finalData")
        
        kotlinx.coroutines.delay(500)
        
        finish()
    }
}

class DataStep1 : BaseStep<String>() {
    override fun getStepId(): String = "DataStep1"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("数据步骤1")
        val currentData = getData()
        setData("$currentData World")
        
        kotlinx.coroutines.delay(500)
        finish()
    }
}

class DataStep2 : BaseStep<String>() {
    override fun getStepId(): String = "DataStep2"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("数据步骤2")
        val currentData = getData()
        setData("$currentData !")
        
        kotlinx.coroutines.delay(500)
        finish()
    }
}

class DataStep3 : BaseStep<String>() {
    override fun getStepId(): String = "DataStep3"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("数据步骤3")
        val currentData = getData()
        setData("$currentData 完成")
        
        kotlinx.coroutines.delay(500)
        finish()
    }
}

