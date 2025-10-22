package org.spark.stepstep

import kotlinx.coroutines.delay
import org.spark.stepstep.BaseStep
import org.spark.stepstep.StepCompletionProvider
import java.io.IOException

/**
 * 协程示例步骤集合
 * 
 * 演示如何在协程环境中使用Step框架
 * 包括异步操作、错误处理、数据传递等
 */

/**
 * 示例步骤1：异步数据加载
 */
class AsyncDataLoadStep : BaseStep<String>() {
    
    override fun getStepId(): String = "AsyncDataLoadStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始异步加载数据")
        
        try {
            // 模拟异步数据加载
            val data = loadDataAsync()
            logI("数据加载完成: $data")
            
            // 完成步骤
            finish()
        } catch (e: Exception) {
            logE("数据加载失败: ${e.message}")
            error(e)
        }
    }
    
    private suspend fun loadDataAsync(): String {
        // 模拟网络请求延迟
        delay(2000)
        return "Loaded data from server"
    }
}

/**
 * 示例步骤2：权限请求（协程版本）
 */
class CoroutinePermissionStep(
    private val permissions: List<String>
) : BaseStep<String>() {
    
    private var isPermissionGranted = false
    
    override fun getStepId(): String = "CoroutinePermissionStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("申请权限: ${permissions.joinToString()}")
        
        try {
            // 模拟权限请求
            val granted = requestPermissionsAsync()
            
            if (granted) {
                logI("权限已授予，进入下一步")
                finish()
            } else {
                logW("权限被拒绝")
                // 可以选择重试、跳过或中止
                navigateBack()
            }
        } catch (e: Exception) {
            logE("权限请求失败: ${e.message}")
            error(e)
        }
    }
    
    private suspend fun requestPermissionsAsync(): Boolean {
        // 模拟权限请求延迟
        delay(1500)
        // 模拟权限授予
        return true
    }
    
    override suspend fun isAvailable(): Boolean {
        return !isPermissionGranted
    }
}

/**
 * 示例步骤3：设备连接（协程版本）
 */
class CoroutineDeviceConnectionStep(
    private val deviceMac: String
) : BaseStep<String>() {
    
    private var isConnecting = false
    
    override fun getStepId(): String = "CoroutineDeviceConnectionStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始连接设备: $deviceMac")
        
        try {
            val connected = connectDeviceAsync()
            
            if (connected) {
                logI("设备连接成功")
                finish()
            } else {
                logE("设备连接失败")
                error(IOException("Device connection failed"))
            }
        } catch (e: Exception) {
            logE("设备连接异常: ${e.message}")
            error(e)
        }
    }
    
    private suspend fun connectDeviceAsync(): Boolean {
        if (isConnecting) {
            logW("正在连接中，请勿重复操作")
            return false
        }
        
        isConnecting = true
        
        try {
            // 模拟设备连接延迟
            delay(3000)
            
            // 模拟连接结果（80%成功率）
            val success = Math.random() > 0.2
            logI("连接结果: $success")
            
            return success
        } finally {
            isConnecting = false
        }
    }
    
    override suspend fun onStepStopped() {
        super.onStepStopped()
        // 如果正在连接，取消连接操作
        if (isConnecting) {
            logI("步骤停止，取消连接操作")
            isConnecting = false
        }
    }
    
    override suspend fun cleanup() {
        super.cleanup()
        // 清理连接资源
        logI("清理连接资源")
        isConnecting = false
    }
}

/**
 * 示例步骤4：数据同步（协程版本）
 */
class CoroutineDataSyncStep : BaseStep<String>() {
    
    override fun getStepId(): String = "CoroutineDataSyncStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始同步数据")
        
        try {
            val syncResult = syncDataAsync()
            logI("数据同步完成: $syncResult")
            finish()
        } catch (e: Exception) {
            logE("数据同步失败: ${e.message}")
            error(e)
        }
    }
    
    private suspend fun syncDataAsync(): String {
        // 模拟数据同步延迟
        delay(2500)
        return "Data synced successfully"
    }
}

