package com.google.android.gms.internal.wear_companion

import com.google.android.libraries.wear.companion.setup.StepCompletionProvider

/**
 * StepCompletionProvider的实现类
 * 对应反编译代码中的zzgkm类
 */
class StepCompletionProviderImpl(
    private val setupEngine: SetupEngineImpl
) : StepCompletionProvider {
    
    private var isValid = true

    /**
     * 使无效
     */
    internal fun invalidate() {
        this.isValid = false
    }

    override fun isValid(): Boolean = isValid

    override fun finish() {
        if (!isValid) {
            throw IllegalStateException("StepCompletionProvider already invalidated")
        }
        setupEngine.finishCurrentStep()
    }

    override fun navigateBack(): Boolean {
        if (!isValid) {
            throw IllegalStateException("StepCompletionProvider already invalidated")
        }
        return setupEngine.navigateBack()
    }

    override fun abortSetup(reason: String, resetWatch: Boolean) {
        if (!isValid) {
            throw IllegalStateException("StepCompletionProvider already invalidated")
        }
        setupEngine.abort(reason)
    }
}

