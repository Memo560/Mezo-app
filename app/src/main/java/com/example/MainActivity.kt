package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MangaScreen
import com.example.ui.MangaViewModel
import com.example.ui.MangaViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      // Retrieve custom Manga repository instance from custom application class
      val repository = (application as MangaApplication).repository
      val viewModel: MangaViewModel = viewModel(
          factory = MangaViewModelFactory(repository)
      )

      val isDarkMode by viewModel.isDarkMode.collectAsState()

      MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
        MangaScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
