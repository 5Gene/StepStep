package com.spark.stepstep.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.spark.stepstep.R
import com.google.android.libraries.wear.companion.setup.SetupStep
import com.spark.ui.SetupViewModel
import com.spark.ui.fragments.BluetoothPermissionFragment
import com.spark.ui.fragments.DoneFragment
import com.spark.ui.fragments.LocationPermissionFragment
import com.spark.ui.fragments.NotificationAccessFragment
import com.spark.ui.steps.BluetoothPermissionStep
import com.spark.ui.steps.DoneStep
import com.spark.ui.steps.LocationPermissionStep
import com.spark.ui.steps.NotificationAccessStep

/**
 * Setup主Activity - 使用ViewModel管理逻辑，UI只负责展示
 */
class SetupActivity : AppCompatActivity() {
    
    private lateinit var viewModel: SetupViewModel
    private lateinit var backButton: Button
    private lateinit var progressBar: ProgressBar
    
    // 步骤到Fragment的映射
    private val stepFragmentMap = mapOf<Class<out SetupStep>, () -> Fragment>(
        LocationPermissionStep::class.java to { LocationPermissionFragment() },
        BluetoothPermissionStep::class.java to { BluetoothPermissionFragment() },
        NotificationAccessStep::class.java to { NotificationAccessFragment() },
        DoneStep::class.java to { DoneFragment() }
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        
        // 创建ViewModel
        viewModel = ViewModelProvider(this)[SetupViewModel::class.java]
        
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.progressBar)
        
        // 监听步骤变化
        viewModel.currentStep.observe(this, Observer { change ->
            change?.let {
                showFragmentForStep(it)
                updateProgress(it)
            }
        })
        
        // 监听进度变化
        viewModel.progress.observe(this, Observer { progress ->
            progressBar.progress = progress
        })
        
        // 监听是否可以返回
        viewModel.canGoBack.observe(this, Observer { canGoBack ->
            backButton.isEnabled = canGoBack
        })
        
        // 设置返回按钮
        backButton.setOnClickListener {
            // 通知ViewModel回退，ViewModel会更新状态并触发LiveData
            // LiveData会通知Activity，在showFragmentForStep中处理Fragment回退
            val canGoBack = viewModel.navigateBack()
            if (!canGoBack) {
                finish()
            }
        }
        
        // 启动Setup流程
        viewModel.startSetup()
    }
    
    /**
     * 根据步骤显示对应的Fragment，使用回退栈管理
     */
    private fun showFragmentForStep(change: com.google.android.libraries.wear.companion.setup.SetupEngine.SetupStepChange) {
        change.currentStep?.let { step ->
            val fragmentFactory = stepFragmentMap[step.javaClass]
            
            if (fragmentFactory != null) {
                when (change.transitionHint) {
                    com.google.android.libraries.wear.companion.setup.SetupEngine.TransitionHint.FORWARD -> {
                        // 前进：添加新Fragment到栈，从右侧滑入
                        addFragmentToStack(step, fragmentFactory)
                        android.util.Log.d("SetupActivity", "前进：添加Fragment: ${step.javaClass.simpleName}, 栈大小: ${supportFragmentManager.backStackEntryCount}")
                    }
                    com.google.android.libraries.wear.companion.setup.SetupEngine.TransitionHint.BACKWARD -> {
                        // 后退：从栈中弹出Fragment，系统会自动使用相反方向的动画
                        if (supportFragmentManager.backStackEntryCount > 0) {
                            supportFragmentManager.popBackStack()
                            android.util.Log.d("SetupActivity", "后退：弹出Fragment，栈大小: ${supportFragmentManager.backStackEntryCount}")
                        } else {
                            // 如果栈为空，说明是第一次或异常情况，直接添加Fragment
                            addFragmentToStack(step, fragmentFactory, addToBackStack = false)
                            android.util.Log.w("SetupActivity", "后退：栈为空，直接添加Fragment: ${step.javaClass.simpleName}")
                        }
                    }
                }
                
                // 更新ViewModel中的StepCompletionProvider
                updateStepCompletionProvider(step)
            } else {
                android.util.Log.w("SetupActivity", "未找到对应的Fragment: ${step.javaClass.simpleName}")
            }
        }
    }
    
    /**
     * 添加Fragment到回退栈（前进）
     */
    private fun addFragmentToStack(step: SetupStep, fragmentFactory: () -> Fragment, addToBackStack: Boolean = true) {
        val fragment = fragmentFactory()
        val tag = step.javaClass.simpleName

        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,  // enter: 从右侧滑入
                R.anim.slide_out_left,  // exit: 向左滑出
                android.R.anim.slide_in_left,   // popEnter: 从左侧滑入（回退时）
                android.R.anim.slide_out_right // popExit: 向右滑出（回退时）
            )
            .replace(R.id.fragmentContainer, fragment, tag)
        
        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }
        
        transaction.commit()
    }
    
    /**
     * 更新ViewModel中的StepCompletionProvider
     */
    private fun updateStepCompletionProvider(step: SetupStep) {
        val provider = when (step) {
            is LocationPermissionStep -> step.getStepCompletionProvider()
            is BluetoothPermissionStep -> step.getStepCompletionProvider()
            is NotificationAccessStep -> step.getStepCompletionProvider()
            is DoneStep -> step.getStepCompletionProvider()
            else -> null
        }
        viewModel.setCurrentStepCompletionProvider(provider)
    }
    
    /**
     * 更新进度
     */
    private fun updateProgress(change: com.google.android.libraries.wear.companion.setup.SetupEngine.SetupStepChange) {
        // 进度由ViewModel的LiveData自动更新
    }
    
    override fun onBackPressed() {
        // 通知ViewModel回退，ViewModel会更新状态并触发LiveData
        // LiveData会通知Activity，在showFragmentForStep中处理Fragment回退
        val canGoBack = viewModel.navigateBack()
        if (!canGoBack) {
            super.onBackPressed()
        }
    }
}
