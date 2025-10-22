package org.spark.stepstep

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.spark.stepstep.StepApi
import org.spark.stepstep.StepChange
import org.spark.stepstep.StepCompletionProvider
import org.spark.stepstep.BaseStep
import java.io.IOException

/**
 * 协程Step框架使用示例
 * 
 * 演示如何在协程环境中使用Step框架
 * 包括异步操作、错误处理、数据传递等
 */
object CoroutineUsageExample {
    
    /**
     * 示例1：基本协程使用
     */
    suspend fun basicCoroutineUsage() {
        // 1. 创建Step引擎
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(AsyncDataLoadStep())
            .addStep(CoroutinePermissionStep(listOf("android.permission.BLUETOOTH")))
            .addStep(CoroutineDeviceConnectionStep("00:11:22:33:44:55"))
            .addStep(CoroutineDataSyncStep())
            .addStep(CoroutineCompleteStep())
            .build()
        
        // 2. 启动Step流程（链式调用）
        try {
            engine
                .onSuccess { data ->
                    println("Step流程成功完成: $data")
                }
                .onError { error ->
                    println("Step流程出错: ${error.message}")
                }
                .start("初始数据")
        } catch (e: Exception) {
            println("流程异常: ${e.message}")
        }
    }
    
    /**
     * 示例2：动态步骤管理
     */
    suspend fun dynamicStepManagement() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(AsyncDataLoadStep())
            .addStep(DynamicStepManagementStep())
            .addStep(CoroutineCompleteStep())
            .build()
        
