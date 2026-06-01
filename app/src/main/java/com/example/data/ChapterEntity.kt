package com.example.data

import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "chapters", primaryKeys = ["id", "mangaId"])
data class ChapterEntity(
    val id: String,
    val mangaId: String,
    val title: String,
    val number: Double,
    val releaseDate: String,
    val isRead: Boolean = false,
    val lastReadPage: Int = 0,
    val totalPages: Int = 10
) : Serializable
