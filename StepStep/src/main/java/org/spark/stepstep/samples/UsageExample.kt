package org.spark.stepstep.samples

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.spark.stepstep.BaseStep
import org.spark.stepstep.StepApi
import org.spark.stepstep.StepChange
import org.spark.stepstep.StepCompletionProvider

/**
 * Step框架使用示例
 * 
 * 演示了框架的各种使用方式
 */
object UsageExample {
    
    /**
     * 示例1：基本使用
     * 
     * 创建一个简单的Step流程
     */
    fun basicUsage() {
        // 1. 创建Step引擎
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStepStep())
            .addStep(PermissionStepStep(listOf("android.permission.BLUETOOTH")))
            .addStep(DeviceConnectionStepStep("00:11:22:33:44:55"))
            .addStep(ConfigSyncStepStep())
            .addStep(CompleteStepStep())
            .build()
        
        // 2. 监听步骤变化（使用Flow，需要在协程中）
        // lifecycleScope.launch {
        //     engine.getStepChangeFlow().collect { stepChange ->
        //         stepChange?.let { handleStepChange(it) }
        //     }
        // }
        
        // 3. 启动Step流程（链式调用）
        engine
            .onSuccess { data -> println("流程成功: $data") }
            .onError { error -> println("流程失败: ${error.message}") }
            .start("初始数据")
    }
    
    /**
     * 示例2：动态插入步骤
     * 
     * 在现有步骤之间插入新步骤
     */
    fun dynamicInsertionUsage() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStepStep())
            .addStep(DeviceConnectionStepStep("00:11:22:33:44:55"))
            .addStep(CompleteStepStep())
            // 在WelcomeStepStep之后插入权限步骤
            .addStepAfter(
                "WelcomeStepStep",
                PermissionStepStep(listOf("android.permission.BLUETOOTH"))
            )
            // 在CompleteStepStep之前插入配置同步步骤
            .addStepBefore(
                "CompleteStepStep",
                ConfigSyncStepStep()
            )
            .build()
        
        engine
            .onSuccess { data -> println("流程成功: $data") }
            .onError { error -> println("流程失败: ${error.message}") }
            .start("初始数据")
    }
    
    /**
     * 示例3：使用Kotlin reified类型的便捷API
     */
    fun reifiedTypeUsage() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStepStep())
            .addStep(DeviceConnectionStepStep("00:11:22:33:44:55"))
            .addStep(CompleteStepStep())
            // 使用ID-based API
            .addStepAfter(
                "WelcomeStepStep",
                PermissionStepStep(listOf("android.permission.BLUETOOTH"))
            )
            .addStepBefore(
                "CompleteStepStep",
                ConfigSyncStepStep()
            )
            .build()
        
        engine
            .onSuccess { data -> println("流程成功: $data") }
            .onError { error -> println("流程失败: ${error.message}") }
            .start("初始数据")
    }
    
    /**
     * 示例4：使用便捷方法
     */
    fun convenientUsage() {
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStepStep())
            .addStep(PermissionStepStep(listOf("android.permission.BLUETOOTH")))
            .addStep(DeviceConnectionStepStep("00:11:22:33:44:55"))
            .addStep(ConfigSyncStepStep())
            .addStep(CompleteStepStep())
            .build()
        
        engine
            .onSuccess { data -> println("流程成功: $data") }
            .onError { error -> println("流程失败: ${error.message}") }
            .start("初始数据")
    }
    
    /**
     * 示例5：条件步骤
     * 
     * 根据条件决定是否执行某些步骤
     */
    fun conditionalStepUsage() {
        val needPermission = true
        val isFirstTimeStep = true
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStepStep())
            .addStep(
                ConditionalStepStep { needPermission }
            )
            .addStep(DeviceConnectionStepStep("00:11:22:33:44:55"))
            .addStep(
                ConditionalStepStep { isFirstTimeStep }
            )
            .addStep(CompleteStepStep())
            .build()
        
        engine
            .onSuccess { data -> println("流程成功: $data") }
            .onError { error -> println("流程失败: ${error.message}") }
            .start("初始数据")
    }
    
    /**
     * 示例6：完整的生命周期管理
     * 
     * 在Activity/Fragment中使用，完整的生命周期管理
     */
    fun fullLifecycleUsage(lifecycleOwner: LifecycleOwner) {
        // 创建引擎
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(WelcomeStepStep())
            .addStep(PermissionStepStep(listOf("android.permission.BLUETOOTH")))
            .addStep(DeviceConnectionStepStep("00:11:22:33:44:55"))
            .addStep(ConfigSyncStepStep())
            .addStep(CompleteStepStep())
            .build()
        
        // 使用Flow在协程中监听
        lifecycleOwner.lifecycleScope.launch {
            engine.getStepChangeFlow().collect { stepChange ->
                stepChange?.let { handleStepChange(it) }
            }
        }
        
        // 启动Step流程（链式调用）
        engine
            .onSuccess { data -> println("流程成功: $data") }
            .onError { error -> println("流程失败: ${error.message}") }
            .start("初始数据")
    }
    
    /**
     * 示例7：多个业务模块协同插入步骤
     * 
     * 演示如何避免插入冲突
     */
    fun multiModuleUsage() {
        // 基础Step流程
        val builder = StepApi.createStepEngineBuilder()
            .addStep(WelcomeStepStep())
            .addStep(DeviceConnectionStepStep("00:11:22:33:44:55"))
            .addStep(CompleteStepStep())
        
        // 模块A：添加权限步骤
        builder.addStepAfter<WelcomeStepStep>(
            PermissionStepStep(listOf("android.permission.BLUETOOTH"))
        )
        
        // 模块B：想在WelcomeStepStep后添加另一个步骤
        // 这会抛出异常，因为不允许多个步骤插入同一位置
        try {
            builder.addStepAfter<WelcomeStepStep>(
                ConfigSyncStepStep()
            )
        } catch (e: IllegalArgumentException) {
            println("检测到插入冲突: ${e.message}")
            
            // 解决方案1：插入到不同的位置
            builder.addStepAfter<PermissionStepStep>(
                ConfigSyncStepStep()
            )
            
            // 解决方案2：允许冲突（不推荐）
            // builder.addStepAfter<WelcomeStepStep>(
            //     ConfigSyncStepStep(),
            //     allowConflict = true
            // )
        }
        
        val engine = builder.build()
        engine.start()
    }
    
    /**
     * 处理步骤变化
     */
    private fun handleStepChange(stepChange: StepChange) {
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
            is WelcomeStepStep -> {
                // 显示欢迎页面
            }
            is PermissionStepStep -> {
                // 显示权限请求页面
            }
            is DeviceConnectionStepStep -> {
                // 显示设备连接页面
            }
            is ConfigSyncStepStep -> {
                // 显示配置同步页面
            }
            is CompleteStepStep -> {
                // 显示完成页面
            }
            null -> {
                // Step流程结束
                if (stepChange.isCompleted()) {
                    // 正常完成
                } else if (stepChange.isAborted()) {
                    // 用户取消或出错
                }
            }
        }
    }
    
    /**
     * 示例8：自定义步骤实现
     */
    class CustomBusinessStep : BaseStep() {
        
        override fun getStepId(): String = "CustomBusinessStep"
        
        override fun isAvailable(): Boolean {
            // 根据业务逻辑决定是否显示此步骤
            return checkBusinessCondition()
        }
        
        override fun onStepStarted(stepCompletionProvider: StepCompletionProvider) {
            super.onStepStarted(stepCompletionProvider)
            
            logI("开始执行业务逻辑")
            
            // 执行业务操作
            performBusinessLogic { success ->
                if (success) {
                    // 成功，进入下一步
                    finish()
                } else {
                    // 失败，可以选择重试、返回或中止
                    // navigateBack()  // 返回上一步
                    // abortStep()    // 中止流程
                }
            }
        }
        
        override fun onStepResumed(stepCompletionProvider: StepCompletionProvider) {
            super.onStepResumed(stepCompletionProvider)
            
            // 从后续步骤返回时，可能需要刷新UI
            logI("步骤恢复，刷新UI")
        }
        
        override fun onStepStopped() {
            super.onStepStopped()
            
            // 步骤停止，保存状态或取消操作
            logI("步骤停止，保存状态")
        }
        
        override fun cleanup() {
            super.cleanup()
            
            // 清理资源
            logI("清理业务资源")
        }
        
        private fun checkBusinessCondition(): Boolean {
            // 业务条件检查
            return true
        }
        
        private fun performBusinessLogic(callback: (Boolean) -> Unit) {
            // 执行业务逻辑
            callback(true)
        }
    }
}

