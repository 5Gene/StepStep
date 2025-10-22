package org.spark.stepstep

import org.spark.stepstep.BaseStep
import org.spark.stepstep.StepCompletionProvider

/**
 * 示例步骤集合
 * 
 * 演示如何实现自定义的Step步骤
 */

/**
 * 示例步骤1：欢迎页面
 */
class WelcomeStepStep : BaseStep<String>() {
    
    override fun getStepId(): String = "WelcomeStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        // 这里可以启动Activity、显示Dialog等
        logI("显示欢迎页面")
        
        // 模拟用户点击"下一步"
        // 实际使用中应该在用户操作后调用
        // finish()
    }
    
    override suspend fun isAvailable(): Boolean {
        // 可以根据条件决定是否显示此步骤
        return true
    }
}

/**
 * 示例步骤2：权限申请
 */
class PermissionStepStep(
    private val permissions: List<String>
) : BaseStep<String>() {
    
    private var isPermissionGranted = false
    
    override fun getStepId(): String = "PermissionStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("申请权限: ${permissions.joinToString()}")
        
        // 这里应该请求权限
        // 实际使用中需要通过Activity或Fragment请求权限
        requestPermissions()
    }
    
    private fun requestPermissions() {
        // 模拟权限请求
        // 实际使用中应该使用Android的权限请求API
        
        // 假设权限已授予
        isPermissionGranted = true
        onPermissionResult(isPermissionGranted)
    }
    
    private fun onPermissionResult(granted: Boolean) {
        if (granted) {
            logI("权限已授予，进入下一步")
            finish()
        } else {
            logW("权限被拒绝")
            // 可以选择重试、跳过或中止
            // navigateBack() // 返回上一步
            // abortStep() // 中止流程
            finish() // 或者继续（如果权限不是必须的）
        }
    }
    
    override suspend fun isAvailable(): Boolean {
        // 如果权限已经授予，可以跳过此步骤
        return !isPermissionGranted
    }
}

/**
 * 示例步骤3：设备连接
 */
class DeviceConnectionStepStep(
    private val deviceMac: String
) : BaseStep<String>() {
    
    private var isConnecting = false
    
    override fun getStepId(): String = "DeviceConnectionStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始连接设备: $deviceMac")
        connectDevice()
    }
    
    private fun connectDevice() {
        if (isConnecting) {
            logW("正在连接中，请勿重复操作")
            return
        }
        
        isConnecting = true
        
        // 模拟设备连接
        // 实际使用中应该调用蓝牙连接API
        
        // 模拟连接成功
        onConnectionResult(true)
    }
    
    private fun onConnectionResult(success: Boolean) {
        isConnecting = false
        
        if (success) {
            logI("设备连接成功")
            finish()
        } else {
            logE("设备连接失败")
            // 可以提示用户重试
            // abortStep(fromUser = false)
        }
    }
    
    override suspend fun onStepStopped() {
        super.onStepStopped()
        // 如果正在连接，可能需要取消连接操作
        if (isConnecting) {
            logI("步骤停止，取消连接操作")
            isConnecting = false
        }
    }
    
    override suspend fun cleanup() {
        super.cleanup()
        // 清理连接资源
        logI("清理连接资源")
    }
}

/**
 * 示例步骤4：配置同步
 */
class ConfigSyncStepStep : BaseStep<String>() {
    
    override fun getStepId(): String = "ConfigSyncStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("开始同步配置")
        syncConfig()
    }
    
    private fun syncConfig() {
        // 模拟配置同步
        logI("配置同步完成")
        finish()
    }
    
    override suspend fun isAvailable(): Boolean {
        // 只在首次设置时需要同步配置
        return true
    }
}

/**
 * 示例步骤5：完成页面
 */
class CompleteStepStep : BaseStep<String>() {
    
    override fun getStepId(): String = "CompleteStep"
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("显示完成页面")
        
        // 这是最后一步，用户点击完成后调用finish()
        // finish()
    }
}

/**
 * 条件步骤示例：只在特定条件下显示
 * 
 * 演示了如何使用构造函数参数控制步骤可用性
 */
class ConditionalStepStep(
    private val condition: () -> Boolean
) : BaseStep<String>() {
    
    override fun getStepId(): String = "ConditionalStep"
    
    /**
     * 重写 isAvailable() 使用传入的条件函数
     */
    override suspend fun isAvailable(): Boolean {
        val available = condition()
        logI("isAvailable: $available")
        return available
    }
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("条件步骤执行")
        finish()
    }
}

/**
 * 首次设置步骤示例
 * 
 * 只在首次设置时显示，之后会被跳过
 */
class FirstTimeStepStep(
    private val isFirstTime: Boolean
) : BaseStep<String>() {
    
    override fun getStepId(): String = "FirstTimeStepStep"
    
    /**
     * 只在首次设置时可用
     */
    override suspend fun isAvailable(): Boolean {
        logI("isAvailable: isFirstTime=$isFirstTime")
        return isFirstTime
    }
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("显示首次设置引导")
        // 显示引导页面
        finish()
    }
}

/**
 * 系统版本相关步骤示例
 * 
 * 只在特定系统版本上执行
 */
class ApiLevelDependentStep(
    private val minApiLevel: Int
) : BaseStep<String>() {
    
    override fun getStepId(): String = "ApiLevelDependentStep"
    
    /**
     * 检查系统版本是否满足要求
     */
    override suspend fun isAvailable(): Boolean {
        val currentApiLevel = android.os.Build.VERSION.SDK_INT
        val available = currentApiLevel >= minApiLevel
        logI("isAvailable: currentApiLevel=$currentApiLevel, minApiLevel=$minApiLevel, available=$available")
        return available
    }
    
    override suspend fun onStepStarted(stepCompletionProvider: StepCompletionProvider<String>) {
        super.onStepStarted(stepCompletionProvider)
        
        logI("执行需要API $minApiLevel 的功能")
        finish()
    }
}

