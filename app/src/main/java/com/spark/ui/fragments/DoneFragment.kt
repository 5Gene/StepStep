package com.spark.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.spark.stepstep.R

/**
 * 完成Fragment - 只负责UI展示
 */
class DoneFragment : Fragment() {
    
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var finishButton: Button
    
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
        finishButton = view.findViewById(R.id.actionButton)
        
        titleText.text = "设置完成"
        descriptionText.text = "恭喜！所有设置步骤已完成。您现在可以使用应用的全部功能了。"
        finishButton.text = "完成"
        
        finishButton.setOnClickListener {
            requireActivity().finish()
        }
    }
}
