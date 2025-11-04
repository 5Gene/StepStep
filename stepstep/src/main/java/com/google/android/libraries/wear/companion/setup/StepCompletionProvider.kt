package com.google.android.libraries.wear.companion.setup

/**
 * Step Completion Provider - 提供步骤完成相关的操作
 */
interface StepCompletionProvider {
    
    /**
     * 检查是否有效
     */
    fun isValid(): Boolean

    /**
     * 完成当前步骤
     */
    fun finish()

    /**
     * 导航返回上一步
     * @return 是否成功返回
     */
    fun navigateBack(): Boolean

    /**
     * 中止Setup流程
     * @param reason 中止原因
     * @param resetWatch 是否重置手表
     */
    fun abortSetup(reason: String, resetWatch: Boolean)

    /**
     * 中止Setup流程（默认不重置手表）
     */
    fun abortSetup(reason: String) {
        abortSetup(reason, true)
    }
}

