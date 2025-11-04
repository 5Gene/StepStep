package com.google.android.libraries.wear.companion.setup

import kotlinx.coroutines.flow.StateFlow

/**
 * 获取SetupEngine当前步骤的当前值
 * 类似于SetupNavigator中使用的currentValue
 */
val StateFlow<SetupEngine.SetupStepChange>.currentValue: SetupEngine.SetupStepChange
    get() = this.value

