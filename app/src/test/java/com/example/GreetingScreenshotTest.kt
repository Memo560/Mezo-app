package com.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.ChapterEntity
import com.example.data.MangaEntity
import com.example.ui.ChapterRowWithDownload
import com.example.ui.MangaGridCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockManga = MangaEntity(
        id = "one_piece",
        titleAr = "ون بيس",
        titleEn = "One Piece",
        author = "Eiichiro Oda",
        descriptionAr = "المغامرة الكبرى للحصول على الكنز الأسطوري الون بيس.",
        coverGradientStart = 0xFF0EA5E9,
        coverGradientEnd = 0xFF2563EB,
        status = "مستمر",
        rating = 4.8f,
        genres = "مغامرة, أكشن",
        sourceName = "MangaDex"
    )

    val mockChapter = ChapterEntity(
        id = "one_piece_ch_1",
        mangaId = "one_piece",
        title = "الفصل 1: بداية فجر المغامرة اللانهائية",
        number = 1.0,
        releaseDate = "2026/05/12",
        isRead = false,
        lastReadPage = 2,
        totalPages = 10
    )

    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          MangaGridCard(
              manga = mockManga,
              isDarkMode = true,
              onClick = {}
          )
          ChapterRowWithDownload(
              chapter = mockChapter,
              isDarkMode = true,
              isDownloaded = false,
              downloadProgress = null,
              onRead = {},
              onDownload = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
