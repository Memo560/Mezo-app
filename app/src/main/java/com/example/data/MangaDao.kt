package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {

    // --- Manga Quereies ---
    @Query("SELECT * FROM mangas ORDER BY rating DESC")
    fun getAllMangas(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM mangas WHERE isBookmarked = 1 ORDER BY lastReadTime DESC, id DESC")
    fun getLibraryMangas(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM mangas WHERE id = :mangaId LIMIT 1")
    fun getMangaByIdFlow(mangaId: String): Flow<MangaEntity?>

    @Query("SELECT * FROM mangas WHERE id = :mangaId LIMIT 1")
    suspend fun getMangaById(mangaId: String): MangaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangas(mangas: List<MangaEntity>)

    @Update
    suspend fun updateManga(manga: MangaEntity)

    @Query("UPDATE mangas SET isBookmarked = :isBookmarked WHERE id = :mangaId")
    suspend fun updateBookmarkStatus(mangaId: String, isBookmarked: Int)

    @Query("UPDATE mangas SET lastReadChapterId = :chapterId, lastReadChapterTitle = :chapterTitle, lastReadTime = :timestamp WHERE id = :mangaId")
    suspend fun updateMangaLastRead(mangaId: String, chapterId: String, chapterTitle: String, timestamp: Long)

    // --- Chapter Queries ---
    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId ORDER BY number DESC")
    fun getChaptersForMangaFlow(mangaId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId ORDER BY number DESC")
    suspend fun getChaptersForManga(mangaId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId AND id = :chapterId LIMIT 1")
    suspend fun getChapter(mangaId: String, chapterId: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET isRead = :isRead, lastReadPage = :lastPage WHERE id = :chapterId AND mangaId = :mangaId")
    suspend fun updateChapterProgress(mangaId: String, chapterId: String, isRead: Boolean, lastPage: Int)

    // --- History Queries ---
    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC")
    fun getHistoryFlow(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM reading_history WHERE mangaId = :mangaId")
    suspend fun deleteHistoryForManga(mangaId: String)

    @Query("DELETE FROM reading_history")
    suspend fun clearAllHistory()

    // --- Bulk and Backup Queries ---
    @Query("SELECT * FROM mangas")
    suspend fun getAllMangasDirect(): List<MangaEntity>

    @Query("SELECT * FROM chapters")
    suspend fun getAllChaptersDirect(): List<ChapterEntity>

    @Query("SELECT * FROM reading_history")
    suspend fun getAllHistoryDirect(): List<HistoryEntity>

    @Query("DELETE FROM mangas")
    suspend fun deleteAllMangas()

    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(history: List<HistoryEntity>)
}
