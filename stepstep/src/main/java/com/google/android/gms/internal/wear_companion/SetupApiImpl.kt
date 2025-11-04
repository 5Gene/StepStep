package com.google.android.gms.internal.wear_companion

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import com.google.android.libraries.wear.companion.setup.SetupApi
import com.google.android.libraries.wear.companion.setup.SetupEngine
import com.google.android.libraries.wear.companion.setup.SetupEngineBuilder
import com.google.android.libraries.wear.companion.setup.StepType

/**
 * SetupApi的实现类
 * 对应反编译代码中的zzgin类
 */
class SetupApiImpl(
    private val stepFactory: SetupStepFactory,
    private val builderFactory: SetupEngineBuilderFactory
) : SetupApi {
    
    private var currentEngine: SetupEngineImpl? = null
    private val _lastCreatedEngineFlow = MutableStateFlow<SetupEngine?>(null)

    override fun getCurrentEngine(): SetupEngine? {
        // 检查当前引擎是否已完成
        currentEngine?.takeIf { it.isFinished() }?.let {
            currentEngine = null
        }
        return currentEngine
    }

    override fun getLastCreatedEngine(): Flow<SetupEngine> = 
        _lastCreatedEngineFlow.asStateFlow()
            .filterNotNull()

    override fun createSetupEngineBuilder(): SetupEngineBuilder {
        return builderFactory.createBuilder(this)
    }

    override fun createSetupEngineBuilder(watch: BluetoothDevice): SetupEngineBuilder {
        return builderFactory.createBuilder(this)
    }


    override fun createSetupEngineBuilder(extras: Bundle): SetupEngineBuilder {
        // 从Bundle中提取设备信息（暂时不使用）
        return createSetupEngineBuilder()
    }

    /**
     * 设置当前引擎
     */
    internal fun setCurrentEngine(engine: SetupEngineImpl) {
        this.currentEngine = engine
        _lastCreatedEngineFlow.value = engine
    }

    /**
     * 获取SetupStepFactory
     */
    internal fun getStepFactory(): SetupStepFactory = stepFactory

    /**
     * Setup步骤模型
     */
    data class SetupStepModel(
        val step: com.google.android.libraries.wear.companion.setup.SetupStep,
        val isPointOfNoReturn: Boolean
    )
}