        // 启动流程
        engine.start("初始数据")
    }
    
    /**
     * 示例3：数据传递
     */
    suspend fun dataPassingExample() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(DataPassingStep("userName", "John Doe"))
            .addStep(DataPassingStep("deviceId", "device123"))
            .addStep(CoroutineCompleteStep())
            .build()
        
        // 启动流程
        engine.start("初始数据")
    }
    
    /**
     * 示例4：错误处理
     */
    suspend fun errorHandlingExample() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(ErrorHandlingStep())
            .addStep(CoroutineCompleteStep())
            .build()
        
        try {
            engine
                .onSuccess { data ->
                    println("流程成功: $data")
                }
                .onError { error ->
                    println("流程失败: ${error.message}")
                    when (error) {
                        is IOException -> println("网络错误")
                        is SecurityException -> println("权限错误")
                        else -> println("其他错误: ${error.javaClass.simpleName}")
                    }
                }
                .start("初始数据")
        } catch (e: Exception) {
            println("捕获异常: ${e.message}")
        }
    }
    
    /**
     * 示例5：条件步骤
     */
    suspend fun conditionalStepExample() {
        val needPermission = true
        val isFirstTime = true
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(AsyncDataLoadStep())
            .addStep(
                CoroutineConditionalStep { 
                    // 异步检查条件
                    delay(500)
                    needPermission 
                }
            )
            .addStep(
                CoroutineConditionalStep { 
                    // 异步检查条件
                    delay(300)
                    isFirstTime 
                }
            )
            .addStep(CoroutineCompleteStep())
            .build()
        
        engine.start("条件步骤测试数据")
    }
    
    /**
     * 示例6：超时处理
     */
    suspend fun timeoutHandlingExample() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(TimeoutHandlingStep())
            .addStep(CoroutineCompleteStep())
            .build()
        
        try {
            engine
                .onError { error ->
                    if (error is kotlinx.coroutines.TimeoutCancellationException) {
                        println("操作超时")
                    } else {
                        println("其他错误: ${error.message}")
                    }
                }
                .start("超时测试数据")
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            println("整体流程超时")
        }
    }
    
    /**
     * 示例7：完整的生命周期管理
     */
    suspend fun fullLifecycleExample(lifecycleOwner: LifecycleOwner) {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(AsyncDataLoadStep())
            .addStep(CoroutinePermissionStep(listOf("android.permission.BLUETOOTH")))
            .addStep(CoroutineDeviceConnectionStep("00:11:22:33:44:55"))
            .addStep(CoroutineDataSyncStep())
            .addStep(CoroutineCompleteStep())
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
                    println("流程成功完成: $data")
                }
                .onError { error ->
                    println("流程失败: ${error.message}")
                }
                .start("初始数据")
        } catch (e: Exception) {
            println("流程异常: ${e.message}")
        }
    }
    
    /**
     * 示例8：自定义业务步骤
     */
    class CustomBusinessStep: BaseStep<String>() {
        
        override fun getStepId(): String = "CustomBusinessStep"
        
        override suspend fun isAvailable(): Boolean {
            // 异步检查业务条件
            delay(200)
            return checkBusinessCondition()
        }
        
        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)
            
            logI("开始执行业务逻辑")
            
            try {
                // 执行业务操作
                val result = performBusinessLogic()
                logI("业务逻辑执行完成: $result")
                finish()
            } catch (e: Exception) {
                logE("业务逻辑执行失败: ${e.message}")
                error(e)
            }
        }
        
        override suspend fun onStepResumed(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepResumed(stepCompletionProvider)
            
            // 从后续步骤返回时，可能需要刷新UI
            logI("步骤恢复，刷新UI")
        }
        
        override suspend fun onStepStopped() {
            super.onStepStopped()
            
            // 步骤停止，保存状态或取消操作
            logI("步骤停止，保存状态")
        }
        
        override suspend fun cleanup() {
            super.cleanup()
            
            // 清理资源
            logI("清理业务资源")
        }
        
        private suspend fun checkBusinessCondition(): Boolean {
            // 异步业务条件检查
            delay(300)
            return Math.random() > 0.3
        }
        
        private suspend fun performBusinessLogic(): String {
            // 模拟业务逻辑执行
            delay(2000)
            
            // 模拟随机失败
            if (Math.random() < 0.2) {
                throw IOException("Business logic failed")
            }
            
            return "Business logic completed successfully"
        }
    }
    
    /**
     * 示例9：多步骤协同工作
     */
    suspend fun multiStepCollaboration() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(AsyncDataLoadStep())
            .addStep(CoroutinePermissionStep(listOf("android.permission.BLUETOOTH")))
            .addStep(CoroutineDeviceConnectionStep("00:11:22:33:44:55"))
            .addStep(CoroutineDataSyncStep())
            .addStep(CustomBusinessStep())
            .addStep(CoroutineCompleteStep())
            .build()
        
        // 设置共享数据
        engine.setData("userId", "user123")
        engine.setData("deviceType", "smartphone")
        
        try {
            engine
                .onSuccess { data ->
                    println("多步骤协同完成: $data")
                }
                .onError { error ->
                    println("多步骤协同失败: ${error.message}")
                }
                .start("初始数据")
        } catch (e: Exception) {
            println("多步骤协同异常: ${e.message}")
        }
    }
    
    /**
     * 示例10：步骤间通信
     */
    suspend fun stepCommunication() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(DataPassingStep("step1", "Hello"))
            .addStep(DataPassingStep("step2", "World"))
            .addStep(DataPassingStep("step3", "!"))
            .addStep(CoroutineCompleteStep())
            .build()
        
        // 启动流程
        engine.start("初始数据")
        
        // 在流程中传递数据
        engine.setData("message", "Step communication test")
        
        // 获取传递的数据
        val message = engine.getData("message")
        println("传递的消息: $message")
    }
    
    /**
     * 处理步骤变化
     */
    private fun handleStepChange(stepChange: StepChange<String>) {
        when (stepChange.changeType) {
            StepChange.ChangeType.STARTED -> {
                println("Step流程开始")
            }
            StepChange.ChangeType.FORWARD -> {
                println("进入步骤: ${stepChange.currentStep?.getStepId()}")
                println("进度: ${stepChange.currentIndex + 1}/${stepChange.totalSteps}")
            }
            StepChange.ChangeType.BACKWARD -> {
                println("返回到步骤: ${stepChange.currentStep?.getStepId()}")
            }
            StepChange.ChangeType.COMPLETED -> {
                println("Step流程完成！")
            }
            StepChange.ChangeType.ABORTED -> {
                println("Step流程已中止")
            }
        }
        
        // 根据具体步骤处理UI
        when (val step = stepChange.currentStep) {
            is AsyncDataLoadStep -> {
                println("显示数据加载页面")
            }
            is CoroutinePermissionStep -> {
                println("显示权限请求页面")
            }
            is CoroutineDeviceConnectionStep -> {
                println("显示设备连接页面")
            }
            is CoroutineDataSyncStep -> {
                println("显示数据同步页面")
            }
            is CoroutineCompleteStep -> {
                println("显示完成页面")
            }
            null -> {
                // Step流程结束
                if (stepChange.isCompleted()) {
                    println("正常完成")
                } else if (stepChange.isAborted()) {
                    println("用户取消或出错")
                }
            }
        }
    }
}
