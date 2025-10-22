package org.spark.stepstep

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.spark.stepstep.StepApi
import org.spark.stepstep.StepChange
import org.spark.stepstep.StepCompletionProvider
import org.spark.stepstep.BaseStep
import java.io.IOException

/**
 * 协程Step框架完整示例
 * 
 * 展示所有新功能的综合使用
 */
object CoroutineCompleteExample {
    
    /**
     * 完整示例：设备配对流程
     * 
     * 展示协程、数据传递、错误处理、动态步骤管理等所有功能
     */
    suspend fun devicePairingExample() {
        println("=== 设备配对流程示例 ===")
        
        // 1. 创建Step引擎，支持String类型的数据传递
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStep(PermissionStep(listOf("android.permission.BLUETOOTH")))
            .addStep(DeviceDiscoveryStep())
            .addStep(DeviceConnectionStep("00:11:22:33:44:55"))
            .addStep(DataSyncStep())
            .addStep(CompleteStep())
            .build()
        
        // 2. 设置初始数据
        engine.setData("userId", "user123")
        engine.setData("deviceType", "smartphone")
        engine.setData("sessionId", "session_${System.currentTimeMillis()}")
        
        // 3. 启动流程（链式调用）
        try {
            engine
                .onSuccess { data ->
                    println("✅ 设备配对成功: $data")
                }
                .onError { error ->
                    println("❌ 设备配对失败: ${error.message}")
                }
                .start("初始数据")
        } catch (e: Exception) {
            println("流程异常: ${e.message}")
        }
    }
    
