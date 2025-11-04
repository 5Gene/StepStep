package com.google.android.gms.internal.wear_companion

import com.google.android.libraries.wear.companion.setup.SetupStep
import com.google.android.libraries.wear.companion.setup.StepType

/**
 * Setup步骤工厂接口
 */
interface SetupStepFactory {
    /**
     * 根据步骤类型创建步骤
     */
    fun createStep(stepType: StepType): SetupStep
}

