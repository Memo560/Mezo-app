package com.example.data

import kotlinx.coroutines.flow.Flow

class MangaRepository(private val mangaDao: MangaDao) {

    val allMangas: Flow<List<MangaEntity>> = mangaDao.getAllMangas()
    val libraryMangas: Flow<List<MangaEntity>> = mangaDao.getLibraryMangas()
    val readingHistory: Flow<List<HistoryEntity>> = mangaDao.getHistoryFlow()

    fun getMangaFlow(mangaId: String): Flow<MangaEntity?> {
        return mangaDao.getMangaByIdFlow(mangaId)
    }

    fun getChaptersFlow(mangaId: String): Flow<List<ChapterEntity>> {
        return mangaDao.getChaptersForMangaFlow(mangaId)
    }

    suspend fun insertMangas(mangas: List<MangaEntity>) {
        mangaDao.insertMangas(mangas)
    }

    suspend fun insertChapters(chapters: List<ChapterEntity>) {
        mangaDao.insertChapters(chapters)
    }

    suspend fun toggleBookmark(mangaId: String, currentStatus: Boolean) {
        mangaDao.updateBookmarkStatus(mangaId, if (currentStatus) 0 else 1)
    }

    suspend fun getMangaById(mangaId: String): MangaEntity? {
        return mangaDao.getMangaById(mangaId)
    }

    suspend fun getChapter(mangaId: String, chapterId: String): ChapterEntity? {
        return mangaDao.getChapter(mangaId, chapterId)
    }

    suspend fun updateChapterProgress(mangaId: String, chapterId: String, lastPage: Int, totalPages: Int, isRead: Boolean) {
        // Update chapter status
        mangaDao.updateChapterProgress(mangaId, chapterId, isRead, lastPage)

        val manga = mangaDao.getMangaById(mangaId)
        val chapter = mangaDao.getChapter(mangaId, chapterId)
        
        if (manga != null && chapter != null) {
            // Update last read details on the manga itself
            mangaDao.updateMangaLastRead(mangaId, chapterId, chapter.title, System.currentTimeMillis())

            // Insert dynamic history row
            val history = HistoryEntity(
                mangaId = mangaId,
                chapterId = chapterId,
                chapterTitle = chapter.title,
                mangaTitleAr = manga.titleAr,
                coverGradientStart = manga.coverGradientStart,
                coverGradientEnd = manga.coverGradientEnd,
                lastReadPage = lastPage,
                totalPages = totalPages,
                timestamp = System.currentTimeMillis()
            )
            mangaDao.insertHistory(history)
        }
    }

    suspend fun deleteHistory(mangaId: String) {
        mangaDao.deleteHistoryForManga(mangaId)
    }

    suspend fun clearHistory() {
        mangaDao.clearAllHistory()
    }

    suspend fun getAllMangasDirect(): List<MangaEntity> = mangaDao.getAllMangasDirect()
    suspend fun getAllChaptersDirect(): List<ChapterEntity> = mangaDao.getAllChaptersDirect()
    suspend fun getAllHistoryDirect(): List<HistoryEntity> = mangaDao.getAllHistoryDirect()

    suspend fun deleteAllMangas() = mangaDao.deleteAllMangas()
    suspend fun deleteAllChapters() = mangaDao.deleteAllChapters()
    suspend fun insertHistoryList(history: List<HistoryEntity>) = mangaDao.insertHistoryList(history)
}
