package com.example.modernnotepad.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.modernnotepad.databinding.DialogColorPickerBinding
import com.example.modernnotepad.model.Note

class ColorPickerDialog : DialogFragment() {
    private var _binding: DialogColorPickerBinding? = null
    private val binding get() = _binding!!

    private var selectedColor: Int = Note.DEFAULT_COLOR
    private var onColorSelected: ((Int) -> Unit)? = null

    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        onColorSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogColorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupColorGrid()
        setupButtons()

        // 设置圆角背景
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupColorGrid() {
        val gridLayout = binding.colorGrid
        val colors = Note.COLORS

        val columnCount = 5
        val rowCount = (colors.size + columnCount - 1) / columnCount

        gridLayout.columnCount = columnCount
        gridLayout.rowCount = rowCount

        for (i in colors.indices) {
            val color = colors[i]
            val imageView = ImageView(requireContext()).apply {
                // 设置固定的尺寸
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 80
                    height = 80
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }

                // 创建圆形颜色指示器
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    setStroke(2, Color.GRAY)
                }
                background = drawable

                setOnClickListener {
                    selectedColor = color
                    updateSelection()
                }
            }

            gridLayout.addView(imageView)
        }
        
        // 初始化选中状态
        updateSelection()
    }

    private fun updateSelection() {
        for (i in 0 until binding.colorGrid.childCount) {
            val view = binding.colorGrid.getChildAt(i) as ImageView
            val drawable = view.background as GradientDrawable
            
            if (i < Note.COLORS.size) {
                val color = Note.COLORS[i]

                if (color == selectedColor) {
                    drawable.setStroke(4, Color.BLACK)
                } else {
                    drawable.setStroke(2, Color.GRAY)
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnConfirm.setOnClickListener {
            onColorSelected?.invoke(selectedColor)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ColorPickerDialog"
    }
}