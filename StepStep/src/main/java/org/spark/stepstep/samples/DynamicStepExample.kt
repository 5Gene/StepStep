package org.spark.stepstep.samples

import kotlinx.coroutines.delay
import org.spark.stepstep.BaseStep
import org.spark.stepstep.StepCompletionProvider
import org.spark.stepstep.StepApi

/**
 * 动态步骤管理示例
 * 
 * 演示如何在步骤中动态添加新步骤，并让下一步执行新增的步骤
 */
object DynamicStepExample {
    
    /**
     * 示例1：在步骤中动态添加步骤
     */
    suspend fun dynamicAddStepExample() {
        println("=== 动态添加步骤示例 ===")
        
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
}

/**
 * 第一个步骤：会动态添加中间步骤
 */
class FirstStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "FirstStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("第一个步骤开始执行")
        
        // 获取当前数据
        val currentData = getData()
        logI("当前数据: $currentData")
        
        // 修改数据
        val newData = "第一个步骤修改后的数据"
        setData(newData)
        logI("修改数据为: $newData")
        
        // 模拟一些处理时间
        delay(1000)
        
        // 动态添加中间步骤
        logI("动态添加中间步骤")
        val middleStep = MiddleStep<T>()
        addStepAfter("FirstStep", middleStep)
        
        logI("第一个步骤完成，进入下一步")
        finish()
    }
}

/**
 * 中间步骤：动态添加的步骤
 */
class MiddleStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "MiddleStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("中间步骤开始执行（这是动态添加的步骤）")
        
        // 获取数据
        val currentData = getData()
        logI("接收到的数据: $currentData")
        
        // 修改数据
        val newData = "中间步骤修改后的数据"
        setData(newData)
        logI("修改数据为: $newData")
        
        // 模拟处理时间
        delay(1500)
        
        // 可以继续动态添加更多步骤
        logI("动态添加额外步骤")
        val extraStep = ExtraStep<T>()
        addStep(extraStep)
        
        logI("中间步骤完成，进入下一步")
        finish()
    }
}

/**
 * 额外步骤：在中间步骤中动态添加的步骤
 */
class ExtraStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "ExtraStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("额外步骤开始执行（这是第二个动态添加的步骤）")
        
        // 获取数据
        val currentData = getData()
        logI("接收到的数据: $currentData")
        
        // 修改数据
        val newData = "额外步骤修改后的数据"
        setData(newData)
        logI("修改数据为: $newData")
        
        // 模拟处理时间
        delay(800)
        
        logI("额外步骤完成，进入下一步")
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
        
        logI("最后一个步骤开始执行")
        
        // 获取最终数据
        val finalData = getData()
        logI("最终数据: $finalData")
        
        // 模拟处理时间
        delay(500)
        
        logI("最后一个步骤完成，流程结束")
        finish()
    }
}

/**
 * 示例2：条件性动态添加步骤
 */
class ConditionalDynamicStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "ConditionalDynamicStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("条件性动态步骤开始执行")
        
        // 获取数据
        val currentData = getData()
        logI("当前数据: $currentData")
        
        // 根据条件动态添加不同的步骤
        val shouldAddPermissionStep = checkPermissionNeeded()
        val shouldAddNetworkStep = checkNetworkNeeded()
        
        if (shouldAddPermissionStep) {
            logI("需要权限，动态添加权限步骤")
            addStep(PermissionStep<T>())
        }
        
        if (shouldAddNetworkStep) {
            logI("需要网络，动态添加网络步骤")
            addStep(NetworkStep<T>())
        }
        
        // 修改数据
        val newData = "条件性步骤修改后的数据"
        setData(newData)
        
        delay(1000)
        
        logI("条件性动态步骤完成")
        finish()
    }
    
    private suspend fun checkPermissionNeeded(): Boolean {
        delay(200)
        return Math.random() > 0.5
    }
    
    private suspend fun checkNetworkNeeded(): Boolean {
        delay(200)
        return Math.random() > 0.3
    }
}

/**
 * 权限步骤
 */
class PermissionStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "PermissionStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("权限步骤执行")
        
        val currentData = getData()
        val newData = "$currentData + 权限已授予"
        setData(newData)
        
        delay(1000)
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
        
        logI("网络步骤执行")
        
        val currentData = getData()
        val newData = "$currentData + 网络已连接"
        setData(newData)
        
        delay(1200)
        finish()
    }
}

/**
 * 示例3：在步骤中动态添加多个步骤
 */
class MultiDynamicStep<T> : BaseStep<T>() {
    
    override fun getStepId(): String = "MultiDynamicStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("多步骤动态添加开始执行")
        
        // 动态添加多个步骤
        val stepsToAdd = listOf(
            Step1<T>(),
            Step2<T>(),
            Step3<T>()
        )
        
        stepsToAdd.forEach { step ->
            logI("动态添加步骤: ${step.getStepId()}")
            addStep(step)
        }
        
        val currentData = getData()
        val newData = "$currentData + 多步骤已添加"
        setData(newData)
        
        delay(500)
        
        logI("多步骤动态添加完成")
        finish()
    }
}

class Step1<T> : BaseStep<T>() {
    override fun getStepId(): String = "Step1"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        logI("Step1 执行")
        delay(300)
        finish()
    }
}

class Step2<T> : BaseStep<T>() {
    override fun getStepId(): String = "Step2"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        logI("Step2 执行")
        delay(300)
        finish()
    }
}

class Step3<T> : BaseStep<T>() {
    override fun getStepId(): String = "Step3"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<T>) {
        super.onStepStarted(stepCompletionProvider)
        logI("Step3 执行")
        delay(300)
        finish()
    }
}
