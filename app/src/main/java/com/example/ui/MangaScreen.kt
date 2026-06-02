package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.io.File
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ChapterEntity
import com.example.data.HistoryEntity
import com.example.data.MangaEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadBackupSettings(context)
        viewModel.initRepositories(context)
    }

    // Set colors according to dynamic dark mode toggle
    val backgroundColor = if (isDarkMode) SlateDark else SlateLightBg

    BackHandler(enabled = true) {
        val wentBack = viewModel.navigateBack()
        if (!wentBack) {
            (context as? android.app.Activity)?.finish()
        }
    }

    Scaffold(
        modifier = modifier.testTag("manga_screen_root"),
        containerColor = backgroundColor,
        bottomBar = {
            AnimatedVisibility(
                visible = currentScreen is MangaUiScreen.Library || 
                          currentScreen is MangaUiScreen.Sources || 
                          currentScreen is MangaUiScreen.History ||
                          currentScreen is MangaUiScreen.Profile ||
                          currentScreen is MangaUiScreen.Updates ||
                          currentScreen is MangaUiScreen.More,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                MangaBottomNav(
                    currentScreen = currentScreen,
                    isDarkMode = isDarkMode,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is MangaUiScreen.Library -> {
                        MangaLibraryView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Sources -> {
                        MangaCatalogView(viewModel = viewModel)
                    }
                    is MangaUiScreen.History -> {
                        MangaHistoryView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Profile -> {
                        MangaProfileView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Updates -> {
                        MangaUpdatesView(viewModel = viewModel)
                    }
                    is MangaUiScreen.More -> {
                        MangaMoreView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Settings -> {
                        MangaSettingsView(viewModel = viewModel)
                    }
                    is MangaUiScreen.AppearanceSettings -> {
                        MangaAppearanceSettingsView(viewModel = viewModel)
                    }
                    is MangaUiScreen.LibrarySettings -> {
                        MangaLibrarySettingsView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Downloads -> {
                        MangaDownloadsQueueView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Categories -> {
                        MangaCategoriesView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Stats -> {
                        MangaStatsView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Backup -> {
                        MangaBackupView(viewModel = viewModel)
                    }
                    is MangaUiScreen.Detail -> {
                        MangaDetailView(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is MangaUiScreen.Reader -> {
                        MangaReaderView(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}

// --- Custom Bottom Navigation Bar with RTL-ordered Tabs ---
@Composable
fun MangaBottomNav(
    currentScreen: MangaUiScreen,
    isDarkMode: Boolean,
    onNavigate: (MangaUiScreen) -> Unit
) {
    val navBarBackground = if (isDarkMode) SlateDark else SlateLightCard
    val dividerColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
    val textSecondaryColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight

    NavigationBar(
        containerColor = navBarBackground,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = dividerColor,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        // order left-to-right (which renders right-to-left under standard RTL locale, but we enforce it beautifully)
        // More, Sources, History, Updates, Library

        // 1. المزيد (More)
        val isMoreSelected = currentScreen is MangaUiScreen.More || currentScreen is MangaUiScreen.Settings || currentScreen is MangaUiScreen.AppearanceSettings || currentScreen is MangaUiScreen.LibrarySettings
        NavigationBarItem(
            selected = isMoreSelected,
            onClick = { onNavigate(MangaUiScreen.More) },
            icon = {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "المزيد",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "المزيد",
                    fontSize = 11.sp,
                    fontWeight = if (isMoreSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorSchemePrimaryOrWhite(isMoreSelected),
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )

        // 2. تصفح (Browse)
        val isSourcesSelected = currentScreen is MangaUiScreen.Sources
        NavigationBarItem(
            selected = isSourcesSelected,
            onClick = { onNavigate(MangaUiScreen.Sources) },
            icon = {
                BadgedBox(
                    badge = {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("2", color = Color.White, fontSize = 9.sp)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "تصفح",
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "تصفح",
                    fontSize = 11.sp,
                    fontWeight = if (isSourcesSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorSchemePrimaryOrWhite(isSourcesSelected),
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )

        // 3. السجل (History)
        val isHistorySelected = currentScreen is MangaUiScreen.History
        NavigationBarItem(
            selected = isHistorySelected,
            onClick = { onNavigate(MangaUiScreen.History) },
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "السجل",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "السجل",
                    fontSize = 11.sp,
                    fontWeight = if (isHistorySelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorSchemePrimaryOrWhite(isHistorySelected),
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )

        // 4. التحديثات (Updates)
        val isUpdatesSelected = currentScreen is MangaUiScreen.Updates
        NavigationBarItem(
            selected = isUpdatesSelected,
            onClick = { onNavigate(MangaUiScreen.Updates) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "التحديثات",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "التحديثات",
                    fontSize = 11.sp,
                    fontWeight = if (isUpdatesSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorSchemePrimaryOrWhite(isUpdatesSelected),
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )

        // 5. المكتبة (Library)
        val isLibSelected = currentScreen is MangaUiScreen.Library
        NavigationBarItem(
            selected = isLibSelected,
            onClick = { onNavigate(MangaUiScreen.Library) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CollectionsBookmark,
                    contentDescription = "المكتبة",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "المكتبة",
                    fontSize = 11.sp,
                    fontWeight = if (isLibSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorSchemePrimaryOrWhite(isLibSelected),
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )
    }
}

@Composable
fun colorSchemePrimaryOrWhite(selected: Boolean): Color {
    return if (selected) Color.White else MaterialTheme.colorScheme.primary
}

// --- 1. LIBRARY VIEW with Custom Playlists ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaLibraryView(viewModel: MangaViewModel) {
    val libraryMangas by viewModel.libraryMangas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val readingLists by viewModel.readingLists.collectAsState()
    val selectedList by viewModel.selectedReadingList.collectAsState()

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val cardBackground = if (isDarkMode) SlateCard else SlateLightCard
    val inputBackground = if (isDarkMode) SlateCard else Color(0xFFF1F5F9)

    var showManageListsDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    // Dialog context for long-click list management
    var showCategoryAssignDialog by remember { mutableStateOf<MangaEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Upper Library row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مكتبتي",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "المانجا المفضلة والمخصصة لقوائمك",
                    fontSize = 12.sp,
                    color = textSecColor
                )
            }

            // Stat badge with category manager trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { showManageListsDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إنشاء قائمة",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { showManageListsDialog = true }
                ) {
                    Text(
                        text = "إدارة القوائم ⚙",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search in library text-field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("بحث في المكتبة...", color = textSecColor) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("library_search_field"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f),
                focusedContainerColor = inputBackground,
                unfocusedContainerColor = inputBackground,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = textSecColor
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = textSecColor)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Lists filtering row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "قائمتي: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textSecColor,
                modifier = Modifier.padding(end = 4.dp)
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                ScrollableRow(
                    items = readingLists,
                    selectedItem = selectedList,
                    isDarkMode = isDarkMode
                ) {
                    viewModel.setSelectedReadingList(it)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (libraryMangas.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(cardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = textSecColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "لا توجد مانجا تطابق التصفية",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اجعل المانجا مفضلةً، أو انقر مطولاً عليها لتصنيفها ضمن إحدى قوائم القراءة الخاصة بك!",
                    fontSize = 12.sp,
                    color = textSecColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(libraryMangas, key = { it.id }) { manga ->
                    MangaGridCard(
                        manga = manga,
                        isDarkMode = isDarkMode,
                        onClick = { viewModel.navigateTo(MangaUiScreen.Detail(manga.id)) },
                        onLongClick = { showCategoryAssignDialog = manga }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // LIST MANAGER BOTTOM SHEET/DIALOG FOR CREATING AND DELETING LISTS
    if (showManageListsDialog) {
        AlertDialog(
            onDismissRequest = { showManageListsDialog = false },
            containerColor = cardBackground,
            title = {
                Text("إدارة قوائم المانجا المخصصة", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("أنشئ أو احذف تصنيفات لتنظيم المانجا بأسلوب Tachiyomi:", color = textSecColor, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Input for new list
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newListName,
                            onValueChange = { newListName = it },
                            placeholder = { Text("قائمة جديدة...", fontSize = 12.sp, color = textSecColor) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = textSecColor.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newListName.trim().isNotEmpty()) {
                                    viewModel.createReadingList(newListName)
                                    newListName = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("إضافة")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable category elements
                    Text("التصنيفات الحالية:", fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(readingLists.filter { it != "الكل" && it != "المفضلة" }) { listName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(listName, color = textColor, fontSize = 13.sp)
                                    IconButton(
                                        onClick = { viewModel.deleteReadingList(listName) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف القائمة",
                                            tint = PriorityHigh,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageListsDialog = false }) {
                    Text("تم", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // CATEGORY CONFIGURATOR DIALOG (FOR CHOSEN MANGA)
    showCategoryAssignDialog?.let { manga ->
        val userCategories = readingLists.filter { it != "الكل" && it != "المفضلة" }

        AlertDialog(
            onDismissRequest = { showCategoryAssignDialog = null },
            containerColor = cardBackground,
            title = {
                Text("تنظيم: ${manga.titleAr}", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("حدد القوائم المخصصة لتخزين هذه القصة بها:", color = textSecColor, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (userCategories.isEmpty()) {
                        Text("لا تتوفر قوائم مخصصة حالياً. يرجى إنشاء قائمة أولاً باستخدام زر 'إدارة القوائم'!", color = textSecColor, fontSize = 12.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(userCategories) { listName ->
                                val isChecked = viewModel.isMangaInReadingList(manga.id, listName)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleMangaInCategory(manga.id, listName) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { viewModel.toggleMangaInCategory(manga.id, listName) }
                                    )
                                    Text(listName, color = textColor, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryAssignDialog = null }) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// --- 2. BROWSE/CATALOG VIEW & Keiyoushi Extensions ---
@Composable
fun MangaCatalogView(viewModel: MangaViewModel) {
    val catalogMangas by viewModel.catalogMangas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val extensions by viewModel.keiyoushiExtensions.collectAsState()
    val repositories by viewModel.repositories.collectAsState()
    val selectedRepoUrl by viewModel.selectedRepository.collectAsState()

    val context = LocalContext.current
    var activeSegment by remember { mutableStateOf("sources") } // "sources" or "keiyoushi"

    var showAddRepoDialog by remember { mutableStateOf(false) }
    var repoUrlInput by remember { mutableStateOf("") }

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val cardBackground = if (isDarkMode) SlateCard else SlateLightCard
    val inputBackground = if (isDarkMode) SlateCard else Color(0xFFF1F5F9)

    // Filter available extensions to those belonging to the currently selected repository
    val activeRepoExts = remember(extensions, selectedRepoUrl) {
        extensions.filter { it.repoUrl == selectedRepoUrl }.distinctBy { it.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Upper Segmented Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkMode) SlateCard else Color(0xFFE2E8F0))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { activeSegment = "sources" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSegment == "sources") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeSegment == "sources") Color.White else textSecColor
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .height(38.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("القصص المتوفرة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { activeSegment = "keiyoushi" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSegment == "keiyoushi") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeSegment == "keiyoushi") Color.White else textSecColor
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .height(38.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("إدارة المستودعات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeSegment == "sources") {
            // Header
            Text(
                text = "تصفح بقارئ Mezo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "اكتشف آلاف الفصول والقصص المانجا المترجمة من مستودعاتك النشطة",
                fontSize = 12.sp,
                color = textSecColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("ابحث باسم المانجا أو المؤلف...", color = textSecColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f),
                    focusedContainerColor = inputBackground,
                    unfocusedContainerColor = inputBackground,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = textSecColor) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = textSecColor)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sources List Filtering Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "المصدر: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecColor,
                    modifier = Modifier.padding(end = 4.dp)
                )
                val sources = listOf("الكل") + extensions.filter { it.isInstalled && it.isEnabled }.map { ext ->
                    if (ext.id == "arab_manga") "بوابة المانجا" else ext.sourceName
                }.distinct()
                Box(modifier = Modifier.fillMaxWidth()) {
                    ScrollableRow(items = sources, selectedItem = selectedSource, isDarkMode = isDarkMode) {
                        viewModel.setSourceFilter(it)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Genres List Filtering Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "التصنيف: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecColor,
                    modifier = Modifier.padding(end = 4.dp)
                )
                val genres = listOf("الكل", "أكشن", "خيال", "مغامرة", "شونين", "دراما", "رعب")
                Box(modifier = Modifier.fillMaxWidth()) {
                    ScrollableRow(items = genres, selectedItem = selectedGenre, isDarkMode = isDarkMode) {
                        viewModel.setGenreFilter(it)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (catalogMangas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔍 لا نتائج متطابقة أو المستودعات معطلة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "فَعِّل مصادرك من تبويب المستودعات للتأكد من جلب المحتوى.",
                        fontSize = 12.sp,
                        color = textSecColor
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(catalogMangas, key = { it.id }) { manga ->
                        MangaGridCard(
                            manga = manga,
                            isDarkMode = isDarkMode,
                            onClick = { viewModel.navigateTo(MangaUiScreen.Detail(manga.id)) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        } else {
            // REPOSITORIES & SOURCES INDEX VIEW (إدارة المستودعات)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المستودعات والقصص",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    IconButton(
                        onClick = { showAddRepoDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة مستودع", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Text(
                    text = "قم بإدارة مستودعات المانجا الافتراضية والخاصة مع جلب مصادر فصولها الفعالة والتعامل مع روابط الـ JSON مباشرة",
                    fontSize = 12.sp,
                    color = textSecColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 1: Active Repositories
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("المستودعات المضافة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable/Grid list of active Repositories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repositories.forEach { repo ->
                        val isSelected = repo.url == selectedRepoUrl
                        Card(
                            onClick = { viewModel.selectRepository(repo.url) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else cardBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier.widthIn(min = 130.dp, max = 190.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = repo.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (repo.url == "local") "محلي مدمج" else "مستودع خارجي",
                                        fontSize = 9.sp,
                                        color = textSecColor
                                    )
                                }
                                if (repo.isCustom) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { viewModel.removeRepository(repo.url, context) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف المستودع",
                                            tint = PriorityHigh,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Simple shortcut "+" card
                    Card(
                        onClick = { showAddRepoDialog = true },
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(width = 50.dp, height = 44.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة مستودع", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Extensions inside the selected Repo
                val selectedRepoName = repositories.find { it.url == selectedRepoUrl }?.name ?: "المجهول"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("المصادر المتوفرة بـ ($selectedRepoName)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (activeRepoExts.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("لا تتوفر أي مصادر في هذا المستودع حالياً.", fontSize = 12.sp, color = textSecColor)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(activeRepoExts, key = { it.id }) { ext ->
                            ExtensionRowCard(
                                ext = ext,
                                isDarkMode = isDarkMode,
                                onInstall = { viewModel.installExtension(ext.id, context) },
                                onUninstall = { viewModel.uninstallExtension(ext.id, context) },
                                onToggle = { viewModel.toggleExtension(ext.id, context) }
                            )
                        }
                    }
                }
            }
        }
    }

    // New Repository Link Dialog
    if (showAddRepoDialog) {
        AlertDialog(
            onDismissRequest = { showAddRepoDialog = false },
            title = {
                Text(
                    text = "إضافة مستودع جديد",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "أدخل رابط الـ JSON index الخاص بالمستودع الذي تود جلبه في التطبيق لمزامنة مصادر فصول المانجا الإضافية تلقائياً:",
                        fontSize = 12.sp,
                        color = textSecColor,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Right
                    )
                    OutlinedTextField(
                        value = repoUrlInput,
                        onValueChange = { repoUrlInput = it },
                        placeholder = { Text("https://example.com/repo-index.json", fontSize = 12.sp, color = textSecColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = inputBackground,
                            unfocusedContainerColor = inputBackground
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = repoUrlInput.trim()
                        if (input.isNotEmpty()) {
                            viewModel.addRepository(
                                cleanUrl = input,
                                context = context,
                                onSuccess = { msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                }
                            )
                            repoUrlInput = ""
                            showAddRepoDialog = false
                        } else {
                            android.widget.Toast.makeText(context, "الرجاء إدخال الرابط أولاً!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("إضافة", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        repoUrlInput = ""
                        showAddRepoDialog = false
                    }
                ) {
                    Text("إلغاء", color = textSecColor)
                }
            }
        )
    }
}

// Keiyoushi Extension Row Card rendering
@Composable
fun ExtensionRowCard(
    ext: KeiyoushiExtension,
    isDarkMode: Boolean,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    onToggle: () -> Unit
) {
    val cardBg = if (isDarkMode) SlateCard else SlateLightCard
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight

    var isSimulatingDownload by remember { mutableStateOf(false) }
    var simProg by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (ext.isInstalled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated source logo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(ext.name.take(1), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(ext.name, fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (ext.isInstalled) PriorityLow.copy(alpha = 0.15f) else textSecColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (ext.isInstalled) "نشط" else "متاح في Keiyoushi",
                            fontSize = 8.sp,
                            color = if (ext.isInstalled) PriorityLow else textSecColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text("الإصدار: ${ext.version} • المصدر: ${ext.sourceName}", fontSize = 11.sp, color = textSecColor)
            }

            // Install/uninstall button actions with animated indicator feedback
            if (isSimulatingDownload) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = simProg, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            } else {
                if (!ext.isInstalled) {
                    Button(
                        onClick = {
                            scope.launch {
                                isSimulatingDownload = true
                                for (p in 1..4) {
                                    simProg = p * 0.25f
                                    kotlinx.coroutines.delay(180)
                                }
                                onInstall()
                                isSimulatingDownload = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تثبيت", fontSize = 11.sp)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Switch(
                            checked = ext.isEnabled,
                            onCheckedChange = { onToggle() },
                            modifier = Modifier.graphicsLayer(scaleX = 0.75f, scaleY = 0.75f)
                        )

                        IconButton(onClick = onUninstall) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "إزالة", tint = PriorityHigh, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// Horizontal tag scrollbar selector (categories)
@Composable
fun ScrollableRow(
    items: List<String>,
    selectedItem: String,
    isDarkMode: Boolean,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            val isSelected = selectedItem == item
            val itemBg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                         else if (isDarkMode) SlateCard else SlateLightCard

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(itemBg)
                    .clickable { onSelect(item) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item,
                    fontSize = 11.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDarkMode) TextSecondaryDark else TextSecondaryLight),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// --- 3. READING HISTORY VIEW (السجل) ---
@Composable
fun MangaHistoryView(viewModel: MangaViewModel) {
    val history by viewModel.readingHistory.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val cardBackground = if (isDarkMode) SlateCard else SlateLightCard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "سجل القراءة",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "تابع تقدم فصولك المقروءة حالياً",
                    fontSize = 12.sp,
                    color = textSecColor
                )
            }

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        Toast.makeText(context, "تم مسح سجل القراءة بالكامل", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear all",
                        tint = PriorityHigh,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(cardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = textSecColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "لا توجد قراءات حديثة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ستظهر أي فصول تفتحها وتقرأها هنا لتستطيع العثور عليها ومتابعتها لاحقاً.",
                    fontSize = 12.sp,
                    color = textSecColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.mangaId }) { item ->
                    HistoryRow(
                        history = item,
                        isDarkMode = isDarkMode,
                        onResume = { viewModel.navigateTo(MangaUiScreen.Reader(item.mangaId, item.chapterId)) },
                        onDelete = { viewModel.removeHistoryForManga(item.mangaId) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

// --- 4. THE AUTHENTICATION AND DYNAMIC PROFILE VIEW ---
@Composable
fun SettingsListItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    isExpanded: Boolean = false,
    onToggle: () -> Unit,
    textColor: Color,
    textSecColor: Color,
    cardBg: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = description,
                        color = textSecColor,
                        fontSize = 11.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = textSecColor,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer {
                            rotationZ = if (isExpanded) 90f else 0f
                        }
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = textSecColor.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
fun MangaProfileView(viewModel: MangaViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val libraryMangas by viewModel.libraryMangas.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState()
    val trackingServices by viewModel.trackingServices.collectAsState()
    val totalMinutesRead by viewModel.totalMinutesRead.collectAsState()
    val readingStreak by viewModel.readingStreak.collectAsState()
    val isVerticalReadingDefault by viewModel.isVerticalReading.collectAsState()
    val readingFontSizeDefault by viewModel.readingFontSize.collectAsState()

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDarkMode) SlateCard else SlateLightCard

    var isRegisterState by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isEditingProfile by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf("avatar_1") }
    
    var showConnectionDialogByService by remember { mutableStateOf<String?>(null) }
    var bindingUsernameInput by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var fileToRestoreConfirm by remember { mutableStateOf<File?>(null) }
    var isSettingsMode by remember { mutableStateOf(false) }

    var isTrackingExpanded by remember { mutableStateOf(false) }
    var isThemeExpanded by remember { mutableStateOf(false) }
    var isBackupExpanded by remember { mutableStateOf(false) }

    val availableBackups by viewModel.availableBackups.collectAsState()
    val autoBackupInterval by viewModel.autoBackupInterval.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSettingsMode) "إعدادات التطبيق" else "الملف الشخصي",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = if (isSettingsMode) "الوضع الليلى، خدمات التتبع، والتحكم بالنسخ الاحتياطي" else "سجل تقدمك وقوائمك الشخصية وسجل دخولك لمزامنتها",
                    fontSize = 11.sp,
                    color = textSecColor
                )
            }

            IconButton(
                onClick = { isSettingsMode = !isSettingsMode },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isSettingsMode) Icons.Default.Close else Icons.Default.Settings,
                    contentDescription = if (isSettingsMode) "الإعدادات" else "الملف الشخصي",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val current = currentUser
        if (current == null) {
            // AUTHENTICATION SCREEN FOR LOGGED-OUT USERS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mezo_logo_option5_1780289442777),
                        contentDescription = "Mezo Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isRegisterState) "إنشاء حساب جديد على Mezo" else "تسجيل الدخول إلى Mezo Manga",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "احفظ تقدم فصول المانجا وشارك في التعقيبات والمناقشة لـ Keiyoushi",
                        fontSize = 11.sp,
                        color = textSecColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isRegisterState) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            placeholder = { Text("اسم المستخدم") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor, unfocusedTextColor = textColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("البريد الإلكتروني") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor, unfocusedTextColor = textColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("كلمة المرور") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor, unfocusedTextColor = textColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (email.contains("@") && email.length > 5) {
                                val user = if (isRegisterState) username else email.substringBefore("@")
                                viewModel.loginUser(user.ifEmpty { "مستخدم Mezo" }, email)
                            } else {
                                viewModel.loginUser("مستخدم ميزو", "user@mezo.com")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(if (isRegisterState) "إنشاء حساب" else "تسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { isRegisterState = !isRegisterState }) {
                        Text(
                            text = if (isRegisterState) "لديك حساب بالفعل؟ سجل دخولك" else "ليس لديك حساب؟ قم بإنشائه الآن",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            if (isSettingsMode) {
                // --- SETTINGS LIST WITH COLLAPSIBLE ITEMS (MAINTENANCE DESIGN REFACTOR) ---

                // Section 1: Tracking & Sync Accounts
                SettingsListItem(
                    title = "مزامنة خدمات التتبع الخارجية",
                    description = "اربط حسابك لتتبع تقدم فصول المانجا ومزامنته تلقائياً مع المواقع العالمية",
                    icon = Icons.Default.Share,
                    iconColor = MaterialTheme.colorScheme.primary,
                    isExpanded = isTrackingExpanded,
                    onToggle = { isTrackingExpanded = !isTrackingExpanded },
                    textColor = textColor,
                    textSecColor = textSecColor,
                    cardBg = cardBg
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        trackingServices.filter { it.id != "local" }.forEach { service ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val logoColor = if (service.id == "mal") Color(0xFF2E51A2) else Color(0xFF3577FF)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(logoColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = service.name.take(1),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = service.name, color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (service.isConnected) {
                                            Text(text = "متصل باسم: ${service.username}", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text(text = "غير متصل", color = textSecColor, fontSize = 11.sp)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (service.isConnected) {
                                                viewModel.disconnectTrackingService(service.id)
                                            } else {
                                                showConnectionDialogByService = service.id
                                                bindingUsernameInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (service.isConnected) Color(0xFFEF4444).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary,
                                            contentColor = if (service.isConnected) Color(0xFFEF4444) else Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            if (service.isConnected) "إلغاء الربط" else "ربط الحساب",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Appearance & Reading Settings
                SettingsListItem(
                    title = "مظهر التطبيق وخيارات القراءة",
                    description = "التحكم بالوضع الليلي، اتجاه الصفحات الافتراضي وحجم خط الحوارات",
                    icon = Icons.Default.Settings,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    isExpanded = isThemeExpanded,
                    onToggle = { isThemeExpanded = !isThemeExpanded },
                    textColor = textColor,
                    textSecColor = textSecColor,
                    cardBg = cardBg
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Switch for Dark Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                                Text("الوضع الليلـي للمظهر", color = textColor, fontSize = 13.sp)
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() }
                            )
                        }

                        Divider(color = textSecColor.copy(alpha = 0.08f))

                        // Reading direction defaults
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("طريقة عرض الصفحات الافتراضية", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(if (isVerticalReadingDefault) "عرض عمودي مستمر (ويب تون)" else "عرض أفقي تقليدي (صفحة تلو الأخرى)", color = textSecColor, fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.toggleReadingDirection() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isVerticalReadingDefault) "التبديل لأفقي" else "التبديل لعمودي",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = textSecColor.copy(alpha = 0.08f))

                        // Custom default dialogues font size
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("حجم الخط الافتراضي لحوارات المانجا", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${readingFontSizeDefault.toInt()}sp", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = readingFontSizeDefault,
                                onValueChange = { viewModel.setReadingFontSize(it) },
                                valueRange = 10f..24f,
                                steps = 7,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Section 3: Safe Backups & Recovery
                SettingsListItem(
                    title = "النسخ الاحتياطي والاستعادة الذكية",
                    description = "احفظ تاريخ قراءتك، الفصول المفضلة، وإحصائيات تقدمك واستعدها بأي وقت محلياً",
                    icon = Icons.Default.Refresh,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    isExpanded = isBackupExpanded,
                    onToggle = { isBackupExpanded = !isBackupExpanded },
                    textColor = textColor,
                    textSecColor = textSecColor,
                    cardBg = cardBg
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "يتم تشفير وتخزين النسخ الاحتياطية محلياً لنقل تقدم حسابك وسجل مفضلاتك بسهولة دون اتصال بخوادم خارجية.",
                            color = textSecColor,
                            fontSize = 11.sp
                        )

                        Divider(color = textSecColor.copy(alpha = 0.08f))

                        // Auto-backup configuration
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "جدولة المزامنة التلقائية:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(
                                    "none" to "إيقاف",
                                    "daily" to "يومي",
                                    "weekly" to "أسبوعي",
                                    "monthly" to "شهري"
                                ).forEach { (value, label) ->
                                    val isSelected = autoBackupInterval == value
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else cardBg.copy(alpha = 0.5f))
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else textSecColor.copy(alpha = 0.15f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.setAutoBackupInterval(context, value) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            color = if (isSelected) Color.White else textSecColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = textSecColor.copy(alpha = 0.08f))

                        // Trigger operations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.createBackup(context) { success, result ->
                                        if (success) {
                                            Toast.makeText(context, "تم حفظ النسخة الاحتياطية بنجاح باسم $result", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "فشل إنشاء النسخة: $result", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسخ الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    importText = ""
                                    showImportDialog = true
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استيراد نص", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Local backups file list
                        if (availableBackups.isNotEmpty()) {
                            Divider(color = textSecColor.copy(alpha = 0.08f))
                            Text(
                                "النسخ المتوفرة المخزنة محلياً:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                availableBackups.take(5).forEach { file ->
                                    val isAuto = file.name.startsWith("auto_backup")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(textColor.copy(alpha = 0.03f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                if (isAuto) "نسخة تلقائية" else "نسخة يدوية",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isAuto) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                file.name.substringAfter("_").substringBefore(".json").replace("_", " "),
                                                color = textSecColor,
                                                fontSize = 9.sp
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        val clipData = android.content.ClipData.newPlainText("Mahyun Backup", file.readText())
                                                        clipboardManager.setPrimaryClip(clipData)
                                                        Toast.makeText(context, "تم نسخ محتوى ملف التتبع لمشاركته", Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "فشل نسخ النص", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Send,
                                                    contentDescription = "مشاركة",
                                                    tint = textSecColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { fileToRestoreConfirm = file },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "استعادة",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteBackupFile(context, file) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف",
                                                    tint = PriorityHigh,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Connection Popup Dialog
                if (showConnectionDialogByService != null) {
                    val serviceId = showConnectionDialogByService!!
                    val serviceName = if (serviceId == "mal") "MyAnimeList" else "AniList"
                    AlertDialog(
                        onDismissRequest = { showConnectionDialogByService = null },
                        title = {
                            Text(
                                text = "ربط وتأكيد حساب $serviceName",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    text = "أدخل اسم المستخدم لتأكيد تسجيل الدخول الفوري ومزامنة فصول المانجا التي تقرأها تلقائياً:",
                                    fontSize = 12.sp,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = bindingUsernameInput,
                                    onValueChange = { bindingUsernameInput = it },
                                    placeholder = { Text("اسم المستخدم في $serviceName") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor, unfocusedTextColor = textColor
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (bindingUsernameInput.trim().isNotEmpty()) {
                                        viewModel.connectTrackingService(serviceId, bindingUsernameInput.trim())
                                        showConnectionDialogByService = null
                                    }
                                }
                            ) {
                                Text("ربط الآن")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConnectionDialogByService = null }) {
                                Text("إلغاء")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modern stand-alone Sign Out Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.logoutUser() },
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PriorityHigh.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = PriorityHigh,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "تسجيل الخروج من الحساب الشخصي",
                                color = PriorityHigh,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = PriorityHigh,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { isSettingsMode = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardBg
                    )
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = textColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("العودة لمعلومات الحساب والإحصائيات", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // --- PRIMARY PROFILE PROFILE INFO & STATS VIEW ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar view
                            val avatarColor = when (current.avatarRes) {
                                "avatar_1" -> Color(0xFFEF4444)
                                "avatar_2" -> Color(0xFF3B82F6)
                                "avatar_3" -> Color(0xFF10B981)
                                "avatar_4" -> Color(0xFFF59E0B)
                                "avatar_5" -> Color(0xFF8B5CF6)
                                else -> Color(0xFFEC4899)
                            }

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = current.username.take(1).uppercase(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(current.username, fontWeight = FontWeight.Bold, color = textColor, fontSize = 18.sp)
                                Text(current.email, color = textSecColor, fontSize = 12.sp)
                                Text("عضوية نشطة منذ ${current.joinedDate}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            IconButton(onClick = { isEditingProfile = !isEditingProfile }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل الملف", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (isEditingProfile) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = textSecColor.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("تعديل الاسم والبريد الشخصي:", fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            var editName by remember { mutableStateOf(current.username) }
                            var editEmail by remember { mutableStateOf(current.email) }

                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                placeholder = { Text("الاسم") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor, unfocusedTextColor = textColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                placeholder = { Text("البريد الإلكتروني") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor, unfocusedTextColor = textColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("اختر لون الرمز الخاص بك:", fontWeight = FontWeight.Bold, color = textColor, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Colored dots for Avatar selection
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5", "avatar_6").forEach { av ->
                                    val dotColor = when (av) {
                                        "avatar_1" -> Color(0xFFEF4444)
                                        "avatar_2" -> Color(0xFF3B82F6)
                                        "avatar_3" -> Color(0xFF10B981)
                                        "avatar_4" -> Color(0xFFF59E0B)
                                        "avatar_5" -> Color(0xFF8B5CF6)
                                        else -> Color(0xFFEC4899)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                            .border(
                                                width = if (selectedAvatar == av) 2.dp else 0.dp,
                                                color = textColor,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedAvatar = av }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (editName.isNotEmpty() && editEmail.isNotEmpty()) {
                                        viewModel.updateProfile(editName, editEmail, selectedAvatar)
                                        isEditingProfile = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("حفظ التغييرات")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STATISTICS LAYOUT GRID
                Text("إحصائيات القراءة", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bookmarked count card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = PriorityHigh, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "${libraryMangas.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                            Text(text = "سلسلة بالمكتبة", fontSize = 11.sp, color = textSecColor, textAlign = TextAlign.Center)
                        }
                    }

                    // History reads card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "${readingHistory.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                            Text(text = "سلاسل مقروءة", fontSize = 11.sp, color = textSecColor, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total hours read card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            val hrs = totalMinutesRead.toFloat() / 60
                            Text(text = String.format("%.1f", hrs) + " س", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                            Text(text = "وقت القراءة الإجمالي", fontSize = 11.sp, color = textSecColor, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }

                    // Streak count card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "$readingStreak أيام", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                            Text(text = "سلسلة أيام القراءة", fontSize = 11.sp, color = textSecColor, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                }

                // Real dynamic Genre Analysis bar charts
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text("تحليل التصنيفات المفضلة لديك", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("أنواع المانجا الأكثر تكراراً في مكتبتك الشخصية بنسب بيانية", fontSize = 11.sp, color = textSecColor)

                        Spacer(modifier = Modifier.height(14.dp))

                        val genreCounts = remember(libraryMangas) {
                            val counts = mutableMapOf<String, Int>()
                            libraryMangas.forEach { manga ->
                                manga.genres.split(",").forEach { g ->
                                    val name = g.trim()
                                    if (name.isNotEmpty()) {
                                        counts[name] = (counts[name] ?: 0) + 1
                                    }
                                }
                            }
                            if (counts.isEmpty()) {
                                mapOf("أكشن" to 4, "مغامرة" to 3, "خيال" to 2, "دراما" to 1)
                            } else {
                                counts.toList().sortedByDescending { it.second }.take(4).toMap()
                            }
                        }

                        val maxCount = genreCounts.values.maxOrNull() ?: 1
                        genreCounts.forEach { (genre, count) ->
                            val percent = count.toFloat() / maxCount.toFloat()
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = genre, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "$count سلاسل", color = textSecColor, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(textColor.copy(alpha = 0.08f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(percent)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.secondary
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- ELEGANT CLICKABLE ROW/CARD LINKING TO SETTINGS ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSettingsMode = true },
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("اعدادات التطبيق والنسخ الاحتياطي", fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                            Text("الوضع الليلى، ربط حسابات تتبع الفصول، والنسخ ودورياته تلقائياً", color = textSecColor, fontSize = 11.sp)
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Standard arrows are auto-mirrored
                            contentDescription = null,
                            tint = textSecColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Confirm restore dialog
        if (fileToRestoreConfirm != null) {
            val file = fileToRestoreConfirm!!
            AlertDialog(
                onDismissRequest = { fileToRestoreConfirm = null },
                title = {
                    Text(
                        "تأكيد استعادة النسخة الاحتياطية",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                text = {
                    Text(
                        "هل أنت متأكد من رغبتك في استعادة هذه النسخة؟ سيقوم هذا باستبدال قائمة المانجا الحالية وسجل قراءتك ونقاط تقدمك بالكامل ببيانات ملف النسخة الاحتياطية.",
                        fontSize = 12.sp,
                        color = textSecColor
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.restoreBackupFromFile(context, file) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                fileToRestoreConfirm = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("استعادة الآن", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fileToRestoreConfirm = null }) {
                        Text("إلغاء", color = textSecColor)
                    }
                }
            )
        }

        // Custom JSON Import Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = {
                    Text(
                        "استيراد نسخة احتياطية (نص JSON)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "قم بلصق محتويات نص JSON الخاص بنسختك الاحتياطية هنا لاستعادة تتبعك بالكامل فوراً:",
                            fontSize = 11.sp,
                            color = textSecColor
                        )
                        OutlinedTextField(
                            value = importText,
                            onValueChange = { importText = it },
                            placeholder = { Text("{ \"version\": 1, ... }", fontSize = 11.sp) },
                            singleLine = false,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor, unfocusedTextColor = textColor
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (importText.trim().isNotEmpty()) {
                                viewModel.restoreBackupFromJson(context, importText.trim()) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) showImportDialog = false
                                }
                            }
                        },
                        enabled = importText.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("استيراد واستعادة", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("إلغاء", color = textSecColor)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- 5. DETAILS VIEW & Ratings, Comment and Offline downloader ---
@Composable
fun MangaDetailView(
    viewModel: MangaViewModel,
    onBack: () -> Unit
) {
    val manga by viewModel.selectedManga.collectAsState()
    val chapters by viewModel.selectedMangaChapters.collectAsState()
    val customRatings by viewModel.customRatings.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val readingLists by viewModel.readingLists.collectAsState()
    val downloadedChapters by viewModel.downloadedChapters.collectAsState()
    val downloadProgress by viewModel.downloadingChapterInProgress.collectAsState()
    val trackingServices by viewModel.trackingServices.collectAsState()
    val mangaTrackingProgressMap by viewModel.mangaTrackingProgress.collectAsState()

    val context = LocalContext.current
    val currentManga = manga ?: return

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val cardBackground = if (isDarkMode) SlateCard else SlateLightCard

    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            Color(currentManga.coverGradientStart),
            if (isDarkMode) SlateDark else SlateLightBg,
            if (isDarkMode) SlateDark else SlateLightBg
        )
    )

    var userCommentText by remember { mutableStateOf("") }
    var showCategorizerTrigger by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) SlateDark else SlateLightBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Splash cover gradient and back arrow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .background(headerGradient)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                MangaBookCoverArt(
                    gradientStart = currentManga.coverGradientStart,
                    gradientEnd = currentManga.coverGradientEnd,
                    title = currentManga.titleAr,
                    coverUrl = currentManga.coverUrl,
                    modifier = Modifier
                        .size(width = 110.dp, height = 160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadowElevation()
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val currentRating = customRatings[currentManga.id] ?: currentManga.rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", currentRating),
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${currentManga.ratingVotes + (if (customRatings.containsKey(currentManga.id)) 1 else 0)})",
                            color = textSecColor,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentManga.titleAr,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Text(
                        text = currentManga.titleEn,
                        fontSize = 13.sp,
                        color = textSecColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "المؤلف: ${currentManga.author}",
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )

                    Text(
                        text = "المصدر: ${currentManga.sourceName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Expanded Action buttons for Library, Native Sharing and category tagging
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isFavorite = currentManga.isBookmarked

            Button(
                onClick = {
                    viewModel.toggleLibraryBookmark(currentManga)
                    val msg = if (isFavorite) "تمت الإزالة من مكتبتي" else "تمت الإضافة لمكتبتي بنجاح!"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite) cardBackground else MaterialTheme.colorScheme.primary,
                    contentColor = if (isFavorite) textColor else Color.White
                ),
                border = if (isFavorite) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFavorite) "في المكتبة" else "حفظ بالمكتبة",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, currentManga.titleAr)
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "أقرأ الآن مانجا '${currentManga.titleAr}' الرائعة للمؤلف '${currentManga.author}' على تطبيق Mezo للمانجا! \n${currentManga.descriptionAr}"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة المانجا عبر..."))
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cardBackground,
                    contentColor = textColor
                ),
                border = BorderStroke(1.dp, textSecColor.copy(alpha = 0.2f)),
                modifier = Modifier.width(52.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة", modifier = Modifier.size(18.dp))
            }

            if (isFavorite) {
                Button(
                    onClick = { showCategorizerTrigger = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardBackground,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.width(52.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "تنظيم بقوائم", modifier = Modifier.size(18.dp))
                }
            }
        }

        // Details content tags
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentManga.genres.split(",").forEach { g ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(cardBackground)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = g.trim(),
                            fontSize = 11.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating prompt section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBackground)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ما هو تقييمك للمانجا؟", fontSize = 12.sp, color = textSecColor, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val activeMangaRating = customRatings[currentManga.id] ?: 0f
                    for (star in 1..5) {
                        val isStarred = star <= activeMangaRating
                        Icon(
                            imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Rate $star",
                            tint = if (isStarred) Color(0xFFFBBF24) else textSecColor,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    viewModel.rateManga(currentManga.id, star.toFloat())
                                    Toast.makeText(context, "شكرًا لتقييمك بـ $star نجوم!", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "قصة المانجا",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currentManga.descriptionAr,
                fontSize = 13.sp,
                color = textSecColor,
                lineHeight = 20.sp,
                textAlign = TextAlign.Start
            )

            // --- DYNAMIC EXTERNAL TRACKING SYNC PANEL ---
            Spacer(modifier = Modifier.height(18.dp))
            val connectedServices = trackingServices.filter { it.isConnected }
            if (connectedServices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBackground.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, textSecColor.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ربط تتبع الفصول (MAL / AniList)", fontWeight = FontWeight.Bold, color = textColor, fontSize = 13.sp)
                            Text("اربط حساب MyAnimeList أو AniList في صفحة الملف الشخصي لمزامنة فصولك تلقائياً هنا وبالمواقع العالمية.", fontSize = 11.sp, color = textSecColor)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تتبع ومزامنة الفصول (الخوادم السحابية)", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                        }
                        
                        connectedServices.forEach { service ->
                            Spacer(modifier = Modifier.height(10.dp))
                            val progressList = mangaTrackingProgressMap[currentManga.id] ?: emptyList()
                            val prog = progressList.find { it.serviceId == service.id }
                            
                            val currentStatus = prog?.status ?: "أخطط للقراءة"
                            val currentChapters = prog?.chaptersRead ?: 0
                            val currentScore = prog?.score ?: 0f
                            
                            val logoColor = when (service.id) {
                                "mal" -> Color(0xFF2E51A2)
                                "anilist" -> Color(0xFF3577FF)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(textColor.copy(alpha = 0.03f))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(logoColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(service.name.take(1), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(service.name, color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    
                                    // Status tag selector
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("أقرأ حالياً", "مخطط", "مكتمل", "متوقف").forEach { st ->
                                            val isSel = currentStatus == st
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSel) logoColor else cardBackground)
                                                    .border(1.dp, if (isSel) logoColor else textSecColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        viewModel.updateTrackingProgress(
                                                            mangaId = currentManga.id,
                                                            serviceId = service.id,
                                                            status = st,
                                                            chaptersRead = currentChapters,
                                                            score = currentScore
                                                        )
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(st, color = if (isSel) Color.White else textSecColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Chapter Incremental tracker
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("الفصول المقروءة:", color = textSecColor, fontSize = 11.sp)
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (currentChapters > 0) {
                                                    viewModel.updateTrackingProgress(
                                                        mangaId = currentManga.id,
                                                        serviceId = service.id,
                                                        status = currentStatus,
                                                        chaptersRead = currentChapters - 1,
                                                        score = currentScore
                                                    )
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("-", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        
                                        Text(
                                            "$currentChapters / ${chapters.size.coerceAtLeast(24)}",
                                            color = textColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        
                                        IconButton(
                                            onClick = {
                                                if (currentChapters < chapters.size.coerceAtLeast(24)) {
                                                    viewModel.updateTrackingProgress(
                                                        mangaId = currentManga.id,
                                                        serviceId = service.id,
                                                        status = currentStatus,
                                                        chaptersRead = currentChapters + 1,
                                                        score = currentScore
                                                    )
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("+", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Rating score slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تقييمك على ${service.name}:", color = textSecColor, fontSize = 11.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Slider(
                                            value = currentScore,
                                            onValueChange = {
                                                viewModel.updateTrackingProgress(
                                                    mangaId = currentManga.id,
                                                    serviceId = service.id,
                                                    status = currentStatus,
                                                    chaptersRead = currentChapters,
                                                    score = it
                                                )
                                            },
                                            valueRange = 0f..10f,
                                            steps = 9,
                                            modifier = Modifier.width(120.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = String.format("%.1f", currentScore), color = logoColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فصول القصة (${chapters.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = textSecColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "حدث أولاً", fontSize = 11.sp, color = textSecColor)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (chapters.isEmpty()) {
                Text("تجرى الصيانة وجاري تحميل فصول المصدر...", fontSize = 12.sp, color = textSecColor)
            } else {
                chapters.forEach { chapter ->
                    val isDownloaded = downloadedChapters.contains(chapter.id)
                    val activeProgress = downloadProgress[chapter.id]

                    ChapterRowWithDownload(
                        chapter = chapter,
                        isDarkMode = isDarkMode,
                        isDownloaded = isDownloaded,
                        downloadProgress = activeProgress,
                        onRead = { viewModel.navigateTo(MangaUiScreen.Reader(currentManga.id, chapter.id)) },
                        onDownload = {
                            viewModel.downloadChapter(chapter.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "تعليقات القراء ومناقشات الفصول",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("أحدث مشاركات المانجا من مجتمع Mezo العربي:", color = textSecColor, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(10.dp))

            val relevantComments = comments.filter { it.mangaId == currentManga.id }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userCommentText,
                    onValueChange = { userCommentText = it },
                    placeholder = { Text("أضف تعليقك الآن حول القصة...", fontSize = 12.sp, color = textSecColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = textSecColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (userCommentText.trim().isNotEmpty()) {
                            viewModel.addComment(currentManga.id, null, "أنت (زائر ميزو)", userCommentText)
                            userCommentText = ""
                            Toast.makeText(context, "تم نشر تعليقك بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "إرسال", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (relevantComments.isEmpty()) {
                Text("لا توجد تعليقات هنا للمانجا هذه بعد. كن أول من يكتب تعليقاً ممتعاً!", color = textSecColor, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    relevantComments.forEach { comment ->
                        CommentItemBlock(
                            comment = comment,
                            isDarkMode = isDarkMode,
                            onLike = { viewModel.toggleLikeComment(comment.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showCategorizerTrigger) {
        val userCategories = readingLists.filter { it != "الكل" && it != "المفضلة" }
        AlertDialog(
            onDismissRequest = { showCategorizerTrigger = false },
            containerColor = cardBackground,
            title = {
                Text("أضف المانجا لقوائم القراءة المخصصة", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("حدد القوائم المصنفة لتخزين هذه المغامرة تلقائياً:", color = textSecColor, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (userCategories.isEmpty()) {
                        Text("يرجى إنشاء تصنيفات أولاً عبر المكتبة -> إدارة قوائم القراءة للوصول السريع!", color = textSecColor, fontSize = 12.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(userCategories) { catName ->
                                val isChecked = viewModel.isMangaInReadingList(currentManga.id, catName)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleMangaInCategory(currentManga.id, catName) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { viewModel.toggleMangaInCategory(currentManga.id, catName) }
                                    )
                                    Text(catName, color = textColor, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategorizerTrigger = false }) {
                    Text("تم وحفظ", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Chapter Row including offline downloader capability
@Composable
fun ChapterRowWithDownload(
    chapter: ChapterEntity,
    isDarkMode: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    onRead: () -> Unit,
    onDownload: () -> Unit
) {
    val indicatorColor = if (chapter.isRead) (if (isDarkMode) TextSecondaryDark.copy(alpha = 0.4f) else Color.LightGray)
                         else MaterialTheme.colorScheme.primary
    val cardBg = if (chapter.isRead) (if (isDarkMode) SlateCard.copy(alpha = 0.5f) else SlateLightCard.copy(alpha = 0.6f))
                 else (if (isDarkMode) SlateCard else SlateLightCard)
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chapter_row_${chapter.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, if (chapter.isRead) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onRead() }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = chapter.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (chapter.isRead) textColor.copy(alpha = 0.5f) else textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isDownloaded) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PriorityLow.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("دون اتصال", color = PriorityLow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "تاريخ الإضافة: ${chapter.releaseDate} • ${chapter.totalPages} صفحات",
                        fontSize = 10.sp,
                        color = textSecColor
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (downloadProgress != null) {
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = downloadProgress, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                } else {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.Check else Icons.Default.ArrowDownward,
                            contentDescription = "تحميل الفصل",
                            tint = if (isDownloaded) PriorityLow else textSecColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (chapter.isRead) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("مكتمل", fontSize = 9.sp, color = textSecColor, fontWeight = FontWeight.Bold)
                    }
                } else if (chapter.lastReadPage > 0) {
                    val progress = ((chapter.lastReadPage + 1).toFloat() / chapter.totalPages * 100).toInt()
                    Text(
                        text = "صفحة ${chapter.lastReadPage + 1} ($progress%)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Visual Comment list element card
@Composable
fun CommentItemBlock(
    comment: MangaComment,
    isDarkMode: Boolean,
    onLike: () -> Unit
) {
    val cardBg = if (isDarkMode) SlateCard.copy(alpha = 0.4f) else Color.White
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight

    val avatarColor = when (comment.avatarIndex % 5) {
        0 -> Color(0xFFEF4444)
        1 -> Color(0xFF3B82F6)
        2 -> Color(0xFF10B981)
        3 -> Color(0xFFF59E0B)
        else -> Color(0xFF8B5CF6)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, textSecColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.author.take(1).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(comment.author, fontWeight = FontWeight.Bold, color = textColor, fontSize = 12.sp)
                    Text(comment.timestamp, color = textSecColor, fontSize = 9.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(comment.content, color = textColor.copy(alpha = 0.9f), fontSize = 11.sp, textAlign = TextAlign.Start)

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Icon(
                        imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (comment.isLiked) PriorityHigh else textSecColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${comment.likes} إعجاب", color = textSecColor, fontSize = 9.sp)
                }
            }
        }
    }
}

// --- 6. CINEMATIC FULLSCREEN READER & Font Config panel ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaReaderView(
    viewModel: MangaViewModel,
    onBack: () -> Unit
) {
    val manga by viewModel.selectedManga.collectAsState()
    val chapter by viewModel.activeChapter.collectAsState()
    val isVerticalReading by viewModel.isVerticalReading.collectAsState()
    val readingFontSize by viewModel.readingFontSize.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val comments by viewModel.comments.collectAsState()

    val currentManga = manga ?: return
    val currentChapter = chapter ?: return

    var activePage by remember(currentChapter.id) {
        val maxPage = (currentChapter.totalPages - 1).coerceAtLeast(0)
        mutableStateOf(currentChapter.lastReadPage.coerceIn(0, maxPage))
    }
    var hideOverlays by remember { mutableStateOf(false) }
    var showQuickSettingsPanel by remember { mutableStateOf(false) }
    var showCommentsDrawerSheet by remember { mutableStateOf(false) }
    var readerCommentText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val totalPages = currentChapter.totalPages
    val pagesList = (1..totalPages).toList()

    // Overlay colors
    val hudBackground = Color.Black.copy(alpha = 0.9f)
    val overlayTextCol = Color.White
    val overlayTextSecCol = Color.LightGray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070B)) // Amoled pure black
    ) {
        if (isVerticalReading) {
            val clampedActivePage = activePage.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
            val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = clampedActivePage)

            LaunchedEffect(scrollState.firstVisibleItemIndex) {
                activePage = scrollState.firstVisibleItemIndex
                viewModel.recordProgress(
                    mangaId = currentManga.id,
                    chapterId = currentChapter.id,
                    page = activePage,
                    maxPages = totalPages,
                    isCompleted = activePage == totalPages - 1
                )
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { hideOverlays = !hideOverlays }
                        )
                    }
            ) {
                items(pagesList) { page ->
                    SimulatedMangaPage(
                        mangaId = currentManga.id,
                        pageNumber = page,
                        chapterTitle = currentChapter.title,
                        customFontSize = readingFontSize,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(680.dp)
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "قد انتهيت من قراءة هذا الفصل! 🎉",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showCommentsDrawerSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تعليقات القراء", color = Color.White)
                            }

                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = SlateCard)
                            ) {
                                Text(text = "قائمة الفصول", color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            val clampedActivePage = activePage.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
            val pagerState = rememberPagerState(initialPage = clampedActivePage) { totalPages }

            LaunchedEffect(pagerState.currentPage) {
                activePage = pagerState.currentPage
                viewModel.recordProgress(
                    mangaId = currentManga.id,
                    chapterId = currentChapter.id,
                    page = activePage,
                    maxPages = totalPages,
                    isCompleted = activePage == totalPages - 1
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { hideOverlays = !hideOverlays }
                        )
                    }
            ) { page ->
                SimulatedMangaPage(
                    mangaId = currentManga.id,
                    pageNumber = page + 1,
                    chapterTitle = currentChapter.title,
                    customFontSize = readingFontSize,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        AnimatedVisibility(
            visible = !hideOverlays,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(hudBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentManga.titleAr,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = overlayTextCol
                        )
                        Text(
                            text = currentChapter.title,
                            fontSize = 11.sp,
                            color = overlayTextSecCol
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { showQuickSettingsPanel = !showQuickSettingsPanel }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "تخصيص الحجم", tint = overlayTextCol)
                        }

                        IconButton(onClick = { showCommentsDrawerSheet = true }) {
                            Icon(imageVector = Icons.Default.Comment, contentDescription = "التعليقات", tint = overlayTextCol)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                .clickable { viewModel.toggleReadingDirection() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isVerticalReading) "ويب تون ↕" else "مانجا ↔",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = overlayTextCol)
                        }
                    }
                }
            }
        }

        if (showQuickSettingsPanel && !hideOverlays) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp, start = 12.dp, end = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(hudBackground)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حجم خط نصوص المحادثة (عربي)", color = overlayTextCol, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${readingFontSize.toInt()}sp", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = readingFontSize,
                        onValueChange = { viewModel.setReadingFontSize(it) },
                        valueRange = 10f..24f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        if (!hideOverlays) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(hudBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "صفحة ${activePage + 1} من $totalPages",
                    color = overlayTextCol,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showCommentsDrawerSheet) {
            AlertDialog(
                onDismissRequest = { showCommentsDrawerSheet = false },
                containerColor = if (isDarkMode) SlateDark else SlateLightCard,
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تعقيبات الفصل", color = if (isDarkMode) Color.White else TextPrimaryLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showCommentsDrawerSheet = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val activeChComments = comments.filter { it.chapterId == currentChapter.id || (it.id.startsWith("c_user") && it.chapterId == null) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = readerCommentText,
                                onValueChange = { readerCommentText = it },
                                placeholder = { Text("اكتب تعقيبًا على الفصل الأسطوريّ...") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDarkMode) Color.White else TextPrimaryLight,
                                    unfocusedTextColor = if (isDarkMode) Color.White else TextPrimaryLight
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (readerCommentText.isNotEmpty()) {
                                        viewModel.addComment(currentManga.id, currentChapter.id, "أنت (قارئ ميزو)", readerCommentText)
                                        readerCommentText = ""
                                    }
                                },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .size(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "إرسال", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (activeChComments.isEmpty()) {
                                item { 
                                    Text("كن أول من يعلّق ويشارك نظرياته حول هذا الفصل!", color = if (isDarkMode) TextSecondaryDark else TextSecondaryLight, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) 
                                }
                            } else {
                                items(activeChComments) { comment ->
                                    CommentItemBlock(
                                        comment = comment,
                                        isDarkMode = isDarkMode,
                                        onLike = { viewModel.toggleLikeComment(comment.id) }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCommentsDrawerSheet = false }) {
                        Text("تم")
                    }
                }
            )
        }
    }
}

// Visual Book Cover Canvas component
@Composable
fun MangaBookCoverArt(
    gradientStart: Long,
    gradientEnd: Long,
    title: String,
    modifier: Modifier = Modifier,
    coverUrl: String = ""
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(gradientStart), Color(gradientEnd))
                )
            )
    ) {
        if (coverUrl.isNotEmpty()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val centerOffset = Offset(size.width * 0.5f, size.height * 0.45f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.45f,
                    center = centerOffset
                )
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = size.width * 0.32f,
                    center = centerOffset
                )
            }
        }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(Color.White.copy(alpha = 0.2f))
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "MANGA",
                    fontSize = 7.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(4.dp)
            )
        }
    }
}

// Simulated comic page pane
@Composable
fun SimulatedMangaPage(
    mangaId: String,
    pageNumber: Int,
    chapterTitle: String,
    customFontSize: Float,
    modifier: Modifier = Modifier
) {
    val pageColors = when (mangaId) {
        "demon_slayer" -> listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
        "one_piece" -> listOf(Color(0xFF0A1E3F), Color(0xFF081223))
        "attack_on_titan" -> listOf(Color(0xFF2D0B0B), Color(0xFF0E0303))
        "solo_leveling" -> listOf(Color(0xFF0C2429), Color(0xFF060D0E))
        else -> listOf(Color(0xFF0A2B1D), Color(0xFF05100B))
    }

    Box(
        modifier = modifier
            .background(Brush.verticalGradient(colors = pageColors))
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
    ) {
        AsyncImage(
            model = "https://picsum.photos/seed/${mangaId}_p${pageNumber}/800/1200",
            contentDescription = "Page $pageNumber",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "صفحة $pageNumber - $chapterTitle",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            val sfx = when (pageNumber % 3) {
                0 -> Pair("بوممم!", Color(0xFFEF4444))
                1 -> Pair("تشااا!", Color(0xFF0EA5E9))
                else -> Pair("وووششش!", Color(0xFFF59E0B))
            }
            Text(
                text = sfx.first,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = sfx.second,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    val dialogueText = getRealisticMangaDialogue(mangaId, pageNumber)
                    Text(
                        text = dialogueText,
                        color = Color.Black,
                        fontSize = customFontSize.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}

// Dialogue scripts according to story steps
fun getRealisticMangaDialogue(mangaId: String, page: Int): String {
    return when (mangaId) {
        "demon_slayer" -> when (page) {
            1 -> "تانجيرو: نيزوكو! ارجوكِ تمسكي بالبقاء، سأجد بالتأكيد طريقة كفيلة بإنقاذك!"
            2 -> "نيزوكو: (همهمات غاضبة متصارعة كشيطان يصارع إنسانيته بمشقة)"
            3 -> "صوت قادم من المجهول: تراجع يا فتى! قتلة الشياطين وجدوا لأداء هذا الواجب!"
            4 -> "تانجيرو: لا تقتلها! أقسم لك بدمي وجسدي، لن أسمح لها بإيذاء أي إنسان!"
            else -> "توميوكا غيو: الضعاف لا يملكون خيارات أو حلولاً لحماية أحبائهم!"
        }
        "one_piece" -> when (page) {
            1 -> "لوفي: سأجمع طاقماً من الأصدقاء لا يقهر، وسنجد كنز الون بيس العظيم!"
            2 -> "شانكس: صبي صغير مثلك يبكي من لكمة واحدة لا يستطيع تحمّل عبء البحر يا لوفي!"
            3 -> "لوفي: لن أبكي بعد الآن! هذه القبعة وهبتها لي، وسأثبت جدارتي يوماً ما!"
            else -> "زورو: لوفي، إن أردت قيادة قراصنة المستقبل، فيجب أن أكون أعظم سياف بالعالم!"
        }
        "attack_on_titan" -> when (page) {
            1 -> "المواطنون: يا إلهي... انظروا للأعلى! السور... لقد تم تدمير السور الخارجي تماماً!"
            2 -> "إيرين: أمي! أمي! ارجوكِ امسكي بيدي، العمالقة يقتربون من هنا!"
            3 -> "هانيس: تراجع يا إيرين! من واجب الجنود حماية الأجيال الناشئة والرحيل بأمان!"
            else -> "إيرين: سأبيدهم جميعاً... حبة حية، عمالقة السور وكل عدو عابر!"
        }
        "solo_leveling" -> when (page) {
            1 -> "سونغ جين وو: لقبت بأضعف صياد، لكن الموت داخل هذا المعبد عار ترفضه عائلتي."
            2 -> "النظام الغامض: [لقد أكملت متطلبات المهمة السرية لوراثة العرش الخفي]"
            3 -> "جين وو: هل هذا يعني... أنه يمكنني اكتساب ومضاعفة مستواي بشكل لانهائي?"
            else -> "النظام: [جارٍ ترقية الخصائص... تهانينا لقد صرت الصياد الأوحد للظلال]"
        }
        else -> when (page) {
            1 -> "يوجي: سأابتلع هذا الإصبع الخبيث! لا سبيل لإنقاذ الأبرياء إلا باكتساب قوى كافية!"
            2 -> "سوكونا: ههههه! أخيراً، خرجت للوجود بعد ألف عام من الحبس الكئيب!"
            3 -> "غوجو ساتورو: لا داعي للقلق يوجي, فبصفتي الأقوى سأتكفل بأي خطر ناتج!"
            else -> "يوجي: سحر مجابهة اللعنات يعني اتخاذ قرارات شجاعة حتى النهاية!"
        }
    }
}

// --- Dynamic Graphic Cards and Details ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaGridCard(
    manga: MangaEntity,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
            .testTag("manga_card_${manga.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MangaBookCoverArt(
                gradientStart = manga.coverGradientStart,
                gradientEnd = manga.coverGradientEnd,
                title = manga.titleAr,
                coverUrl = manga.coverUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shadowElevation()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = manga.titleAr,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else TextPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = manga.status,
                fontSize = 10.sp,
                color = if (manga.status == "مستمر") PriorityLow else (if (isDarkMode) TextSecondaryDark else TextSecondaryLight),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HistoryRow(
    history: HistoryEntity,
    isDarkMode: Boolean,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBg = if (isDarkMode) SlateCard else SlateLightCard
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onResume() }
            .testTag("history_item_${history.mangaId}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, textSecColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MangaBookCoverArt(
                gradientStart = history.coverGradientStart,
                gradientEnd = history.coverGradientEnd,
                title = history.mangaTitleAr,
                modifier = Modifier
                    .size(width = 46.dp, height = 64.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = history.mangaTitleAr,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "وصلت لـ: ${history.chapterTitle}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                val progressPercent = ((history.lastReadPage + 1).toFloat() / history.totalPages * 100).toInt()
                Text(
                    text = "صفحة ${history.lastReadPage + 1} من ${history.totalPages} ($progressPercent%)",
                    fontSize = 10.sp,
                    color = textSecColor
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onResume) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume reading",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete from history",
                        tint = textSecColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun Modifier.shadowElevation() = this.drawBehind {
    drawRect(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(4f, 4f),
        size = size
    )
}

// === NEW MIHON VIEWS DEFINED TO MATCH SCREENSHOTS ===

@Composable
fun MangaBackHeader(
    title: String,
    onBack: () -> Unit,
    trailingContent: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "عودة",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

// --- 1. UPDATES VIEW (التحديثات) ---
@Composable
fun MangaUpdatesView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val context = LocalContext.current

    val dummyUpdates = remember {
        listOf(
            Pair("The S Rank Butler", "فصل 45 • اليوم في ١١:٢٢ ص"),
            Pair("The Apocalypse has co...", "فصل 57 • اليوم في ١٠:٤٠ ص"),
            Pair("Logging 10.000 Years in...", "فصل 324 • اليوم في ٠٨:١٥ ص"),
            Pair("The Ultimate Shut-In", "الفصل 81 - موطئ قدم في أولسا... • أمس"),
            Pair("The Eternal Supreme", "الفصل 534 - حجر قوس قزح • أمس"),
            Pair("The Eternal Supreme", "الفصل 533 - حماية الحرس • أمس"),
            Pair("Demon Slayer", "الفصل 205 - نهاية القوى الروحية • قبل ٣ أيام"),
            Pair("One Piece", "الفصل 1111 - مواجهة العمالقة • قبل ٤ أيام")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "التحديثات",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "آخر تحديث للمكتبة: قبل ٧ ساعات",
            fontSize = 11.sp,
            color = textSecColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "اليوم",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dummyUpdates) { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) SlateCard else SlateLightCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left action button (Download)
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "بدء تنزيل فصل ${item.first}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "تنزيل",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Text Info
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = item.first,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.second,
                                fontSize = 12.sp,
                                color = textSecColor,
                                textAlign = TextAlign.Right
                            )
                        }

                        // Red dot for unread status
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PriorityHigh)
                        )
                    }
                }
            }
        }
    }
}

// --- 2. MORE VIEW (المزيد) ---
@Composable
fun MangaMoreView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val downloadOnly by viewModel.downloadOnly.collectAsState()
    val incognitoMode by viewModel.incognitoMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Upper Profile section matching Mihon logo design
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currentUser?.username ?: "مستكشف المانجا المجهول",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = currentUser?.email ?: "الاشتراك المجاني • انضم يونيو ٢٠٢٦",
                    fontSize = 12.sp,
                    color = textSecColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Profile circular avatar framed by a dynamic accent board
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Toggles block
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) SlateCard else SlateLightCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Toggle 1: Download Only
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = downloadOnly,
                        onCheckedChange = { viewModel.setDownloadOnly(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "المنزل فقط",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "تجنب التنزيل الإجباري للمحتوى غير المخزن مؤقتاً",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "تحميل فقط",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                // Toggle 2: Incognito Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = incognitoMode,
                        onCheckedChange = { viewModel.setIncognitoMode(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "الوضع المخفي",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "إيقاف حفظ سجل القراءة مؤقتاً وبشكل مؤقت",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "وضع مخفي",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Action Options
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) SlateCard else SlateLightCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                MangaMoreMenuItem(
                    icon = Icons.Default.Download,
                    title = "قائمة التنزيلات",
                    subtitle = "تنزيل الفصول ومتابعة الطابور المجدول",
                    onClick = { viewModel.navigateTo(MangaUiScreen.Downloads) }
                )
                MangaMoreMenuItem(
                    icon = Icons.Default.Category,
                    title = "الفئات والمجموعات",
                    subtitle = "تحرير وتخصيص الفئات لتنظيم مكتبتك",
                    onClick = { viewModel.navigateTo(MangaUiScreen.Categories) }
                )
                MangaMoreMenuItem(
                    icon = Icons.Default.BarChart,
                    title = "إحصائيات القراءة",
                    subtitle = "إجمالي الدقائق، الرقابة الذاتية والسلاسل المفضلة",
                    onClick = { viewModel.navigateTo(MangaUiScreen.Stats) }
                )
                MangaMoreMenuItem(
                    icon = Icons.Default.Storage,
                    title = "البيانات والتخزين",
                    subtitle = "النسخ الاحتياطي، الاستعادة وتخزين الملفات بالهاتف",
                    onClick = { viewModel.navigateTo(MangaUiScreen.Backup) }
                )
                MangaMoreMenuItem(
                    icon = Icons.Default.Settings,
                    title = "الإعدادات العامة",
                    subtitle = "التحكم بالمظهر، السمات، والميزات المتقدمة للتطبيق",
                    onClick = { viewModel.navigateTo(MangaUiScreen.Settings) }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun MangaMoreMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (MaterialTheme.colorScheme.background == Color.Black) TextSecondaryDark else TextSecondaryLight,
                textAlign = TextAlign.Right
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

// --- 3. SETTINGS ROOT VIEW (الإعدادات العامة) ---
@Composable
fun MangaSettingsView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val themeAccentPreset by viewModel.themeAccentPreset.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MangaBackHeader(
            title = "الإعدادات",
            onBack = { viewModel.navigateBack() },
            trailingContent = {
                IconButton(onClick = { Toast.makeText(context, "البحث في الإعدادات...", Toast.LENGTH_SHORT).show() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) SlateCard else SlateLightCard
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                MangaSettingsItem(
                    icon = Icons.Default.Palette,
                    title = "المظهر",
                    subtitle = "السمات المخصصة، الوضع الليلي ($themeAccentPreset)",
                    onClick = { viewModel.navigateTo(MangaUiScreen.AppearanceSettings) }
                )
                MangaSettingsItem(
                    icon = Icons.Default.LibraryBooks,
                    title = "المكتبة",
                    subtitle = "قوائم التصنيفات، التحديث التلقائي والقيود الفنية",
                    onClick = { viewModel.navigateTo(MangaUiScreen.LibrarySettings) }
                )
                MangaSettingsItem(
                    icon = Icons.Default.MenuBook,
                    title = "القارئ",
                    subtitle = "إتجاه القراءة الافتراضي، إعدادات الحواف واللمس",
                    onClick = { Toast.makeText(context, "القارئ: اتجاه القراءة الافتراضي عمودي", Toast.LENGTH_SHORT).show() }
                )
                MangaSettingsItem(
                    icon = Icons.Default.Download,
                    title = "التنزيلات",
                    subtitle = "موقع التخزين، حدود التنزيل المجدولة وصور الفصول",
                    onClick = { Toast.makeText(context, "تم ضبط الحدود الفنية للتنزيلات", Toast.LENGTH_SHORT).show() }
                )
                MangaSettingsItem(
                    icon = Icons.Default.Sync,
                    title = "التتبع",
                    subtitle = "ربط وتتبع حسابات AniList / MyAnimeList ومزامنة تقدمك",
                    onClick = { Toast.makeText(context, "تتبع الحسابات نشط ومستقر", Toast.LENGTH_SHORT).show() }
                )
                MangaSettingsItem(
                    icon = Icons.Default.Explore,
                    title = "تصفح ومصادر القراءة",
                    subtitle = "إضافات ومستودعات Keiyoushi والتحكم بالمواقع ومحركات البحث",
                    onClick = { viewModel.navigateTo(MangaUiScreen.Sources) }
                )
                MangaSettingsItem(
                    icon = Icons.Default.Backup,
                    title = "البيانات والنسخ الاحتياطي",
                    subtitle = "حفظ نسخة مشفرة على الهاتف واستردادها بلمسة وحدة",
                    onClick = { viewModel.navigateTo(MangaUiScreen.Profile) } // Profile screen has the backup view built-in!
                )
                MangaSettingsItem(
                    icon = Icons.Default.Security,
                    title = "الأمان والخصوصية",
                    subtitle = "تأمين بصمة الإصبع والوجه، سجل مؤقت تلقائي وتأمين الروابط",
                    onClick = { Toast.makeText(context, "الأمان مفعل ومحدث بآخر حزمة سرية", Toast.LENGTH_SHORT).show() }
                )
                MangaSettingsItem(
                    icon = Icons.Default.Build,
                    title = "إعدادات متقدمة",
                    subtitle = "مسح ذاكرة التخزين المؤقت، سجلات الشوائب وتقارير التحميل",
                    onClick = { Toast.makeText(context, "تم مسح وتصفير ذاكرة التخزين المؤقت للتطبيق بنجاح", Toast.LENGTH_SHORT).show() }
                )
                MangaSettingsItem(
                    icon = Icons.Default.Info,
                    title = "حول التطبيق",
                    subtitle = "اصدار ميهون v0.17.0 • مفتوح المصدر ومرخص لعام ٢٠٢٦",
                    onClick = { Toast.makeText(context, "تطبيق Mihon Hub v0.17.0 • تم التطوير بالحب والشغف", Toast.LENGTH_LONG).show() }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MangaSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (MaterialTheme.colorScheme.background == Color.Black) TextSecondaryDark else TextSecondaryLight,
                textAlign = TextAlign.Right
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

// --- 4. APPEARANCE SETTINGS VIEW (إعدادات المظهر والسمات الكوزمية) ---
@Composable
fun MangaAppearanceSettingsView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val pureBlackDark by viewModel.pureBlackDark.collectAsState()
    val relativeTime by viewModel.relativeTime.collectAsState()
    val renderMangaCovers by viewModel.renderMangaCovers.collectAsState()
    val themeAccentPreset by viewModel.themeAccentPreset.collectAsState()

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val context = LocalContext.current

    val presets = remember {
        listOf(
            "منتصف الليل",
            "الافتراضي",
            "كاتبوتشينو",
            "أخضر",
            "أرجواني",
            "Nord",
            "أحمر",
            "تاكو",
            "ازرق مخضر",
            "ين & يانغ",
            "يوتسوبا",
            "موجة مد و جزر"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MangaBackHeader(
            title = "المظهر والسمات",
            onBack = { viewModel.navigateBack() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented theme light/dark toggle
        Text(
            text = "تفضيل وضع الإضاءة للواجهة",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.End)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkMode) SlateCard else Color(0xFFE2E8F0)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Light Option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { if (isDarkMode) { viewModel.toggleDarkMode() } }
                    .background(if (!isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "فاتح (Light)",
                    color = if (!isDarkMode) Color.White else textSecColor,
                    fontWeight = if (!isDarkMode) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }

            // Dark Option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { if (!isDarkMode) { viewModel.toggleDarkMode() } }
                    .background(if (isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "داكن (Dark)",
                    color = if (isDarkMode) Color.White else textSecColor,
                    fontWeight = if (isDarkMode) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid selection of dynamic presets
        Text(
            text = "السمة المخصصة (Theme Presets)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .align(Alignment.End)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .height(290.dp)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                val isActive = themeAccentPreset == preset
                val col = getPrimaryColorForPreset(preset)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) SlateCard else SlateLightCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setThemeAccentPreset(preset) }
                        .border(
                            width = if (isActive) 2.dp else 0.5.dp,
                            color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(col),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = if (preset == "ين & يانغ") Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preset,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom display options block
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) SlateCard else SlateLightCard
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Toggle 1: Pure Black
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = pureBlackDark,
                        onCheckedChange = { viewModel.setPureBlackDark(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "وضع داكن الأسود النقي",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "تفعيل خلفية سوداء نقية (AMOLED) لتوفير البطارية",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                }

                Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                // Toggle 2: Relative time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = relativeTime,
                        onCheckedChange = { viewModel.setRelativeTime(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "التوقيت النسبي",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "عرض كلمات كـ «اليوم» بدلاً عن تاريخ اليوم الرقمي الكامل",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                }

                Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                // Toggle 3: Render covers inside descriptions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = renderMangaCovers,
                        onCheckedChange = { viewModel.setRenderMangaCovers(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "تقديم الصور بأوصاف المانجا",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "سرعة معالجة الرسوم وتحميل غلاف فصول المانجا داخل تفاصيلها",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// --- 5. LIBRARY SETTINGS VIEW (إعدادات المكتبة الذكية وتصرف التمرير) ---
@Composable
fun MangaLibrarySettingsView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val sortSettingsPerCategory by viewModel.sortSettingsPerCategory.collectAsState()
    val autoUpdateMetadata by viewModel.autoUpdateMetadata.collectAsState()
    val showUnreadCountBadge by viewModel.showUnreadCountBadge.collectAsState()
    val hideMissingChapters by viewModel.hideMissingChapters.collectAsState()

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MangaBackHeader(
            title = "المكتبة وتحديث البيانات",
            onBack = { viewModel.navigateBack() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Section: Categories (الفئات ومستودع مكتبة الصور)
        Text(
            text = "الفئات وقوائم القراءة",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .align(Alignment.End)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) SlateCard else SlateLightCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                MangaSettingsItem(
                    icon = Icons.Default.Edit,
                    title = "تعديل فئات القراءة المخصصة",
                    subtitle = "تحتوي مكتبتك على ٥ فئات افتراضية مضافة",
                    onClick = { Toast.makeText(context, "إعدادات الفئات مضافة ومحمية", Toast.LENGTH_SHORT).show() }
                )
                MangaSettingsItem(
                    icon = Icons.Default.Help,
                    title = "الفئة الافتراضية المبدئية",
                    subtitle = "تنزيل الفصول تلقائياً: «السؤال دائماً»",
                    onClick = { Toast.makeText(context, "الوضع النشط: السؤال دائماً عند التحميل", Toast.LENGTH_SHORT).show() }
                )

                Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                // Toggle switch: local sorting custom rules
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = sortSettingsPerCategory,
                        onCheckedChange = { viewModel.setSortSettingsPerCategory(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "إعدادات فرز كل صنف",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "مزامنة ترتيب المانجا وفلترتها المخصص على كل فئة بشكل منعزل",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: General updates
        Text(
            text = "تحديثات الخلفية العامة",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .align(Alignment.End)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) SlateCard else SlateLightCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                MangaSettingsItem(
                    icon = Icons.Default.Timer,
                    title = "تواتر التنزيلات التلقائية",
                    subtitle = "التحقق المجدول: «كل ١٢ ساعة» تلقائياً",
                    onClick = { Toast.makeText(context, "تم تحديد وقت الفحص: كل ١٢ ساعة بالخلفية", Toast.LENGTH_SHORT).show() }
                )
                MangaSettingsItem(
                    icon = Icons.Default.Wifi,
                    title = "قيود التحديث التلقائي للجهاز",
                    subtitle = "مسموح: «فقط عند الاتصال بـ شبكة Wi-Fi ومصدر شحن»",
                    onClick = { Toast.makeText(context, "تم حفظ شروط القيود الآمنة للبطارية والإنترنت", Toast.LENGTH_SHORT).show() }
                )

                Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                // Toggle: update metadata
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = autoUpdateMetadata,
                        onCheckedChange = { viewModel.setAutoUpdateMetadata(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "تحديث البيانات الوصفية تلقائياً",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "تحقق تلقائياً من وجود غلاف جديد وتفاصيل محدثة عند جلب المكتبة",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                }

                Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                // Toggle: show unread count badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = showUnreadCountBadge,
                        onCheckedChange = { viewModel.setShowUnreadCountBadge(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "مؤشر غير المقروء في القائمة السفلية",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "عرض دائرة حمراء بأعلى علامة تبويب التحديثات حال نزول فصول جديدة",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Gestures and actions (تصرف السحب)
        Text(
            text = "تصرف وإيماءات السحب للفصول",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .align(Alignment.End)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) SlateCard else SlateLightCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                MangaSettingsItem(
                    icon = Icons.Default.SwipeLeft,
                    title = "إجراء التمرير إلى اليسار للفصل",
                    subtitle = "الإجراء الحالي: «تحديد الفصل كمقروء / غير مقروء»",
                    onClick = { Toast.makeText(context, "اليسار: تحديد كمقروء", Toast.LENGTH_SHORT).show() }
                )
                MangaSettingsItem(
                    icon = Icons.Default.SwipeRight,
                    title = "إجراء التمرير إلى اليمين للفصل",
                    subtitle = "الإجراء الحالي: «تحنزيل الفصل وتخزينه هاتفياً»",
                    onClick = { Toast.makeText(context, "اليمين: بدء التنزيل", Toast.LENGTH_SHORT).show() }
                )

                Divider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                // Toggle: hide missing chapters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = hideMissingChapters,
                        onCheckedChange = { viewModel.setHideMissingChapters(it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "إخفاء مؤشرات الفصول المفقودة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "تجاهل تنبيهات الأخطاء أو قنوات الاتصال بموقع المانجا حال فقدان فصل",
                            fontSize = 11.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}