    /**
     * 动态步骤管理示例
     */
    suspend fun dynamicStepManagementExample() {
        println("\n=== 动态步骤管理示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStep(DataLoadStep())
            .addStep(CompleteStep())
            .build()
        
        // 启动流程
        engine.start()
        
        // 在流程运行过程中动态添加步骤
        delay(1000)
        println("动态添加错误处理步骤")
        engine.addStep(ErrorHandlingStep())
        
        // 动态插入步骤
        delay(2000)
        println("动态插入权限步骤")
        engine.insertStep(1, PermissionStep(listOf("android.permission.CAMERA")))
        
        // 动态移除步骤
        delay(3000)
        println("动态移除数据加载步骤")
        engine.removeStep("DataLoadStep")
    }
    
    /**
     * 错误处理示例
     */
    suspend fun errorHandlingExample() {
        println("\n=== 错误处理示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(ErrorHandlingStep())
            .addStep(CompleteStep())
            .build()
        
        try {
            engine.start(
                onSuccess = { data ->
                    println("✅ 流程成功: $data")
                },
                onError = { error ->
                    when (error) {
                        is IOException -> println("❌ 网络错误: ${error.message}")
                        is SecurityException -> println("❌ 权限错误: ${error.message}")
                        is IllegalArgumentException -> println("❌ 参数错误: ${error.message}")
                        else -> println("❌ 其他错误: ${error.javaClass.simpleName} - ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            println("捕获异常: ${e.message}")
        }
    }
    
    /**
     * 超时处理示例
     */
    suspend fun timeoutHandlingExample() {
        println("\n=== 超时处理示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(TimeoutStep())
            .addStep(CompleteStep())
            .build()
        
        try {
            engine.start(
                onError = { error ->
                    if (error is kotlinx.coroutines.TimeoutCancellationException) {
                        println("⏰ 操作超时")
                    } else {
                        println("❌ 其他错误: ${error.message}")
                    }
                }
            )
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            println("⏰ 整体流程超时")
        }
    }
    
    /**
     * 数据传递示例
     */
    suspend fun dataPassingExample() {
        println("\n=== 数据传递示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(DataStep("step1", "Hello"))
            .addStep(DataStep("step2", "World"))
            .addStep(DataStep("step3", "!"))
            .addStep(CompleteStep())
            .build()
        
        // 设置全局数据
        engine.setData("globalMessage", "Step communication test")
        engine.setData("timestamp", System.currentTimeMillis())
        
        // 启动流程
        engine.start()
        
        // 在流程中获取和设置数据
        val globalMessage = engine.getData("globalMessage")
        val timestamp = engine.getData("timestamp")
        println("全局消息: $globalMessage")
        println("时间戳: $timestamp")
    }
    
    /**
     * 条件步骤示例
     */
    suspend fun conditionalStepExample() {
        println("\n=== 条件步骤示例 ===")
        
        val needPermission = true
        val isFirstTime = true
        val hasNetwork = true
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStep(
                ConditionalStep { 
                    // 异步检查权限条件
                    delay(500)
                    needPermission 
                }
            )
            .addStep(
                ConditionalStep { 
                    // 异步检查首次使用条件
                    delay(300)
                    isFirstTime 
                }
            )
            .addStep(
                ConditionalStep { 
                    // 异步检查网络条件
                    delay(200)
                    hasNetwork 
                }
            )
            .addStep(CompleteStep())
            .build()
        
        engine.start()
    }
    
    /**
     * 完整生命周期示例
     */
    suspend fun fullLifecycleExample(lifecycleOwner: LifecycleOwner) {
        println("\n=== 完整生命周期示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStep())
            .addStep(PermissionStep(listOf("android.permission.BLUETOOTH")))
            .addStep(DeviceDiscoveryStep())
            .addStep(DeviceConnectionStep("00:11:22:33:44:55"))
            .addStep(DataSyncStep())
            .addStep(CompleteStep())
            .build()
        
        // 监听步骤变化
        lifecycleOwner.lifecycleScope.launch {
            engine.getStepChangeFlow().collect { stepChange ->
                stepChange?.let { handleStepChange(it) }
            }
        }
        
        // 启动流程
        try {
            engine
                .onSuccess { data ->
                    println("✅ 流程成功完成: $data")
                }
                .onError { error ->
                    println("❌ 流程失败: ${error.message}")
                }
                .start("初始数据")
        } catch (e: Exception) {
            println("❌ 流程异常: ${e.message}")
        }
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

class WelcomeStep : BaseStep<String>() {
    override fun getStepId(): String = "WelcomeStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("显示欢迎页面")
        delay(1000)
        finish()
    }
}

class PermissionStep(
    private val permissions: List<String>
) : BaseStep<String>() {
    override fun getStepId(): String = "PermissionStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("申请权限: ${permissions.joinToString()}")
        delay(1500)
        finish()
    }
}

class DeviceDiscoveryStep : BaseStep<String>() {
    override fun getStepId(): String = "DeviceDiscoveryStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("搜索设备")
        delay(2000)
        finish()
    }
}

class DeviceConnectionStep(
    private val deviceMac: String
) : BaseStep<String>() {
    override fun getStepId(): String = "DeviceConnectionStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("连接设备: $deviceMac")
        delay(3000)
        finish()
    }
}

class DataSyncStep : BaseStep<String>() {
    override fun getStepId(): String = "DataSyncStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("同步数据")
        delay(2500)
        finish()
    }
}

class CompleteStep : BaseStep<String>() {
    override fun getStepId(): String = "CompleteStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("显示完成页面")
        delay(1000)
        finish()
    }
}

class DataLoadStep : BaseStep<String>() {
    override fun getStepId(): String = "DataLoadStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("加载数据")
        delay(1000)
        finish()
    }
}

class ErrorHandlingStep : BaseStep<String>() {
    override fun getStepId(): String = "ErrorHandlingStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行可能出错的操作")
        
        try {
            delay(1000)
            // 模拟随机错误
            if (Math.random() < 0.5) {
                throw IOException("模拟网络错误")
            }
            finish()
        } catch (e: Exception) {
            error(e)
        }
    }
}

class TimeoutStep : BaseStep<String>() {
    override fun getStepId(): String = "TimeoutStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("执行可能超时的操作")
        
        try {
            withTimeout(3000) {
                delay(5000) // 故意超时
            }
            finish()
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            error(e)
        }
    }
}

class DataStep(
    private val stepName: String,
    private val data: String
) : BaseStep<String>() {
    override fun getStepId(): String = "DataStep_$stepName"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("处理数据: $data")
        delay(500)
        finish()
    }
}

class ConditionalStep(
    private val condition: suspend () -> Boolean
) : BaseStep<String>() {
    override fun getStepId(): String = "ConditionalStep"
    
    override suspend fun isAvailable(): Boolean {
        val available = condition()
        logI("条件检查结果: $available")
        return available
    }
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        logI("条件步骤执行")
        delay(300)
        finish()
    }
}
