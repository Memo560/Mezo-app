package com.example.data

data class BackupPayload(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val username: String = "",
    val score: Double = 0.0,
    val totalMinutesRead: Int = 340,
    val readingStreak: Int = 5,
    val trackingServicesJson: String = "",
    val mangas: List<MangaBackupEntity> = emptyList(),
    val chapters: List<ChapterBackupEntity> = emptyList(),
    val history: List<HistoryBackupEntity> = emptyList()
)

data class MangaBackupEntity(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val author: String,
    val descriptionAr: String,
    val coverGradientStart: Long,
    val coverGradientEnd: Long,
    val status: String,
    val rating: Float,
    val genres: String,
    val sourceName: String,
    val isBookmarked: Boolean,
    val ratingVotes: Int,
    val lastReadChapterId: String? = null,
    val lastReadChapterTitle: String? = null,
    val lastReadTime: Long = 0
)

data class ChapterBackupEntity(
    val id: String,
    val mangaId: String,
    val title: String,
    val number: Double,
    val releaseDate: String,
    val isRead: Boolean,
    val lastReadPage: Int,
    val totalPages: Int
)

data class HistoryBackupEntity(
    val mangaId: String,
    val chapterId: String,
    val chapterTitle: String,
    val mangaTitleAr: String,
    val coverGradientStart: Long,
    val coverGradientEnd: Long,
    val lastReadPage: Int,
    val totalPages: Int,
    val timestamp: Long
)
