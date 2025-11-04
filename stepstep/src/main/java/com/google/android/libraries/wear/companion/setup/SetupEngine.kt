package com.google.android.libraries.wear.companion.setup

import kotlinx.coroutines.flow.Flow

/**
 * Setup Engine - 核心执行引擎
 * 负责管理整个Setup流程的执行
 */
interface SetupEngine {
    
    /**
     * 获取引擎生命周期事件的Flow
     */
    fun getEngineLifecycleEvents(): Flow<LifecycleEvent>

    /**
     * 启动Setup流程
     */
    fun start()

    /**
     * 获取当前步骤的Flow
     */
    fun getCurrentStep(): Flow<SetupStepChange>

    /**
     * 获取最后一个关键阶段步骤
     */
    fun getLastCriticalPhaseStep(): SetupStep?

    /**
     * 中止Setup流程
     * @param reason 中止原因
     */
    fun abort(reason: String)
    
    /**
     * 设置错误回调
     * @param onError 错误回调，当Setup流程发生错误时调用
     * @return SetupEngine自身，支持链式调用
     */
    fun setOnError(onError: (Throwable) -> Unit): SetupEngine
    
    /**
     * 设置完成回调
     * @param onComplete 完成回调，当Setup流程完成时调用
     * @return SetupEngine自身，支持链式调用
     */
    fun setOnComplete(onComplete: () -> Unit): SetupEngine

    /**
     * 生命周期事件接口
     */
    interface LifecycleEvent {
        /**
         * 获取时间戳
         */
        fun getTimestamp(): Long

        /**
         * 开始事件
         */
        data class Started(val time: Long) : LifecycleEvent {
            override fun getTimestamp() = time
        }

        /**
         * 中断事件
         */
        data class Interrupted(
            val time: Long,
            val reason: String
        ) : LifecycleEvent {
            override fun getTimestamp() = time
        }

        /**
         * 中止事件
         */
        data class Aborted(val time: Long) : LifecycleEvent {
            override fun getTimestamp() = time
        }

        /**
         * 完成事件
         */
        data class Finished(val time: Long) : LifecycleEvent {
            override fun getTimestamp() = time
        }
    }

    /**
     * Setup步骤变化
     */
    data class SetupStepChange(
        val currentStep: SetupStep?,
        val transitionHint: TransitionHint,
        val status: Status
    )

    /**
     * 转换提示
     */
    enum class TransitionHint {
        FORWARD,
        BACKWARD
    }

    /**
     * 状态
     */
    enum class Status {
        NOT_STARTED,
        IN_PROGRESS,
        FINISHED,
        ABORTED,
    }

    /**
     * 常量
     */
    companion object {
        val LAST_CRITICAL_PHASE_STEP: StepType = StepType.RESTORE
        const val DEFAULT_HEARTBEAT_TIMEOUT_MILLIS: Long = 30000
        const val DEFAULT_HEARTBEAT_INTERVAL_MILLIS: Long = 10000
    }
}

