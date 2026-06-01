package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "mangas")
data class MangaEntity(
    @PrimaryKey val id: String,
    val titleAr: String,
    val titleEn: String,
    val author: String,
    val descriptionAr: String,
    val coverGradientStart: Long, // Color Hex for dynamic drawing of gorgeous manga covers
    val coverGradientEnd: Long,   // Color Hex for dynamic drawing of gorgeous manga covers
    val status: String,           // "مستمر" (Ongoing), "مكتمل" (Completed)
    val rating: Float,
    val genres: String,           // Comma separated categories e.g. "أكشن, مغامرة, خيال"
    val sourceName: String,       // e.g. "مانجا ليك", "Mangadex"
    val isBookmarked: Boolean = false,
    val ratingVotes: Int = 1240,
    val lastReadChapterId: String? = null,
    val lastReadChapterTitle: String? = null,
    val lastReadTime: Long = 0
) : Serializable
