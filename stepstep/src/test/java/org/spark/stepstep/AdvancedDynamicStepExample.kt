package org.spark.stepstep

import org.spark.stepstep.BaseStep
import org.spark.stepstep.StepApi
import org.spark.stepstep.StepStep
import org.spark.stepstep.StepCompletionProvider
import kotlinx.coroutines.delay

/**
 * 高级动态步骤管理示例
 * 展示如何在指定步骤前后添加新步骤
 */
object AdvancedDynamicStepExample {
    
    /**
     * 演示高级动态步骤管理
     */
    suspend fun demonstrateAdvancedDynamicSteps() {
        println("=== 高级动态步骤管理示例 ===")
        
        val engine = StepApi.createStepEngineBuilder<String>()
            .addStep(FirstStep())
            .addStep(LastStep())
            .build()
        
        // 启动流程（链式调用）
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
     * 第一个步骤：演示在指定步骤前后添加步骤
     */
    class FirstStep : BaseStep<String>() {

        override fun getStepId(): String = "FirstStep"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("第一个步骤开始")

            // 获取当前数据
            val currentData = getData()
            logI("当前数据: $currentData")

            // 创建要添加的步骤
            val middleStep1 = MiddleStep1()
            val middleStep2 = MiddleStep2()
            val middleStep3 = MiddleStep3()

            // 演示不同的添加方式

            // 1. 在指定ID的步骤之后添加
            addStepAfter("FirstStep", middleStep1)
            logI("在 FirstStep 之后添加了 MiddleStep1")

            // 2. 在指定ID的步骤之后添加
            addStepAfter("MiddleStep1", middleStep2)
            logI("在 MiddleStep1 之后添加了 MiddleStep2")

            // 3. 在指定ID的步骤之前添加
            addStepBefore("MiddleStep2", middleStep3)
            logI("在 MiddleStep2 之前添加了 MiddleStep3")

            // 4. 直接添加步骤到末尾
            val additionalStep = AdditionalStep()
            addStep(additionalStep)
            logI("添加了额外步骤到末尾")

            // 模拟一些处理时间
            delay(1000)

            // 修改数据
            setData("FirstStep处理后的数据")

            // 完成当前步骤
            finish()
        }
    }

    /**
     * 中间步骤1
     */
    class MiddleStep1 : BaseStep<String>() {

        override fun getStepId(): String = "MiddleStep1"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("中间步骤1开始")

            val currentData = getData()
            logI("当前数据: $currentData")

            // 模拟处理
            delay(500)

            setData("MiddleStep1处理后的数据")

            finish()
        }
    }

    /**
     * 中间步骤2
     */
    class MiddleStep2 : BaseStep<String>() {

        override fun getStepId(): String = "MiddleStep2"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("中间步骤2开始")

            val currentData = getData()
            logI("当前数据: $currentData")

            // 模拟处理
            delay(500)

            setData("MiddleStep2处理后的数据")

            finish()
        }
    }

    /**
     * 中间步骤3
     */
    class MiddleStep3 : BaseStep<String>() {

        override fun getStepId(): String = "MiddleStep3"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("中间步骤3开始")

            val currentData = getData()
            logI("当前数据: $currentData")

            // 模拟处理
            delay(500)

            setData("MiddleStep3处理后的数据")

            finish()
        }
    }

    /**
     * 最后一个步骤
     */
    class LastStep : BaseStep<String>() {

        override fun getStepId(): String = "LastStep"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
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
     * 条件性动态步骤示例
     * 根据条件在指定位置添加步骤
     */
    class ConditionalAdvancedStep : BaseStep<String>() {

        override fun getStepId(): String = "ConditionalAdvancedStep"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("条件性高级步骤开始")

            // 根据数据内容决定添加哪些步骤
            val data = getData()

            when {
                data.toString().contains("permission") -> {
                    // 如果需要权限，添加权限步骤
                    val permissionStep = PermissionStep()
                    addStepAfter("ConditionalAdvancedStep", permissionStep)
                    logI("添加了权限步骤")
                }

                data.toString().contains("network") -> {
                    // 如果需要网络，添加网络步骤
                    val networkStep = NetworkStep()
                    addStepAfter("ConditionalAdvancedStep", networkStep)
                    logI("添加了网络步骤")
                }

                data.toString().contains("validation") -> {
                    // 如果需要验证，添加验证步骤
                    val validationStep = ValidationStep()
                    addStepAfter("ConditionalAdvancedStep", validationStep)
                    logI("添加了验证步骤")
                }
            }

            finish()
        }
    }

    /**
     * 权限步骤
     */
    class PermissionStep : BaseStep<String>() {

        override fun getStepId(): String = "PermissionStep"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("权限步骤开始")
            delay(1000)
            logI("权限检查完成")
            finish()
        }
    }

    /**
     * 网络步骤
     */
    class NetworkStep : BaseStep<String>() {

        override fun getStepId(): String = "NetworkStep"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("网络步骤开始")
            delay(1000)
            logI("网络连接完成")
            finish()
        }
    }

    /**
     * 验证步骤
     */
    class ValidationStep: BaseStep<String>() {

        override fun getStepId(): String = "ValidationStep"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("验证步骤开始")
            delay(1000)
            logI("数据验证完成")
            finish()
        }
    }

    /**
     * 额外步骤
     */
    class AdditionalStep: BaseStep<String>() {

        override fun getStepId(): String = "AdditionalStep"

        override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
            super.onStepStarted(stepCompletionProvider)

            logI("额外步骤开始")

            val currentData = getData()
            logI("当前数据: $currentData")

            // 模拟处理
            delay(500)

            setData("AdditionalStep处理后的数据")

            finish()
        }
    }
}

