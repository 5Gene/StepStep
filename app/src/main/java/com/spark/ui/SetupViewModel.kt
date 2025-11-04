package com.spark.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.google.android.gms.internal.wear_companion.*
import com.google.android.libraries.wear.companion.setup.*
import com.spark.ui.steps.BluetoothPermissionStep
import com.spark.ui.steps.DefaultSetupStep
import com.spark.ui.steps.DoneStep
import com.spark.ui.steps.LocationPermissionStep
import com.spark.ui.steps.NotificationAccessStep

/**
 * Setup ViewModel - 管理Setup流程的所有逻辑
 * 按照SetupNavigator的方式使用setup框架
 */
class SetupViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application.applicationContext
    
    // SetupApi实例
    private val setupApi: SetupApi by lazy {
        createSetupApi()
    }
    
    // 当前Setup引擎
    private var setupEngine: SetupEngine? = null
    
    // 当前步骤的MutableLiveData（从Flow转换）
    private val _currentStep = MutableLiveData<SetupEngine.SetupStepChange?>()
    val currentStep: LiveData<SetupEngine.SetupStepChange?> = _currentStep
    
    // 生命周期事件的MutableLiveData（从Flow转换）
    private val _lifecycleEvents = MutableLiveData<SetupEngine.LifecycleEvent?>()
    val lifecycleEvents: LiveData<SetupEngine.LifecycleEvent?> = _lifecycleEvents
    
    // 进度LiveData
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress
    
    // 是否可以返回
    private val _canGoBack = MutableLiveData<Boolean>()
    val canGoBack: LiveData<Boolean> = _canGoBack
    
    // 当前步骤的StepCompletionProvider（用于上一步和下一步）
    private var currentStepCompletionProvider: StepCompletionProvider? = null
    
    // 当前步骤索引
    private var currentStepIndex = -1
    
    /**
     * 创建SetupApi实例
     */
    private fun createSetupApi(): SetupApi {
        // 创建StepFactory
        val stepFactory = object : SetupStepFactory {
            override fun createStep(stepType: StepType): SetupStep {
                return when (stepType) {
                    StepType.LOCATION_PERMISSION -> LocationPermissionStep(context)
                    StepType.BLUETOOTH_PERMISSION -> BluetoothPermissionStep(context)
                    StepType.NOTIFICATION_ACCESS -> NotificationAccessStep(context)
                    StepType.DONE -> DoneStep(context)
                    else -> DefaultSetupStep(context, stepType)
                }
            }
        }
        
        // 创建BuilderFactory
        val builderFactory = SetupEngineBuilderFactory()
        
        return SetupApiImpl(stepFactory, builderFactory)
    }
    
    /**
     * 启动Setup流程（按照SetupNavigator的方式）
     */
    fun startSetup() {
        Log.i(TAG, "starting setup")
        
        // 重置步骤索引
        currentStepIndex = -1
        
        // 使用SetupEngineBuilder创建引擎（按照SetupNavigator的方式）
        setupEngine = setupApi.createSetupEngineBuilder()
            .addAfter(StepType.TERMS_OF_SERVICE)
            .step(LocationPermissionStep(context))
            .step(BluetoothPermissionStep(context))
            .step(NotificationAccessStep(context))
            .step(DoneStep(context))
            .buildSteps()
            .build()
        
        // 收集步骤变化Flow并转换为LiveData
        setupEngine!!.getCurrentStep()
            .onEach { change ->
                Log.d(TAG, "SDK setup step changed")
                _currentStep.postValue(change)
                updateProgress(change)
                updateCanGoBack(change)
            }
            .launchIn(viewModelScope)
        
        // 收集生命周期事件Flow并转换为LiveData
        setupEngine!!.getEngineLifecycleEvents()
            .onEach { event ->
                _lifecycleEvents.postValue(event)
            }
            .launchIn(viewModelScope)
        
        // 设置错误和完成回调（链式调用）
        setupEngine!!
            .setOnError { error ->
                Log.e(TAG, "Setup error", error)
                _lifecycleEvents.postValue(null)
            }
            .setOnComplete {
                Log.i(TAG, "Setup completed")
            }
            .start()
    }
    
    /**
     * 获取当前Setup步骤（按照SetupNavigator的方式）
     */
    fun getCurrentStep(): SetupStep? {
        // 从LiveData获取当前值
        val currentValue = _currentStep.value
        return currentValue?.currentStep
    }
    
    /**
     * 完成当前步骤（下一步）
     */
    fun finishCurrentStep() {
        currentStepCompletionProvider?.finish()
    }
    
    /**
     * 返回上一步
     */
    fun navigateBack(): Boolean {
        return currentStepCompletionProvider?.navigateBack() ?: false
    }
    
    /**
     * 设置当前步骤的StepCompletionProvider（由Activity调用）
     */
    fun setCurrentStepCompletionProvider(provider: StepCompletionProvider?) {
        currentStepCompletionProvider = provider
    }
    
    /**
     * 中止Setup流程
     */
    fun abort() {
        setupEngine?.abort("User aborted")
        removeObserver()
    }
    
    /**
     * 移除Observer（清理资源）
     * 注意：Flow收集会自动在viewModelScope取消时停止，无需手动移除
     */
    private fun removeObserver() {
        // Flow收集会自动在viewModelScope取消时停止
    }
    
    /**
     * 更新进度
     */
    private fun updateProgress(change: SetupEngine.SetupStepChange?) {
        if (change == null) return
        
        val progressValue = when (change.currentStep) {
            is LocationPermissionStep -> 25
            is BluetoothPermissionStep -> 50
            is NotificationAccessStep -> 75
            is DoneStep -> 100
            else -> 0
        }
        _progress.postValue(progressValue)
    }
    
    /**
     * 更新是否可以返回
     */
    private fun updateCanGoBack(change: SetupEngine.SetupStepChange) {
        // 根据转换提示更新步骤索引
        when (change.transitionHint) {
            SetupEngine.TransitionHint.FORWARD -> currentStepIndex++
            SetupEngine.TransitionHint.BACKWARD -> currentStepIndex--
        }
        val canGoBack = currentStepIndex > 0
        _canGoBack.postValue(canGoBack)
    }
    
    override fun onCleared() {
        super.onCleared()
        removeObserver()
    }
    
    companion object {
        private const val TAG = "SetupViewModel"
    }
}
