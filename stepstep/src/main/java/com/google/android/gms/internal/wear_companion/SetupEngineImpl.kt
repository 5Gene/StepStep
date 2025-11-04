package com.google.android.gms.internal.wear_companion

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import com.google.android.libraries.wear.companion.setup.SetupEngine
import com.google.android.libraries.wear.companion.setup.SetupStep
import com.google.android.libraries.wear.companion.setup.SetupEngine.LifecycleEvent
import com.google.android.libraries.wear.companion.setup.SetupEngine.SetupStepChange
import com.google.android.libraries.wear.companion.setup.SetupEngine.Status
import com.google.android.libraries.wear.companion.setup.SetupEngine.TransitionHint

/**
 * SetupEngine的实现类
 * 对应反编译代码中的zzglu类
 */
class SetupEngineImpl(
    private val steps: List<SetupApiImpl.SetupStepModel>
) : SetupEngine {

    private var currentStep: SetupStep? = null
    private var currentStepCompletionProvider: StepCompletionProviderImpl? = null
    private val _lifecycleEventsFlow = MutableStateFlow<LifecycleEvent?>(null)
    private val _currentStepFlow = MutableStateFlow<SetupStepChange>(
        SetupStepChange(null, TransitionHint.FORWARD, Status.NOT_STARTED)
    )
    private var isStarted = false
    private var isFinished = false
    private var currentStepIndex = -1

    // 错误和完成回调
    private var onErrorCallback: ((Throwable) -> Unit)? = null
    private var onCompleteCallback: (() -> Unit)? = null

    /**
     * 初始化引擎
     */
    fun initialize() {
        // 初始化逻辑（预留扩展点）
    }

    override fun getEngineLifecycleEvents(): Flow<LifecycleEvent> =
        _lifecycleEventsFlow.asStateFlow()
            .filterNotNull()

    override fun start() {
        if (isStarted) {
            val error = IllegalStateException("Setup flow already in progress")
            onErrorCallback?.invoke(error)
            throw error
        }

        isStarted = true
        val timestamp = System.currentTimeMillis()

        // 发送Started事件
        _lifecycleEventsFlow.value = LifecycleEvent.Started(timestamp)

        // 开始第一个步骤
        try {
            moveToNextStep()
        } catch (e: Throwable) {
            onErrorCallback?.invoke(e)
            throw e
        }
    }

    override fun getCurrentStep(): Flow<SetupStepChange> = _currentStepFlow.asStateFlow()

    override fun getLastCriticalPhaseStep(): SetupStep? {
        // 查找最后一个关键阶段步骤
        return steps.lastOrNull { it.isPointOfNoReturn }?.step
    }

    override fun abort(reason: String) {
        if (currentStepIndex < 0) {
            onErrorCallback?.invoke(RuntimeException(reason))
            return
        }
        val timestamp = System.currentTimeMillis()

        // 停止当前步骤
        currentStepCompletionProvider?.invalidate()
        currentStep?.onStepStopped()

        // 清理所有步骤
//        for (i in 0 until currentStepIndex) {
//            steps[i].step.cleanup()
//        }
        steps.forEach { it.step.cleanup() }

        // 发送Aborted事件
        _lifecycleEventsFlow.value = LifecycleEvent.Aborted(timestamp)

        // 发送步骤变化
        _currentStepFlow.value = SetupStepChange(
            currentStep = null,
            transitionHint = TransitionHint.FORWARD,
            status = Status.ABORTED
        )

        // 调用错误回调
        onErrorCallback?.invoke(RuntimeException(reason))

        isFinished = true
    }

    override fun setOnError(onError: (Throwable) -> Unit): SetupEngine {
        this.onErrorCallback = onError
        return this
    }

    override fun setOnComplete(onComplete: () -> Unit): SetupEngine {
        this.onCompleteCallback = onComplete
        return this
    }

    /**
     * 移动到下一步
     */
    private fun moveToNextStep() {
        currentStepIndex++

        if (currentStepIndex >= steps.size) {
            // 所有步骤完成
            finish()
            return
        }

        moveStep(TransitionHint.FORWARD)
    }

    private fun moveStep(hint: TransitionHint): Boolean {
        try {
            val stepModel = steps[currentStepIndex]
            currentStep = stepModel.step
            if (!currentStep!!.isAvailable()) {
                return false
            }

            // 创建StepCompletionProvider
            currentStepCompletionProvider = StepCompletionProviderImpl(this)

            // 发送步骤变化
            _currentStepFlow.value = SetupStepChange(
                currentStep = currentStep,
                transitionHint = hint,
                status = Status.IN_PROGRESS
            )

            // 启动步骤
            currentStepCompletionProvider?.let { provider ->
                currentStep?.onStepStarted(provider)
            }
            return true
        } catch (e: Throwable) {
            abort(e.message ?: e.toString())
            throw e
        }
    }

    /**
     * 完成当前步骤
     */
    fun finishCurrentStep() {
        currentStep?.onStepStopped()
        moveToNextStep()
    }

    /**
     * 导航返回上一步
     */
    fun navigateBack(): Boolean {
        if (currentStepIndex <= 0) {
            return false
        }

        currentStepCompletionProvider?.invalidate()
        currentStep?.onStepStopped()

        currentStepIndex--
        moveStep(TransitionHint.BACKWARD)
        return true
    }

    /**
     * 完成Setup流程
     */
    private fun finish() {
        val timestamp = System.currentTimeMillis()

        // 清理所有步骤
        steps.forEach { it.step.cleanup() }

        // 发送Finished事件
        _lifecycleEventsFlow.value = LifecycleEvent.Finished(timestamp)

        // 发送步骤变化
        _currentStepFlow.value = SetupStepChange(
            currentStep = null,
            transitionHint = TransitionHint.FORWARD,
            status = Status.FINISHED
        )

        isFinished = true

        // 调用完成回调
        onCompleteCallback?.invoke()
    }

    /**
     * 检查是否已完成
     */
    fun isFinished(): Boolean = isFinished
}

