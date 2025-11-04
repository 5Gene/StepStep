package com.google.android.gms.internal.wear_companion

/**
 * SetupEngineBuilder工厂
 */
class SetupEngineBuilderFactory {
    /**
     * 创建基本的构建器
     */
    fun createBuilder(setupApi: SetupApiImpl): SetupEngineBuilderImpl {
        return SetupEngineBuilderImpl(setupApi)
    }
}

