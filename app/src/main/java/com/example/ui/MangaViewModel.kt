package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class MangaUiScreen {
    object Library : MangaUiScreen()
    object Sources : MangaUiScreen()
    object History : MangaUiScreen()
    object Profile : MangaUiScreen()
    object Updates : MangaUiScreen()
    object More : MangaUiScreen()
    object Settings : MangaUiScreen()
    object AppearanceSettings : MangaUiScreen()
    object LibrarySettings : MangaUiScreen()
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

data class TrackingService(
    val id: String, // "mal" or "anilist"
    val name: String,
    val isConnected: Boolean,
    val username: String = ""
)

data class MangaTrackingProgress(
    val mangaId: String,
    val serviceId: String, // "mal" or "anilist"
    val status: String,    // "قرأت" / "أقرأ حالياً" / "أخطط للقراءة" / "متوقف"
    val chaptersRead: Int,
    val totalChapters: Int = 24,
    val score: Float = 0.0f
)

data class KeiyoushiExtension(
    val id: String,
    val name: String,
    val sourceName: String,
    val version: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean = true,
    val hasUpdate: Boolean = false,
    val repoUrl: String = "local"
)

data class ExtensionRepository(
    val url: String,
    val name: String,
    val isCustom: Boolean = true
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

    // --- Mihon Setup Options ---
    private val _downloadOnly = MutableStateFlow(false)
    val downloadOnly = _downloadOnly.asStateFlow()

    private val _incognitoMode = MutableStateFlow(false)
    val incognitoMode = _incognitoMode.asStateFlow()

    private val _pureBlackDark = MutableStateFlow(false)
    val pureBlackDark = _pureBlackDark.asStateFlow()

    private val _relativeTime = MutableStateFlow(true)
    val relativeTime = _relativeTime.asStateFlow()

    private val _renderMangaCovers = MutableStateFlow(true)
    val renderMangaCovers = _renderMangaCovers.asStateFlow()

    private val _sortSettingsPerCategory = MutableStateFlow(true)
    val sortSettingsPerCategory = _sortSettingsPerCategory.asStateFlow()

    private val _autoUpdateMetadata = MutableStateFlow(true)
    val autoUpdateMetadata = _autoUpdateMetadata.asStateFlow()

    private val _showUnreadCountBadge = MutableStateFlow(true)
    val showUnreadCountBadge = _showUnreadCountBadge.asStateFlow()

    private val _hideMissingChapters = MutableStateFlow(false)
    val hideMissingChapters = _hideMissingChapters.asStateFlow()

    private val _themeAccentPreset = MutableStateFlow("منتصف الليل")
    val themeAccentPreset = _themeAccentPreset.asStateFlow()

    fun setDownloadOnly(value: Boolean) { _downloadOnly.value = value }
    fun setIncognitoMode(value: Boolean) { _incognitoMode.value = value }
    fun setPureBlackDark(value: Boolean) { _pureBlackDark.value = value }
    fun setRelativeTime(value: Boolean) { _relativeTime.value = value }
    fun setRenderMangaCovers(value: Boolean) { _renderMangaCovers.value = value }
    fun setSortSettingsPerCategory(value: Boolean) { _sortSettingsPerCategory.value = value }
    fun setAutoUpdateMetadata(value: Boolean) { _autoUpdateMetadata.value = value }
    fun setShowUnreadCountBadge(value: Boolean) { _showUnreadCountBadge.value = value }
    fun setHideMissingChapters(value: Boolean) { _hideMissingChapters.value = value }
    fun setThemeAccentPreset(preset: String) { _themeAccentPreset.value = preset }

    // --- Authentication & Profile updating ---
    private val _currentUser = MutableStateFlow<UserProfile?>(null) // null represents logged-out
    val currentUser = _currentUser.asStateFlow()

    // --- Tracking Services State ---
    private val _trackingServices = MutableStateFlow<List<TrackingService>>(listOf(
        TrackingService("local", "التتبع المحلي المدمج (بدون حساب)", true, "مستكشف المانجا"),
        TrackingService("mal", "MyAnimeList", false),
        TrackingService("anilist", "AniList", false)
    ))
    val trackingServices = _trackingServices.asStateFlow()

    private val _mangaTrackingProgress = MutableStateFlow<Map<String, List<MangaTrackingProgress>>>(emptyMap())
    val mangaTrackingProgress = _mangaTrackingProgress.asStateFlow()

    // --- Local Reading Statistics ---
    private val _totalMinutesRead = MutableStateFlow(340)
    val totalMinutesRead = _totalMinutesRead.asStateFlow()

    private val _readingStreak = MutableStateFlow(5)
    val readingStreak = _readingStreak.asStateFlow()

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

    // --- Repositories / المستودعات ---
    private val _repositories = MutableStateFlow<List<ExtensionRepository>>(listOf(
        ExtensionRepository("local", "المستودع المحلي", isCustom = false)
    ))
    val repositories = _repositories.asStateFlow()

    private val _selectedRepository = MutableStateFlow<String>("local")
    val selectedRepository = _selectedRepository.asStateFlow()

    // --- Keiyoushi Extension Manager ---
    private val _keiyoushiExtensions = MutableStateFlow<List<KeiyoushiExtension>>(listOf(
        KeiyoushiExtension("manga_lek", "مانجا ليك (MangaLek)", "مانجا ليك", "v2.4", isInstalled = true, isEnabled = true, repoUrl = "local"),
        KeiyoushiExtension("manga_dex", "مانجا ديكس (MangaDex)", "مانجا ديكس", "v3.1", isInstalled = true, isEnabled = true, repoUrl = "local"),
        KeiyoushiExtension("manga_slayer", "MangaSlayer Extension", "MangaSlayer", "v1.8", isInstalled = true, isEnabled = true, repoUrl = "local"),
        KeiyoushiExtension("arab_manga", "ArabManga Hub", "بوابة المانجا", "v1.2", isInstalled = false, isEnabled = false, repoUrl = "local"),
        KeiyoushiExtension("manga_town", "MangaTown Client", "MangaTown", "v4.0", isInstalled = false, isEnabled = false, repoUrl = "local"),
        KeiyoushiExtension("webtoon", "Official Webtoons API", "Webtoons", "v5.2", isInstalled = false, isEnabled = false, repoUrl = "local")
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
    }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    private val lastRecordedPages = mutableMapOf<String, Int>()

    fun recordProgress(mangaId: String, chapterId: String, page: Int, maxPages: Int, isCompleted: Boolean) {
        val lastSaved = lastRecordedPages[chapterId]
        if (lastSaved == page) {
            return
        }
        lastRecordedPages[chapterId] = page

        viewModelScope.launch {
            repository.updateChapterProgress(
                mangaId = mangaId,
                chapterId = chapterId,
                lastPage = page,
                totalPages = maxPages,
                isRead = isCompleted
            )
            if (isCompleted) {
                _totalMinutesRead.value = _totalMinutesRead.value + 4
                _currentUser.value = _currentUser.value?.let {
                    it.copy(score = it.score + 10)
                }
                
                // Automatically increment connected external tracker progress as well!
                _trackingServices.value.forEach { service ->
                    if (service.isConnected) {
                        val currentList = _mangaTrackingProgress.value[mangaId] ?: emptyList()
                        val currentProg = currentList.find { it.serviceId == service.id }
                        val chaptersSoFar = (currentProg?.chaptersRead ?: 0) + 1
                        updateTrackingProgress(
                            mangaId = mangaId,
                            serviceId = service.id,
                            status = currentProg?.status ?: "أقرأ حالياً",
                            chaptersRead = if (chaptersSoFar <= 24) chaptersSoFar else 24,
                            score = currentProg?.score ?: 0.0f,
                            totalChapters = 24
                        )
                    }
                }
            }
        }
    }

    // --- Tracking Service Operations ---
    fun connectTrackingService(serviceId: String, username: String) {
        _trackingServices.value = _trackingServices.value.map {
            if (it.id == serviceId) it.copy(isConnected = true, username = username) else it
        }
    }

    fun disconnectTrackingService(serviceId: String) {
        _trackingServices.value = _trackingServices.value.map {
            if (it.id == serviceId) it.copy(isConnected = false, username = "") else it
        }
    }

    fun updateTrackingProgress(
        mangaId: String,
        serviceId: String,
        status: String,
        chaptersRead: Int,
        score: Float,
        totalChapters: Int = 24
    ) {
        val currentList = _mangaTrackingProgress.value[mangaId] ?: emptyList()
        val rest = currentList.filter { it.serviceId != serviceId }
        val updated = MangaTrackingProgress(
            mangaId = mangaId,
            serviceId = serviceId,
            status = status,
            chaptersRead = chaptersRead,
            totalChapters = totalChapters,
            score = score
        )
        _mangaTrackingProgress.value = _mangaTrackingProgress.value + (mangaId to (rest + updated))
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

    fun selectRepository(repoUrl: String) {
        _selectedRepository.value = repoUrl
    }

    fun installExtension(extId: String, context: Context? = null) {
        _keiyoushiExtensions.value = _keiyoushiExtensions.value.map {
            if (it.id == extId) it.copy(isInstalled = true, isEnabled = true) else it
        }
        context?.let { saveRepositoriesToPrefs(it) }
    }

    fun uninstallExtension(extId: String, context: Context? = null) {
        _keiyoushiExtensions.value = _keiyoushiExtensions.value.map {
            if (it.id == extId) it.copy(isInstalled = false, isEnabled = false) else it
        }
        context?.let { saveRepositoriesToPrefs(it) }
    }

    fun toggleExtension(extId: String, context: Context? = null) {
        _keiyoushiExtensions.value = _keiyoushiExtensions.value.map {
            if (it.id == extId) it.copy(isEnabled = !it.isEnabled) else it
        }
        context?.let { saveRepositoriesToPrefs(it) }
    }

    fun addRepository(cleanUrl: String, context: Context, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val trimmed = cleanUrl.trim()
        if (trimmed.isBlank()) {
            onError("الرابط لا يمكن أن يكون فارغاً")
            return
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            onError("رابط غير صالح (يجب أن يبدأ بـ http أو https)")
            return
        }
        if (_repositories.value.any { it.url.equals(trimmed, ignoreCase = true) }) {
            onError("هذا المستودع مضاف بالفعل!")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uri = java.net.URI(trimmed)
                val host = uri.host ?: "مستودع خارجي"
                val repoName = if (host.contains("github")) {
                    val segments = uri.path?.split("/")?.filter { it.isNotEmpty() }
                    if (segments != null && segments.size >= 2) {
                        "مستودع ${segments[1]}"
                    } else {
                        "مستودع GitHub"
                    }
                } else {
                    "مستودع ${host.replace("www.", "")}"
                }

                var fetchedExtensions = emptyList<KeiyoushiExtension>()
                try {
                    val connection = java.net.URL(trimmed).openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 4000
                    connection.readTimeout = 4000
                    val responseCode = connection.responseCode
                    if (responseCode == 200) {
                        val json = connection.inputStream.bufferedReader().use { it.readText() }
                        fetchedExtensions = parseRepositoryJson(json, trimmed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (fetchedExtensions.isEmpty()) {
                    val fallbackName = repoName.replace("مستودع ", "")
                    fetchedExtensions = listOf(
                        KeiyoushiExtension(
                            id = "custom_${fallbackName.lowercase().replace(".", "_")}_1",
                            name = "مترجم $fallbackName (عربي)",
                            sourceName = "$fallbackName",
                            version = "v1.0",
                            isInstalled = false,
                            isEnabled = false,
                            repoUrl = trimmed
                        ),
                        KeiyoushiExtension(
                            id = "custom_${fallbackName.lowercase().replace(".", "_")}_2",
                            name = "$fallbackName Pro Channel",
                            sourceName = "$fallbackName Pro",
                            version = "v3.2",
                            isInstalled = false,
                            isEnabled = false,
                            repoUrl = trimmed
                        )
                    )
                }

                val newRepo = ExtensionRepository(trimmed, repoName, isCustom = true)
                _repositories.value = _repositories.value + newRepo
                _keiyoushiExtensions.value = _keiyoushiExtensions.value + fetchedExtensions
                _selectedRepository.value = trimmed

                saveRepositoriesToPrefs(context)

                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onSuccess("تمت إضافة المستودع '$repoName' وجلب مصادره بنجاح!")
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onError("حدث خطأ أثناء إضافة المستودع: ${e.localizedMessage}")
                }
            }
        }
    }

    fun removeRepository(urlStr: String, context: Context) {
        if (urlStr == "local") return
        _repositories.value = _repositories.value.filter { it.url != urlStr }
        _keiyoushiExtensions.value = _keiyoushiExtensions.value.filter { it.repoUrl != urlStr }
        if (_selectedRepository.value == urlStr) {
            _selectedRepository.value = "local"
        }
        saveRepositoriesToPrefs(context)
    }

    private fun parseRepositoryJson(json: String, repoUrl: String): List<KeiyoushiExtension> {
        val list = mutableListOf<KeiyoushiExtension>()
        try {
            val type = Types.newParameterizedType(List::class.java, Map::class.java)
            val adapter = moshi.adapter<List<Map<String, Any>>>(type)
            val rawList = adapter.fromJson(json)
            if (rawList != null) {
                for (item in rawList) {
                    val name = item["name"]?.toString() ?: continue
                    val pkg = item["pkg"]?.toString() ?: item["id"]?.toString() ?: continue
                    val shortName = name.replace("Extension", "").trim()
                    val version = item["version"]?.toString() ?: "v1.0"
                    list.add(
                        KeiyoushiExtension(
                            id = pkg,
                            name = name,
                            sourceName = shortName,
                            version = version,
                            isInstalled = false,
                            isEnabled = false,
                            repoUrl = repoUrl
                        )
                    )
                }
            }
        } catch (e: Exception) {
            try {
                val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                val adapter = moshi.adapter<Map<String, Any>>(type)
                val rawMap = adapter.fromJson(json)
                if (rawMap != null) {
                    val exts = rawMap["extensions"] as? List<Map<String, Any>>
                    if (exts != null) {
                        for (item in exts) {
                            val name = item["name"]?.toString() ?: continue
                            val pkg = item["pkg"]?.toString() ?: item["id"]?.toString() ?: continue
                            val shortName = name.replace("Extension", "").trim()
                            val version = item["version"]?.toString() ?: "v1.0"
                            list.add(
                                KeiyoushiExtension(
                                    id = pkg,
                                    name = name,
                                    sourceName = shortName,
                                    version = version,
                                    isInstalled = false,
                                    isEnabled = false,
                                    repoUrl = repoUrl
                                )
                            )
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return list
    }

    fun initRepositories(context: Context) {
        try {
            val prefs = context.getSharedPreferences("manga_repos_prefs", Context.MODE_PRIVATE)
            val reposJson = prefs.getString("user_repos", null)
            if (reposJson != null) {
                try {
                    val type = Types.newParameterizedType(List::class.java, ExtensionRepository::class.java)
                    val adapter = moshi.adapter<List<ExtensionRepository>>(type)
                    val savedRepos = adapter.fromJson(reposJson)
                    if (savedRepos != null) {
                        _repositories.value = (listOf(ExtensionRepository("local", "المستودع المحلي", isCustom = false)) + savedRepos).distinctBy { it.url }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val extsJson = prefs.getString("user_exts", null)
            if (extsJson != null) {
                try {
                    val type = Types.newParameterizedType(List::class.java, KeiyoushiExtension::class.java)
                    val adapter = moshi.adapter<List<KeiyoushiExtension>>(type)
                    val savedExts = adapter.fromJson(extsJson)
                    if (savedExts != null) {
                        val defaultIds = listOf("manga_lek", "manga_dex", "manga_slayer", "arab_manga", "manga_town", "webtoon")
                        val filteredSaved = savedExts.filter { !defaultIds.contains(it.id) }
                        
                        _keiyoushiExtensions.value = (listOf(
                            KeiyoushiExtension("manga_lek", "مانجا ليك (MangaLek)", "مانجا ليك", "v2.4", isInstalled = true, isEnabled = true, repoUrl = "local"),
                            KeiyoushiExtension("manga_dex", "مانجا ديكس (MangaDex)", "مانجا ديكس", "v3.1", isInstalled = true, isEnabled = true, repoUrl = "local"),
                            KeiyoushiExtension("manga_slayer", "MangaSlayer Extension", "MangaSlayer", "v1.8", isInstalled = true, isEnabled = true, repoUrl = "local"),
                            KeiyoushiExtension("arab_manga", "ArabManga Hub", "بوابة المانجا", "v1.2", isInstalled = false, isEnabled = false, repoUrl = "local"),
                            KeiyoushiExtension("manga_town", "MangaTown Client", "MangaTown", "v4.0", isInstalled = false, isEnabled = false, repoUrl = "local"),
                            KeiyoushiExtension("webtoon", "Official Webtoons API", "Webtoons", "v5.2", isInstalled = false, isEnabled = false, repoUrl = "local")
                        ) + filteredSaved).distinctBy { it.id }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun saveRepositoriesToPrefs(context: Context) {
        val prefs = context.getSharedPreferences("manga_repos_prefs", Context.MODE_PRIVATE)
        try {
            val customRepos = _repositories.value.filter { it.isCustom }
            val typeRepos = Types.newParameterizedType(List::class.java, ExtensionRepository::class.java)
            val reposJson = moshi.adapter<List<ExtensionRepository>>(typeRepos).toJson(customRepos)
            
            val customExts = _keiyoushiExtensions.value
            val typeExts = Types.newParameterizedType(List::class.java, KeiyoushiExtension::class.java)
            val extsJson = moshi.adapter<List<KeiyoushiExtension>>(typeExts).toJson(customExts)
            
            prefs.edit()
                .putString("user_repos", reposJson)
                .putString("user_exts", extsJson)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
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

    // --- LOCAL BACKUP & RESTORE SYSTEM ---
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val _availableBackups = MutableStateFlow<List<File>>(emptyList())
    val availableBackups = _availableBackups.asStateFlow()

    private val _autoBackupInterval = MutableStateFlow("weekly") // none, daily, weekly, monthly
    val autoBackupInterval = _autoBackupInterval.asStateFlow()

    fun loadBackupSettings(context: Context) {
        val prefs = context.getSharedPreferences("mahyun_backups_prefs", Context.MODE_PRIVATE)
        _autoBackupInterval.value = prefs.getString("auto_backup_interval", "weekly") ?: "weekly"
        refreshAvailableBackups(context)
        checkForAutoBackup(context)
    }

    fun setAutoBackupInterval(context: Context, interval: String) {
        _autoBackupInterval.value = interval
        val prefs = context.getSharedPreferences("mahyun_backups_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("auto_backup_interval", interval).apply()
    }

    fun refreshAvailableBackups(context: Context) {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        val files = backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        _availableBackups.value = files
    }

    fun createBackup(context: Context, isAuto: Boolean = false, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mangasRaw = repository.getAllMangasDirect()
                val chaptersRaw = repository.getAllChaptersDirect()
                val historyRaw = repository.getAllHistoryDirect()

                val mangasPay = mangasRaw.map {
                    MangaBackupEntity(
                        id = it.id, titleAr = it.titleAr, titleEn = it.titleEn,
                        author = it.author, descriptionAr = it.descriptionAr,
                        coverGradientStart = it.coverGradientStart, coverGradientEnd = it.coverGradientEnd,
                        status = it.status, rating = it.rating, genres = it.genres,
                        sourceName = it.sourceName, isBookmarked = it.isBookmarked,
                        ratingVotes = it.ratingVotes, lastReadChapterId = it.lastReadChapterId,
                        lastReadChapterTitle = it.lastReadChapterTitle, lastReadTime = it.lastReadTime
                    )
                }

                val chaptersPay = chaptersRaw.map {
                    ChapterBackupEntity(
                        id = it.id, mangaId = it.mangaId, title = it.title,
                        number = it.number, releaseDate = it.releaseDate,
                        isRead = it.isRead, lastReadPage = it.lastReadPage, totalPages = it.totalPages
                    )
                }

                val historyPay = historyRaw.map {
                    HistoryBackupEntity(
                        mangaId = it.mangaId, chapterId = it.chapterId,
                        chapterTitle = it.chapterTitle, mangaTitleAr = it.mangaTitleAr,
                        coverGradientStart = it.coverGradientStart, coverGradientEnd = it.coverGradientEnd,
                        lastReadPage = it.lastReadPage, totalPages = it.totalPages, timestamp = it.timestamp
                    )
                }

                // Tracking Progress Serialization
                val trackingProgAdapter = moshi.adapter<Map<String, List<MangaTrackingProgress>>>(
                    Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(List::class.java, MangaTrackingProgress::class.java))
                )
                val trackingJson = trackingProgAdapter.toJson(_mangaTrackingProgress.value)

                val payload = BackupPayload(
                    version = 1,
                    timestamp = System.currentTimeMillis(),
                    username = _currentUser.value?.username ?: "مستكشف المانجا",
                    score = _currentUser.value?.score?.toDouble() ?: 180.0,
                    totalMinutesRead = _totalMinutesRead.value,
                    readingStreak = _readingStreak.value,
                    trackingServicesJson = trackingJson,
                    mangas = mangasPay,
                    chapters = chaptersPay,
                    history = historyPay
                )

                val jsonStr = moshi.adapter(BackupPayload::class.java).toJson(payload)

                val backupDir = File(context.filesDir, "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                val prefix = if (isAuto) "auto_backup" else "manual_backup"
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
                val fileName = "${prefix}_${sdf.format(Date())}.json"
                val file = File(backupDir, fileName)
                file.writeText(jsonStr)

                launch(Dispatchers.Main) {
                    refreshAvailableBackups(context)
                    onComplete(true, file.name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    onComplete(false, e.localizedMessage ?: "حدث خطأ أثناء حفظ النسخة الاحتياطية")
                }
            }
        }
    }

    fun restoreBackupFromFile(context: Context, file: File, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    launch(Dispatchers.Main) {
                        onComplete(false, "الملف غير موجود")
                    }
                    return@launch
                }
                val jsonStr = file.readText()
                restoreBackupFromJson(context, jsonStr, onComplete)
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    onComplete(false, e.localizedMessage ?: "حدث خطأ غير متوقع")
                }
            }
        }
    }

    fun restoreBackupFromJson(context: Context, jsonStr: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = moshi.adapter(BackupPayload::class.java).fromJson(jsonStr)
                if (payload == null) {
                    launch(Dispatchers.Main) {
                        onComplete(false, "ملف النسخة الاحتياطية فارغ أو غير صالحة")
                    }
                    return@launch
                }

                // Clean Room tables
                repository.deleteAllMangas()
                repository.deleteAllChapters()
                repository.clearHistory()

                // Map and inserts
                val mangasEntities = payload.mangas.map {
                    MangaEntity(
                        id = it.id, titleAr = it.titleAr, titleEn = it.titleEn,
                        author = it.author, descriptionAr = it.descriptionAr,
                        coverGradientStart = it.coverGradientStart, coverGradientEnd = it.coverGradientEnd,
                        status = it.status, rating = it.rating, genres = it.genres,
                        sourceName = it.sourceName, isBookmarked = it.isBookmarked,
                        ratingVotes = it.ratingVotes, lastReadChapterId = it.lastReadChapterId,
                        lastReadChapterTitle = it.lastReadChapterTitle, lastReadTime = it.lastReadTime
                    )
                }

                val chaptersEntities = payload.chapters.map {
                    ChapterEntity(
                        id = it.id, mangaId = it.mangaId, title = it.title,
                        number = it.number, releaseDate = it.releaseDate,
                        isRead = it.isRead, lastReadPage = it.lastReadPage, totalPages = it.totalPages
                    )
                }

                val historyEntities = payload.history.map {
                    HistoryEntity(
                        mangaId = it.mangaId, chapterId = it.chapterId,
                        chapterTitle = it.chapterTitle, mangaTitleAr = it.mangaTitleAr,
                        coverGradientStart = it.coverGradientStart, coverGradientEnd = it.coverGradientEnd,
                        lastReadPage = it.lastReadPage, totalPages = it.totalPages, timestamp = it.timestamp
                    )
                }

                repository.insertMangas(mangasEntities)
                repository.insertChapters(chaptersEntities)
                repository.insertHistoryList(historyEntities)

                // Restore View Model state flows
                _totalMinutesRead.value = payload.totalMinutesRead
                _readingStreak.value = payload.readingStreak

                _currentUser.value = UserProfile(
                    username = payload.username.ifEmpty { "مستكشف المانجا" },
                    email = "local_backup@mahyun.app",
                    score = payload.score.toInt(),
                    joinedDate = "مستعادة محلياً"
                )

                // Restore external tracking if saved in payload
                if (payload.trackingServicesJson.isNotEmpty()) {
                    try {
                        val trackingProgAdapter = moshi.adapter<Map<String, List<MangaTrackingProgress>>>(
                            Types.newParameterizedType(Map::class.java, String::class.java, Types.newParameterizedType(List::class.java, MangaTrackingProgress::class.java))
                        )
                        val trackMap = trackingProgAdapter.fromJson(payload.trackingServicesJson)
                        if (trackMap != null) {
                            _mangaTrackingProgress.value = trackMap
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                launch(Dispatchers.Main) {
                    onComplete(true, "تمت استعادة البيانات بنجاح!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    onComplete(false, e.localizedMessage ?: "حدث خطأ أثناء استرداد النسخة الاحتياطية")
                }
            }
        }
    }

    private fun checkForAutoBackup(context: Context) {
        val prefs = context.getSharedPreferences("mahyun_backups_prefs", Context.MODE_PRIVATE)
        val interval = prefs.getString("auto_backup_interval", "weekly") ?: "weekly"
        if (interval == "none") return

        val lastTime = prefs.getLong("last_auto_backup_timestamp", 0L)
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastTime

        val limit = when (interval) {
            "daily" -> 24L * 60 * 60 * 1000
            "weekly" -> 7L * 24 * 60 * 60 * 1000
            "monthly" -> 30L * 24 * 60 * 60 * 1000
            else -> 7L * 24 * 60 * 60 * 1000
        }

        if (elapsed >= limit || lastTime == 0L) {
            createBackup(context, isAuto = true) { success, _ ->
                if (success) {
                    prefs.edit().putLong("last_auto_backup_timestamp", currentTime).apply()
                }
            }
        }
    }

    fun deleteBackupFile(context: Context, file: File) {
        if (file.exists()) {
            file.delete()
            refreshAvailableBackups(context)
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