/**
 * 示例步骤5：完成页面（协程版本）
 */
class CoroutineCompleteStep : BaseStep<String>() {
    
    override fun getStepId(): String = "CoroutineCompleteStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("显示完成页面")
        
        // 模拟显示完成页面
        delay(1000)
        
        // 这是最后一步，用户点击完成后调用finish()
        // finish()
    }
}

/**
 * 示例步骤6：条件步骤（协程版本）
 */
class CoroutineConditionalStep(
    private val condition: suspend () -> Boolean
) : BaseStep<String>() {
    
    override fun getStepId(): String = "CoroutineConditionalStep"
    
    override suspend fun isAvailable(): Boolean {
        val available = condition()
        logI("isAvailable: $available")
        return available
    }
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("条件步骤执行")
        
        // 模拟一些异步操作
        delay(500)
        
        finish()
    }
}

/**
 * 示例步骤7：错误处理步骤
 */
class ErrorHandlingStep: BaseStep<String>() {
    
    override fun getStepId(): String = "ErrorHandlingStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始错误处理步骤")
        
        try {
            // 模拟可能出错的操作
            val result = performRiskyOperation()
            logI("操作成功: $result")
            finish()
        } catch (e: Exception) {
            logE("操作失败: ${e.message}")
            // 使用error方法报告错误
            error(e)
        }
    }
    
    private suspend fun performRiskyOperation(): String {
        // 模拟网络请求
        delay(1000)
        
        // 模拟随机错误（30%概率）
        if (Math.random() < 0.3) {
            throw IOException("Network request failed")
        }
        
        return "Operation completed successfully"
    }
}

/**
 * 示例步骤8：动态步骤管理
 */
class DynamicStepManagementStep: BaseStep<String>() {
    
    override fun getStepId(): String = "DynamicStepManagementStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始动态步骤管理")
        
        try {
            // 模拟根据条件动态添加步骤
            val shouldAddExtraStep = checkCondition()
            
            if (shouldAddExtraStep) {
                logI("条件满足，将添加额外步骤")
                // 这里可以通过某种方式通知引擎添加步骤
                // 实际实现中可能需要通过回调或其他机制
            }
            
            finish()
        } catch (e: Exception) {
            logE("动态步骤管理失败: ${e.message}")
            error(e)
        }
    }
    
    private suspend fun checkCondition(): Boolean {
        delay(500)
        return Math.random() > 0.5
    }
}

/**
 * 示例步骤9：数据传递步骤
 */
class DataPassingStep(
    private val dataKey: String,
    private val dataValue: String
) : BaseStep<String>() {
    
    override fun getStepId(): String = "DataPassingStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始数据传递步骤")
        
        try {
            // 模拟数据处理
            val processedData = processData(dataValue)
            logI("数据处理完成: $processedData")
            
            // 这里可以将数据传递给下一个步骤
            // 实际实现中需要通过引擎的数据容器
            
            finish()
        } catch (e: Exception) {
            logE("数据处理失败: ${e.message}")
            error(e)
        }
    }
    
    private suspend fun processData(data: String): String {
        delay(800)
        return data
    }
}

/**
 * 示例步骤10：超时处理步骤
 */
class TimeoutHandlingStep : BaseStep<String>() {
    
    override fun getStepId(): String = "TimeoutHandlingStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始超时处理步骤")
        
        try {
            // 使用withTimeout处理超时
            val result = kotlinx.coroutines.withTimeout(5000) {
                performLongRunningOperation()
            }
            
            logI("操作完成: $result")
            finish()
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            logE("操作超时")
            error(e)
        } catch (e: Exception) {
            logE("操作失败: ${e.message}")
            error(e)
        }
    }
    
    private suspend fun performLongRunningOperation(): String {
        // 模拟长时间运行的操作
        delay(3000)
        return "Long running operation completed"
    }
}

