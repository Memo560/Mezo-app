package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ChapterEntity
import com.example.data.HistoryEntity
import com.example.data.MangaEntity
import com.example.data.MangaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class MangaUiScreen {
    object Library : MangaUiScreen()
    object Sources : MangaUiScreen()
    object History : MangaUiScreen()
    object Profile : MangaUiScreen()
    data class Detail(val mangaId: String) : MangaUiScreen()
    data class Reader(val mangaId: String, val chapterId: String) : MangaUiScreen()
}

// Data models for the advanced feature states
data class UserProfile(
    val username: String,
    val email: String,
    val score: Int = 180,
    val joinedDate: String = "يونيو 2026",
    val avatarRes: String = "avatar_1"
)

data class KeiyoushiExtension(
    val id: String,
    val name: String,
    val sourceName: String,
    val version: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean = true,
    val hasUpdate: Boolean = false
)

data class MangaComment(
    val id: String,
    val chapterId: String?,
    val mangaId: String,
    val author: String,
    val avatarIndex: Int,
    val content: String,
    val timestamp: String,
    val likes: Int = 12,
    val isLiked: Boolean = false
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MangaViewModel(private val repository: MangaRepository) : ViewModel() {

    // --- State: Current Screen ---
    private val _currentScreen = MutableStateFlow<MangaUiScreen>(MangaUiScreen.Library)
    val currentScreen: StateFlow<MangaUiScreen> = _currentScreen.asStateFlow()

    private val screenBackstack = mutableListOf<MangaUiScreen>(MangaUiScreen.Library)

    // --- State: Search and Filters ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow("الكل")
    val selectedGenre = _selectedGenre.asStateFlow()

    private val _selectedSource = MutableStateFlow("الكل")
    val selectedSource = _selectedSource.asStateFlow()

    // --- Reader Settings & Customization ---
    private val _isVerticalReading = MutableStateFlow(true)
    val isVerticalReading = _isVerticalReading.asStateFlow()

    private val _readingFontSize = MutableStateFlow(14f)
    val readingFontSize = _readingFontSize.asStateFlow()

    // --- Light / Dark Mode Toggle State ---
    private val _isDarkMode = MutableStateFlow(true) // Premium slate-dark mode by default
    val isDarkMode = _isDarkMode.asStateFlow()

    // --- Authentication & Profile updating ---
    private val _currentUser = MutableStateFlow<UserProfile?>(null) // null represents logged-out
    val currentUser = _currentUser.asStateFlow()

    // --- Organizing Manga in Custom Reading Lists ---
    private val _readingLists = MutableStateFlow<List<String>>(listOf("الكل", "المفضلة", "قائمة القراءة السريعة", "للقراءة لاحقاً", "مستمر"))
    val readingLists = _readingLists.asStateFlow()

    private val _selectedReadingList = MutableStateFlow("الكل")
    val selectedReadingList = _selectedReadingList.asStateFlow()

    private val _mangaToCategories = MutableStateFlow<Map<String, Set<String>>>(
        mapOf(
            "demon_slayer" to setOf("قائمة القراءة السريعة"),
            "solo_leveling" to setOf("مستمر"),
            "one_piece" to setOf("للقراءة لاحقاً")
        )
    )
    val mangaToCategories = _mangaToCategories.asStateFlow()

    // --- Keiyoushi Extension Manager ---
    private val _keiyoushiExtensions = MutableStateFlow<List<KeiyoushiExtension>>(listOf(
        KeiyoushiExtension("manga_lek", "مانجا ليك (MangaLek)", "مانجا ليك", "v2.4", isInstalled = true, isEnabled = true),
        KeiyoushiExtension("manga_dex", "مانجا ديكس (MangaDex)", "مانجا ديكس", "v3.1", isInstalled = true, isEnabled = true),
        KeiyoushiExtension("manga_slayer", "MangaSlayer Extension", "MangaSlayer", "v1.8", isInstalled = true, isEnabled = true),
        KeiyoushiExtension("arab_manga", "ArabManga Hub", "بوابة المانجا", "v1.2", isInstalled = false, isEnabled = false),
        KeiyoushiExtension("manga_town", "MangaTown Client", "MangaTown", "v4.0", isInstalled = false, isEnabled = false),
        KeiyoushiExtension("webtoon", "Official Webtoons API", "Webtoons", "v5.2", isInstalled = false, isEnabled = false)
    ))
    val keiyoushiExtensions = _keiyoushiExtensions.asStateFlow()

    // --- Comments & Discussion Forums ---
    private val _comments = MutableStateFlow<List<MangaComment>>(listOf(
        MangaComment("c1", "demon_slayer_ch_1", "demon_slayer", "محمد الديب", 1, "هذا الفصل أسطوري بكل معنى الكلمة ومؤثر جداً!", "قبل ساعة", 23, true),
        MangaComment("c2", "demon_slayer_ch_1", "demon_slayer", "سارة خالد", 2, "توميوكا شخصيتي المفضلة كالعادة.. طريقة رسم بؤرة عينه رهيبة!", "قبل ساعتين", 14),
        MangaComment("c3", "demon_slayer_ch_1", "demon_slayer", "عبدالرحمن أحمد", 3, "الرسم والتحريك بالأنمي مأخوذ مباشرة من فخامة صفحات ها المانجا", "قبل ٣ ساعات", 9),
        MangaComment("c4", "one_piece_ch_1", "one_piece", "أبو بكر القرصان", 4, "رومانس داون البداية الأعظم لأعظم رحلة في التاريخ ⚓☠️", "قبل ٥ ساعات", 45, true),
        MangaComment("c5", "one_piece_ch_1", "one_piece", "ياسر الحربي", 2, "شانكس رجل حقيقي بكل ما تعنيه الكلمة من مروءة وتضحية.", "قبل يوم", 18),
        MangaComment("c6", "solo_leveling_ch_1", "solo_leveling", "ميزو المانجا", 1, "نهوض سونغ جين وو هو الأفضل في تصنيف الويب تونز على الإطلاق! 🔥💪", "قبل ساعة", 56, true),
        MangaComment("c7", "solo_leveling_ch_1", "solo_leveling", "خلود علي", 3, "أحب تفاصيل العرش والوجوه الحجرية المرعبة جداً في هذا المعبد المزدوج.", "قبل يومين", 21)
    ))
    val comments = _comments.asStateFlow()

    // --- Custom Manga Ratings ---
    private val _customRatings = MutableStateFlow<Map<String, Float>>(emptyMap())
    val customRatings = _customRatings.asStateFlow()

    // --- Offline Chapters Downloads ---
    private val _downloadedChapters = MutableStateFlow<Set<String>>(emptySet())
    val downloadedChapters = _downloadedChapters.asStateFlow()

    private val _downloadingChapterInProgress = MutableStateFlow<Map<String, Float>>(emptyMap()) // chId -> progress (0.0f..1.0f)
    val downloadingChapterInProgress = _downloadingChapterInProgress.asStateFlow()

    // --- Stream: Favorite Library mangas (المكتبة) ---
    val libraryMangas: StateFlow<List<MangaEntity>> = combine(
        repository.libraryMangas,
        _searchQuery,
        _selectedReadingList,
        _mangaToCategories
    ) { mangas, query, listFilter, categoriesMap ->
        val filteredByQuery = if (query.isEmpty()) mangas
        else mangas.filter { it.titleAr.contains(query, ignoreCase = true) || it.titleEn.contains(query, ignoreCase = true) }

        when (listFilter) {
            "الكل" -> filteredByQuery
            "المفضلة" -> filteredByQuery.filter { it.isBookmarked }
            else -> filteredByQuery.filter { manga ->
                val categories = categoriesMap[manga.id] ?: emptySet()
                categories.contains(listFilter)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Stream: Browse Catalog Sources (تصفح / المصادر) ---
    val catalogMangas: StateFlow<List<MangaEntity>> = combine(
        repository.allMangas,
        _searchQuery,
        _selectedGenre,
        _selectedSource,
        _keiyoushiExtensions
    ) { all, query, genre, source, extensions ->
        all.filter { manga ->
            // Check matches on search criteria
            val matchesQuery = manga.titleAr.contains(query, ignoreCase = true) ||
                    manga.titleEn.contains(query, ignoreCase = true) ||
                    manga.author.contains(query, ignoreCase = true)
            
            val matchesGenre = genre == "الكل" || manga.genres.contains(genre, ignoreCase = true)
            val matchesSource = source == "الكل" || manga.sourceName.contains(source, ignoreCase = true)

            // Dynamic filter based on which Keiyoushi Extensions are currently installed AND enabled
            val isExtensionActive = extensions.any { ext ->
                ext.isInstalled && ext.isEnabled && (
                    manga.sourceName.contains(ext.sourceName, ignoreCase = true) ||
                    // Allow broad mapping for custom sources
                    (ext.id == "arab_manga" && manga.sourceName.contains("بوابة", ignoreCase = true))
                )
            }

            matchesQuery && matchesGenre && matchesSource && isExtensionActive
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Stream: Reading History (السجل) ---
    val readingHistory: StateFlow<List<HistoryEntity>> = repository.readingHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Detail Page Selection State ---
    private val _selectedMangaId = MutableStateFlow<String?>(null)
    val selectedManga: StateFlow<MangaEntity?> = _selectedMangaId
        .flatMapLatest { id ->
            if (id != null) repository.getMangaFlow(id)
            else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedMangaChapters: StateFlow<List<ChapterEntity>> = _selectedMangaId
        .flatMapLatest { id ->
            if (id != null) repository.getChaptersFlow(id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Reader Chapter Selection State ---
    private val _activeChapterId = MutableStateFlow<String?>(null)
    val activeChapter: StateFlow<ChapterEntity?> = combine(
        _selectedMangaId,
        _activeChapterId
    ) { mangaId, chId ->
        if (mangaId != null && chId != null) {
            repository.getChapter(mangaId, chId)
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Actions ---
    fun navigateTo(screen: MangaUiScreen) {
        if (_currentScreen.value != screen) {
            screenBackstack.add(screen)
            _currentScreen.value = screen
            
            // Sync selected states for detail or reader screens
            when (screen) {
                is MangaUiScreen.Detail -> {
                    _selectedMangaId.value = screen.mangaId
                }
                is MangaUiScreen.Reader -> {
                    _selectedMangaId.value = screen.mangaId
                    _activeChapterId.value = screen.chapterId
                }
                else -> {
                    // Reset details on grid return
                    _searchQuery.value = ""
                }
            }
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackstack.size > 1) {
            screenBackstack.removeAt(screenBackstack.lastIndex)
            val prev = screenBackstack.last()
            _currentScreen.value = prev

            // Align selected state flags matches
            when (prev) {
                is MangaUiScreen.Detail -> {
                    _selectedMangaId.value = prev.mangaId
                }
                is MangaUiScreen.Reader -> {
                    _selectedMangaId.value = prev.mangaId
                    _activeChapterId.value = prev.chapterId
                }
                else -> {
                    _selectedMangaId.value = null
                    _activeChapterId.value = null
                }
            }
            return true
        }
        return false // Exits app or no back operation possible
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGenreFilter(genre: String) {
        _selectedGenre.value = genre
    }

    fun setSourceFilter(source: String) {
        _selectedSource.value = source
    }

    fun toggleReadingDirection() {
        _isVerticalReading.value = !_isVerticalReading.value
    }

    fun toggleLibraryBookmark(manga: MangaEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(manga.id, manga.isBookmarked)
        }
    }

    fun recordProgress(mangaId: String, chapterId: String, page: Int, maxPages: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateChapterProgress(
                mangaId = mangaId,
                chapterId = chapterId,
                lastPage = page,
                totalPages = maxPages,
                isRead = isCompleted
            )
        }
    }

    fun removeHistoryForManga(mangaId: String) {
        viewModelScope.launch {
            repository.deleteHistory(mangaId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- New Action Methods for advanced specifications ---

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun loginUser(username: String, email: String) {
        _currentUser.value = UserProfile(username = username, email = email)
    }

    fun registerUser(username: String, email: String) {
        _currentUser.value = UserProfile(username = username, email = email)
    }

    fun logoutUser() {
        _currentUser.value = null
    }

    fun updateProfile(username: String, email: String, avatarRes: String) {
        _currentUser.value = _currentUser.value?.copy(username = username, email = email, avatarRes = avatarRes)
    }

    fun createReadingList(name: String) {
        val clean = name.trim()
        if (clean.isNotEmpty() && !_readingLists.value.contains(clean)) {
            _readingLists.value = _readingLists.value + clean
        }
    }

    fun deleteReadingList(name: String) {
        if (name != "الكل" && name != "المفضلة") {
            _readingLists.value = _readingLists.value - name
            // Clean up associations
            _mangaToCategories.value = _mangaToCategories.value.mapValues { (_, set) ->
                set - name
            }
            if (_selectedReadingList.value == name) {
                _selectedReadingList.value = "الكل"
            }
        }
    }

    fun setSelectedReadingList(list: String) {
        _selectedReadingList.value = list
    }

    fun isMangaInReadingList(mangaId: String, list: String): Boolean {
        return _mangaToCategories.value[mangaId]?.contains(list) == true
    }

    fun toggleMangaInCategory(mangaId: String, category: String) {
        val currentCategories = _mangaToCategories.value[mangaId] ?: emptySet()
        val updatedCategories = if (currentCategories.contains(category)) {
            currentCategories - category
        } else {
            currentCategories + category
        }
        _mangaToCategories.value = _mangaToCategories.value + (mangaId to updatedCategories)
    }

    fun installExtension(extId: String) {
        _keiyoushiExtensions.value = _keiyoushiExtensions.value.map {
            if (it.id == extId) it.copy(isInstalled = true, isEnabled = true) else it
        }
    }

    fun uninstallExtension(extId: String) {
        _keiyoushiExtensions.value = _keiyoushiExtensions.value.map {
            if (it.id == extId) it.copy(isInstalled = false, isEnabled = false) else it
        }
    }

    fun toggleExtension(extId: String) {
        _keiyoushiExtensions.value = _keiyoushiExtensions.value.map {
            if (it.id == extId) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun rateManga(mangaId: String, rating: Float) {
        _customRatings.value = _customRatings.value + (mangaId to rating)
    }

    fun setReadingFontSize(size: Float) {
        _readingFontSize.value = size
    }

    fun downloadChapter(chapterId: String) {
        if (_downloadedChapters.value.contains(chapterId)) {
            // Un-download
            _downloadedChapters.value = _downloadedChapters.value - chapterId
        } else {
            // Trigger simulated download progress bar
            viewModelScope.launch {
                for (step in 1..5) {
                    _downloadingChapterInProgress.value = _downloadingChapterInProgress.value + (chapterId to (step * 0.2f))
                    kotlinx.coroutines.delay(180)
                }
                _downloadingChapterInProgress.value = _downloadingChapterInProgress.value - chapterId
                _downloadedChapters.value = _downloadedChapters.value + chapterId
            }
        }
    }

    fun addComment(mangaId: String, chapterId: String?, author: String, content: String) {
        if (content.trim().isEmpty()) return
        val newC = MangaComment(
            id = "c_user_${System.currentTimeMillis()}",
            chapterId = chapterId,
            mangaId = mangaId,
            author = author,
            avatarIndex = (1..6).random(),
            content = content,
            timestamp = "الآن",
            likes = 0,
            isLiked = false
        )
        _comments.value = listOf(newC) + _comments.value
    }

    fun toggleLikeComment(commentId: String) {
        _comments.value = _comments.value.map {
            if (it.id == commentId) {
                it.copy(
                    likes = if (it.isLiked) it.likes - 1 else it.likes + 1,
                    isLiked = !it.isLiked
                )
            } else it
        }
    }
}

class MangaViewModelFactory(private val repository: MangaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MangaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MangaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
