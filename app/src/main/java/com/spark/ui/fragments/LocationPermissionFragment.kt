package com.spark.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
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
 * 位置权限申请Fragment - 只负责UI展示
 */
class LocationPermissionFragment : Fragment() {
    
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
        
        titleText.text = "位置权限"
        descriptionText.text = "应用需要位置权限来帮助您发现附近的设备。请点击下方按钮授予位置权限。"
        requestButton.text = "授予位置权限"
        
        // 检查权限状态
        checkPermissionStatus()
        
        requestButton.setOnClickListener {
            requestLocationPermission()
        }
    }
    
    private fun checkPermissionStatus() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            requestButton.isEnabled = false
            requestButton.text = "权限已授予"
            // 通知ViewModel进入下一步
            viewModel.finishCurrentStep()
        }
    }
    
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
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
        
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LocationPermissionFragment", "位置权限已授予")
                // 通知ViewModel进入下一步
                viewModel.finishCurrentStep()
            } else {
                Log.d("LocationPermissionFragment", "位置权限被拒绝")
            }
        }
    }
    
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }
}
