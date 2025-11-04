package com.google.android.libraries.wear.companion.setup

/**
 * Setup Engine Builder - 构建Setup引擎的构建器
 */
interface SetupEngineBuilder {

    /**
     * 在指定步骤类型之后添加步骤
     */
    fun addAfter(stepType: StepType): StepsBuilder

    /**
     * 构建Setup引擎
     */
    fun build(): SetupEngine

    /**
     * 步骤构建器
     */
    interface StepsBuilder {
        /**
         * 添加步骤（带是否不可返回点）
         */
        fun step(setupStep: SetupStep, isPointOfNoReturn: Boolean): StepsBuilder

        /**
         * 添加步骤
         */
        fun step(setupStep: SetupStep): StepsBuilder

        /**
         * 添加步骤（使用步骤类型）
         */
        fun step(stepType: StepType): StepsBuilder


        /**
         * 请求结束关键阶段
         */
        fun requestEndCriticalPhase(): StepsBuilder

        /**
         * 构建步骤并返回构建器
         */
        fun buildSteps(): SetupEngineBuilder
    }
}

