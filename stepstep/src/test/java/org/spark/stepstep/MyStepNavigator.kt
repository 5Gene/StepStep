package org.spark.stepstep

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import okio.Sink
import org.spark.stepstep.StepApi
import org.spark.stepstep.StepChange
import org.spark.stepstep.StepEngine
import org.spark.stepstep.StepStep
import org.spark.stepstep.toLiveData

/**
 * Step导航器示例
 * 
 * 演示如何在实际项目中使用Step框架
 * 类似于Google Step SDK的使用方式
 */
class StepNavigator private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "StepNavigator"
        private var instance: StepNavigator? = null
        
        @JvmStatic
        fun getInstance(context: Context): StepNavigator {
            var ins = instance
            if (ins == null) {
                ins = StepNavigator(context.applicationContext)
                instance = ins
            }
            return ins
        }
    }
    
    private lateinit var currentStepLiveData: LiveData<StepChange<String>?>
    
    /**
     * 步骤变化观察者
     * 负责根据当前步骤进行导航
     */
    private val StepStepChangeObserver = Observer<StepChange<String>?> { stepChange ->
        stepChange?.let { navigate(it) }
    }
    
    private lateinit var StepEngine: StepEngine<String>
    
    private var resultCallback: ((Boolean, String?) -> Unit)? = null
    
    /**
     * 开始Step流程
     * 
     * @param deviceMac 设备MAC地址
     * @param deviceModel 设备型号
     * @param skipWelcome 是否跳过欢迎页面
     * @param result 结果回调
     */
    suspend fun startStep(
        deviceMac: String,
        deviceModel: String,
        skipWelcome: Boolean = false,
        result: ((Boolean, String?) -> Unit)? = null
    ) {
        // 清理之前的Step
        if (this::StepEngine.isInitialized) {
            resultCallback = null
            StepEngine.abort(false)
        }
        
        resultCallback = result
        
        // 构建Step流程
        StepEngine = StepApi.createStepEngineBuilder<String>()
            .apply {
                // 欢迎步骤（可选）
                if (!skipWelcome) {
                    addStep(WelcomeStepStep())
                }
                
                // 权限步骤
                addStep(PermissionStepStep(
                    listOf(
                        "android.permission.BLUETOOTH",
                        "android.permission.BLUETOOTH_ADMIN",
                        "android.permission.ACCESS_FINE_LOCATION"
                    )
                ))
                
                // 设备连接步骤
                addStep(DeviceConnectionStepStep(deviceMac))
                
                // 配置同步步骤
                addStep(ConfigSyncStepStep())
                
                // 完成步骤
                addStep(CompleteStepStep())
            }
            .build()
        
        // 监听步骤变化
        currentStepLiveData = StepEngine.getStepChangeFlow().toLiveData()
        currentStepLiveData.observeForever(StepStepChangeObserver)
        
        // 启动Step流程
        StepEngine.start()
    }
    
    /**
     * 根据步骤变化进行导航
     */
    private fun navigate(stepChange: StepChange<String>) {
        println("[$TAG] Step: ${stepChange.currentStep?.getStepId()}, " +
                "Type: ${stepChange.changeType}, " +
                "Progress: ${stepChange.currentIndex + 1}/${stepChange.totalSteps}")
        
        when (stepChange.changeType) {
            StepChange.ChangeType.STARTED -> {
                println("[$TAG] Step流程已开始")
            }
            
            StepChange.ChangeType.FORWARD -> {
                // 进入新步骤
                handleStepForward(stepChange)
            }
            
            StepChange.ChangeType.BACKWARD -> {
                // 返回上一步
                handleStepBackward(stepChange)
            }
            
            StepChange.ChangeType.COMPLETED -> {
                // 流程完成
                println("[$TAG] Step流程已完成")
                removeObserver()
                resultCallback?.invoke(true, "Step completed successfully")
            }
            
            StepChange.ChangeType.ABORTED -> {
                // 流程中止
                println("[$TAG] Step流程已中止")
                removeObserver()
                resultCallback?.invoke(false, "Step aborted")
            }
        }
    }
    
    /**
     * 处理步骤前进
     */
    private fun handleStepForward(stepChange: StepChange<String>) {
        when (val step = stepChange.currentStep) {
            is WelcomeStepStep -> {
                println("[$TAG] 显示欢迎页面")
                // 启动WelcomeActivity
                // startActivity(WelcomeActivity::class.java)
            }
            
            is PermissionStepStep -> {
                println("[$TAG] 显示权限请求页面")
                // 启动PermissionActivity
                // startActivity(PermissionActivity::class.java)
            }
            
            is DeviceConnectionStepStep -> {
                println("[$TAG] 显示设备连接页面")
                // 启动DeviceConnectionActivity
                // startActivity(DeviceConnectionActivity::class.java)
            }
            
            is ConfigSyncStepStep -> {
                println("[$TAG] 显示配置同步页面")
                // 启动ConfigSyncActivity
                // startActivity(ConfigSyncActivity::class.java)
            }
            
            is CompleteStepStep -> {
                println("[$TAG] 显示完成页面")
                // 启动CompleteActivity
                // startActivity(CompleteActivity::class.java)
            }
            
            null -> {
                println("[$TAG] 没有更多步骤")
            }
        }
    }
    
    /**
     * 处理步骤后退
     */
    private fun handleStepBackward(stepChange: StepChange<String>) {
        println("[$TAG] 返回到步骤: ${stepChange.currentStep?.getStepId()}")
        // 可以在这里处理返回时的特殊逻辑
    }
    
    /**
     * 获取当前步骤
     */
    fun getCurrentStep(): StepStep<String>? {
        if (!this::StepEngine.isInitialized) {
            println("[$TAG] getCurrentStep: StepEngine not initialized")
            return null
        }
        return StepEngine.getCurrentStep()
    }
    
    /**
     * 中止Step流程
     */
    suspend fun abort() {
        if (!this::StepEngine.isInitialized) {
            println("[$TAG] abort: StepEngine not initialized")
            return
        }
        println("[$TAG] abort")
        StepEngine.abort(false)
    }
    
    /**
     * 移除观察者
     */
    private fun removeObserver() {
        if (this::currentStepLiveData.isInitialized) {
            currentStepLiveData.removeObserver(StepStepChangeObserver)
        }
    }
}