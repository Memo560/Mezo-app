package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "reading_history")
data class HistoryEntity(
    @PrimaryKey val mangaId: String,
    val chapterId: String,
    val chapterTitle: String,
    val mangaTitleAr: String,
    val coverGradientStart: Long,
    val coverGradientEnd: Long,
    val lastReadPage: Int,
    val totalPages: Int,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
