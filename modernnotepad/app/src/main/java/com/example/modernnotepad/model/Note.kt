package com.example.modernnotepad.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.modernnotepad.database.Converters
import java.util.Date

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val category: String = "默认",
    val color: Int = 0, // 新增：笔记颜色，使用颜色值
    val tags: List<String> = emptyList(), // 新增：标签列表
    val isPinned: Boolean = false, // 新增：是否置顶
    val reminderTime: Long? = null, // 新增：提醒时间
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time,
    val syncStatus: Int = 0 // 新增：同步状态 0-未同步 1-已同步 2-同步失败
) {
    companion object {
        // 预定义颜色选项
        val COLORS = listOf(
            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
            0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
            0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
            0xFFFFEB3B, 0xFFFFC107, 0xFFFF9800, 0xFFFF5722,
            0xFF795548, 0xFF9E9E9E, 0xFF607D8B, 0xFFFFFFFF
        ).map { it.toInt() }

        // 默认颜色（白色）
        const val DEFAULT_COLOR = 0xFFFFFFFF.toInt()
    }
}