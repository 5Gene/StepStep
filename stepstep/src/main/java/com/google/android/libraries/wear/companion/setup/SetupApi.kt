package com.google.android.libraries.wear.companion.setup

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import kotlinx.coroutines.flow.Flow

/**
 * Setup API - 提供创建Setup引擎的入口
 * 这是整个Setup框架的入口点
 */
interface SetupApi {
    
    fun getCurrentEngine(): SetupEngine?

    /**
     * 获取最后创建的引擎的Flow
     */
    fun getLastCreatedEngine(): Flow<SetupEngine>

    /**
     * 创建Setup引擎构建器（使用Bundle）
     */
    fun createSetupEngineBuilder(extras: Bundle): SetupEngineBuilder

    /**
     * 创建Setup引擎构建器（使用BluetoothDevice）
     */
    fun createSetupEngineBuilder(watch: BluetoothDevice): SetupEngineBuilder


    /**
     * 创建Setup引擎构建器
     */
    fun createSetupEngineBuilder(): SetupEngineBuilder

}

