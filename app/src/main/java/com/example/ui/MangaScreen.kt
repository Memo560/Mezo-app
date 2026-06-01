package com.example.ui

import android.content.Intent
import android.widget.Toast
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
                          currentScreen is MangaUiScreen.Profile,
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

// --- Custom Bottom Navigation Bar with Profile Tab ---
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
        val isLibSelected = currentScreen is MangaUiScreen.Library
        NavigationBarItem(
            selected = isLibSelected,
            onClick = { onNavigate(MangaUiScreen.Library) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
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
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )

        val isSourcesSelected = currentScreen is MangaUiScreen.Sources
        NavigationBarItem(
            selected = isSourcesSelected,
            onClick = { onNavigate(MangaUiScreen.Sources) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "تصفح",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "تصفح",
                    fontSize = 11.sp,
                    fontWeight = if (isSourcesSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )

        val isHistorySelected = currentScreen is MangaUiScreen.History
        NavigationBarItem(
            selected = isHistorySelected,
            onClick = { onNavigate(MangaUiScreen.History) },
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
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
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )

        val isProfileSelected = currentScreen is MangaUiScreen.Profile
        NavigationBarItem(
            selected = isProfileSelected,
            onClick = { onNavigate(MangaUiScreen.Profile) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "حسابي",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "حسابي",
                    fontSize = 11.sp,
                    fontWeight = if (isProfileSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = textSecondaryColor,
                unselectedTextColor = textSecondaryColor
            )
        )
    }
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

    var activeSegment by remember { mutableStateOf("sources") } // "sources" or "keiyoushi"

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val cardBackground = if (isDarkMode) SlateCard else SlateLightCard
    val inputBackground = if (isDarkMode) SlateCard else Color(0xFFF1F5F9)

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
                    .weight(1f)
                    .height(38.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("المصادر المتوفرة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("مستودع Keiyoushi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                text = "اكتشف آلاف الفصول والقصص المانجا المترجمة من مصادرك النشطة",
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
                val sources = listOf("الكل", "مانجا ليك", "مانجا ديكس", "MangaSlayer")
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
                        text = "🔍 لا نتائج متطابقة أو المصادر معطلة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "فَعِّل إضافات Keiyoushi للتأكد من المزامنة وجلب المحتوى.",
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
            // KEIYOUSHI EXTENSION MANAGER INDEX VIEW
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مستودع إضافات Keiyoushi",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "قم بتثبيت وتنزيل وتحديث حزم مصادر المانجا مباشرة من مستودع مجتمع ميهون الرسمي",
                    fontSize = 12.sp,
                    color = textSecColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(extensions) { ext ->
                        ExtensionRowCard(
                            ext = ext,
                            isDarkMode = isDarkMode,
                            onInstall = { viewModel.installExtension(ext.id) },
                            onUninstall = { viewModel.uninstallExtension(ext.id) },
                            onToggle = { viewModel.toggleExtension(ext.id) }
                        )
                    }
                }
            }
        }
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
fun MangaProfileView(viewModel: MangaViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val libraryMangas by viewModel.libraryMangas.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState()

    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val cardBg = if (isDarkMode) SlateCard else SlateLightCard

    var isRegisterState by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isEditingProfile by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf("avatar_1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "الملف الشخصي",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "سجل تقدمك وقوائمك الشخصية وسجل دخولك لمزامنتها",
            fontSize = 12.sp,
            color = textSecColor
        )

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
            // PROFILE DETAILS CARD FOR REGISTERED USERS
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
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = PriorityHigh, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${libraryMangas.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                        Text(text = "سلسلة بالمكتبة", fontSize = 11.sp, color = textSecColor)
                    }
                }

                // History reads card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${readingHistory.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                        Text(text = "سلاسل مقروءة", fontSize = 11.sp, color = textSecColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // USER APP THEME AND SETTINGS SET
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إعدادات التطبيق", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                            Text("الوضع الليلـي", color = textColor, fontSize = 13.sp)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() }
                        )
                    }

                    Divider(color = textSecColor.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.logoutUser() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = PriorityHigh, modifier = Modifier.size(18.dp))
                            Text("تسجيل الخروج", color = PriorityHigh, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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

    var activePage by remember(currentChapter.id) { mutableStateOf(currentChapter.lastReadPage) }
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
            val scrollState = rememberLazyListState()

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
            val pagerState = rememberPagerState(initialPage = activePage) { totalPages }

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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(gradientStart), Color(gradientEnd))
                )
            )
    ) {
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawLine(
                color = Color.White.copy(alpha = 0.12f),
                start = Offset(0f, h * 0.45f),
                end = Offset(w, h * 0.48f),
                strokeWidth = 3f
            )

            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(w * 0.5f, 0f),
                end = Offset(w * 0.48f, h * 0.45f),
                strokeWidth = 3f
            )
            
            for (i in 0..12) {
                val startX = (w * 0.05f) + (i * w * 0.08f)
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(startX, h * 0.6f),
                    end = Offset(startX + (w * 0.1f), h * 0.95f),
                    strokeWidth = 1.5f
                )
            }
        }

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
