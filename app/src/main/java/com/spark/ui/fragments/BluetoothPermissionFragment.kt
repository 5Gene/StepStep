package com.spark.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.spark.stepstep.R
import com.spark.ui.SetupViewModel

/**
 * 蓝牙权限申请Fragment - 只负责UI展示
 */
class BluetoothPermissionFragment : Fragment() {
    
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
        
        titleText.text = "蓝牙权限"
        descriptionText.text = "应用需要蓝牙权限来连接和配对设备。请点击下方按钮授予蓝牙权限。"
        requestButton.text = "授予蓝牙权限"
        
        // 检查权限状态
        checkPermissionStatus()
        
        requestButton.setOnClickListener {
            requestBluetoothPermission()
        }
    }
    
    private fun checkPermissionStatus() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.BLUETOOTH)
        }
        
        val hasPermission = permissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }
        
        if (hasPermission) {
            requestButton.isEnabled = false
            requestButton.text = "权限已授予"
            // 通知ViewModel进入下一步
            viewModel.finishCurrentStep()
        }
    }
    
    private fun requestBluetoothPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.BLUETOOTH)
        }
        
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            REQUEST_BLUETOOTH_PERMISSION
        )
    }
    
    override fun onResume() {
        super.onResume()
        // 当从权限设置返回时，重新检查权限
        checkPermissionStatus()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("BluetoothPermissionFragment", "蓝牙权限已授予")
                // 通知ViewModel进入下一步
                viewModel.finishCurrentStep()
            } else {
                Log.d("BluetoothPermissionFragment", "蓝牙权限被拒绝")
            }
        }
    }
    
    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSION = 1002
    }
}
