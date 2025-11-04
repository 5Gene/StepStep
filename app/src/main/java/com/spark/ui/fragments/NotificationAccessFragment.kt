package com.spark.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.spark.stepstep.R
import com.spark.ui.SetupViewModel

/**
 * 通知访问权限Fragment - 只负责UI展示
 */
class NotificationAccessFragment : Fragment() {
    
    private val viewModel: SetupViewModel by activityViewModels()
    
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var requestButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup_step, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        titleText = view.findViewById(R.id.titleText)
        descriptionText = view.findViewById(R.id.descriptionText)
        requestButton = view.findViewById(R.id.actionButton)
        
        titleText.text = "通知访问权限"
        descriptionText.text = "应用需要通知访问权限来管理通知。请点击下方按钮打开设置页面。"
        requestButton.text = "打开设置"
        
        // 检查权限状态
        checkPermissionStatus()
        
        requestButton.setOnClickListener {
            openNotificationSettings()
        }
    }
    
    private fun checkPermissionStatus() {
        val hasAccess = Settings.Secure.getString(
            requireContext().contentResolver,
            "enabled_notification_listeners"
        )?.contains(requireContext().packageName) == true
        
        if (hasAccess) {
            requestButton.isEnabled = false
            requestButton.text = "权限已授予"
            // 通知ViewModel进入下一步
            viewModel.finishCurrentStep()
        }
    }
    
    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivityForResult(intent, REQUEST_NOTIFICATION_ACCESS)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_NOTIFICATION_ACCESS) {
            // 重新检查权限状态
            checkPermissionStatus()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 当从设置页面返回时，重新检查权限
        checkPermissionStatus()
    }
    
    companion object {
        private const val REQUEST_NOTIFICATION_ACCESS = 1003
    }
}
