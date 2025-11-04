package com.google.android.gms.internal.wear_companion

import com.google.android.libraries.wear.companion.setup.SetupEngine
import com.google.android.libraries.wear.companion.setup.SetupEngineBuilder
import com.google.android.libraries.wear.companion.setup.SetupStep
import com.google.android.libraries.wear.companion.setup.StepType

/**
 * SetupEngineBuilder的实现类
 * 对应反编译代码中的zzgim类
 */
class SetupEngineBuilderImpl(
    private val setupApi: SetupApiImpl
) : SetupEngineBuilder {

    // 步骤映射
    private val stepsMap = mutableMapOf<StepType, MutableList<SetupStepModel>>()



    override fun addAfter(stepType: StepType): SetupEngineBuilder.StepsBuilder {
        return StepsBuilderImpl(this, stepType)
    }

    override fun build(): SetupEngine {
        // 构建步骤列表
        val steps = buildStepsList()
        
        val engine = SetupEngineImpl(
            steps = steps
        )
        
        engine.initialize()
        setupApi.setCurrentEngine(engine)
        
        return engine
    }

    /**
     * 构建步骤列表
     */
    private fun buildStepsList(): List<SetupApiImpl.SetupStepModel> {
        return stepsMap.values.flatten().map { stepModel ->
            SetupApiImpl.SetupStepModel(
                step = stepModel.step,
                isPointOfNoReturn = stepModel.isPointOfNoReturn
            )
        }
    }

    /**
     * 添加步骤到映射
     */
    internal fun addSteps(afterStepType: StepType, stepModels: List<SetupStepModel>) {
        stepsMap[afterStepType] = stepModels.toMutableList()
    }


    /**
     * Setup步骤模型
     */
    data class SetupStepModel(
        val step: SetupStep,
        val isPointOfNoReturn: Boolean
    )

    /**
     * StepsBuilder的实现类
     */
    private class StepsBuilderImpl(
        private val builder: SetupEngineBuilderImpl,
        private val afterStepType: StepType
    ) : SetupEngineBuilder.StepsBuilder {
        
        private val steps = mutableListOf<SetupStepModel>()

        override fun step(setupStep: SetupStep, isPointOfNoReturn: Boolean): SetupEngineBuilder.StepsBuilder {
            steps.add(SetupStepModel(setupStep, isPointOfNoReturn))
            return this
        }

        override fun step(setupStep: SetupStep): SetupEngineBuilder.StepsBuilder {
            return step(setupStep, false)
        }

        override fun step(stepType: StepType): SetupEngineBuilder.StepsBuilder {
            // 使用StepFactory创建步骤
            val stepFactory = builder.setupApi.getStepFactory()
            val step = stepFactory.createStep(stepType)
            return step(step, stepType.isPointOfNoReturn)
        }

        override fun requestEndCriticalPhase(): SetupEngineBuilder.StepsBuilder {
            // 关键阶段结束步骤暂时不使用
            return this
        }

        override fun buildSteps(): SetupEngineBuilder {
            if (steps.isEmpty()) {
                throw IllegalStateException("No SetupSteps have been added. Please add a SetupStep using step()")
            }
            builder.addSteps(afterStepType, steps)
            return builder
        }
    }
}

