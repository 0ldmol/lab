package com.example.modernnotepad.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.modernnotepad.databinding.ItemNoteBinding
import com.example.modernnotepad.model.Note
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit,
    private val onNotePin: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    private val prettyTime = PrettyTime(Locale.getDefault())

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = getItem(position)
                    onNoteClick(note)
                }
            }

            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = getItem(position)
                    onNoteLongClick(note)
                    true
                } else {
                    false
                }
            }

            binding.btnPin.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = getItem(position)
                    onNotePin(note)
                }
            }
        }

        fun bind(note: Note) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteContent.text = note.content

            // 设置卡片背景颜色
            setupCardBackground(note.color)

            // 设置颜色指示器
            setupColorIndicator(note.color)

            // 设置置顶状态
            binding.btnPin.isSelected = note.isPinned
            binding.pinIndicator.isVisible = note.isPinned

            // 设置时间显示
            val updateTime = Date(note.updatedAt)
            binding.tvNoteUpdateTime.text = prettyTime.format(updateTime)

            // 设置标签
            setupTags(note.tags)

            // 设置提醒指示器
            binding.reminderIndicator.isVisible = note.reminderTime != null
        }

        private fun setupCardBackground(color: Int) {
            // Apply the color to the card background with some transparency
            val colorWithAlpha = adjustColorAlpha(color, 0.8f) // 80% opacity
            binding.noteCard.setCardBackgroundColor(colorWithAlpha)
        }

        private fun setupColorIndicator(color: Int) {
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(color)
                cornerRadius = 8f
            }
            binding.colorIndicator.background = drawable

            // 如果颜色是白色，添加边框
            if (color == Note.DEFAULT_COLOR) {
                (binding.colorIndicator.background as GradientDrawable).setStroke(1, Color.LTGRAY)
            }
        }

        private fun setupTags(tags: List<String>) {
            binding.tagsContainer.removeAllViews()

            if (tags.isNotEmpty()) {
                binding.tagsContainer.isVisible = true
                tags.take(3).forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(binding.root.context).apply {
                        text = tag
                        isClickable = false
                        setEnsureMinTouchTargetSize(false)
                        chipMinHeight = 0f
                        setTextSize(10f)
                    }
                    binding.tagsContainer.addView(chip)
                }

                if (tags.size > 3) {
                    val moreChip = com.google.android.material.chip.Chip(binding.root.context).apply {
                        text = "+${tags.size - 3}"
                        isClickable = false
                        setEnsureMinTouchTargetSize(false)
                        chipMinHeight = 0f
                        setTextSize(10f)
                    }
                    binding.tagsContainer.addView(moreChip)
                }
            } else {
                binding.tagsContainer.isVisible = false
            }
        }

        // Helper function to adjust color alpha
        private fun adjustColorAlpha(color: Int, factor: Float): Int {
            val alpha = Math.round(Color.alpha(color) * factor)
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return Color.argb(alpha, red, green, blue)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNoteBinding.inflate(inflater, parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.title == newItem.title && 
                   oldItem.content == newItem.content && 
                   oldItem.category == newItem.category &&
                   oldItem.color == newItem.color &&
                   oldItem.isPinned == newItem.isPinned &&
                   oldItem.reminderTime == newItem.reminderTime &&
                   oldItem.updatedAt == newItem.updatedAt
        }
    }
}