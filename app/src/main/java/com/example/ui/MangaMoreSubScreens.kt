package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateLightCard
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextSecondaryLight

@Composable
fun MangaDownloadsQueueView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MangaBackHeader(
            title = "طابور التنزيلات",
            onBack = { viewModel.navigateBack() }
        )
        Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(64.dp), tint = textSecColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("طابور التنزيلات فارغ حالياً", fontSize = 16.sp, color = textColor)
            }
        }
    }
}

@Composable
fun MangaCategoriesView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val readingLists by viewModel.readingLists.collectAsState()
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MangaBackHeader(
            title = "الفئات والمجموعات",
            onBack = { viewModel.navigateBack() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        readingLists.forEach { listName ->
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) SlateCard else SlateLightCard),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    Text(text = listName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MangaStatsView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MangaBackHeader(title = "إحصائيات القراءة", onBack = { viewModel.navigateBack() })
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) SlateCard else SlateLightCard),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                Text(text = "مجموع وقت القراءة", fontSize = 14.sp, color = textSecColor)
                Text(text = "340 دقيقة", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
fun MangaBackupView(viewModel: MangaViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val availableBackups by viewModel.availableBackups.collectAsState()
    val textColor = if (isDarkMode) Color.White else TextPrimaryLight
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondaryLight
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshAvailableBackups(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MangaBackHeader(
            title = "البيانات والنسخ الاحتياطي",
            onBack = { viewModel.navigateBack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Create backup button
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) SlateCard else SlateLightCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "النسخ الاحتياطي المحلي",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "إنشاء نسخة احتياطية من ملفاتك (المانجا، السجلات، الإعدادات).",
                    fontSize = 13.sp,
                    color = textSecColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        Toast.makeText(context, "جاري إنشاء النسخة الاحتياطية...", Toast.LENGTH_SHORT).show()
                        viewModel.createBackup(context) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("إنشاء نسخة احتياطية", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "النسخ الاحتياطية المتوفرة",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Right
        )

        if (availableBackups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد نسخ احتياطية مسجلة بعد",
                    color = textSecColor,
                    fontSize = 14.sp
                )
            }
        } else {
            availableBackups.forEach { backupFile ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) SlateCard else SlateLightCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Restore button
                        IconButton(onClick = {
                            Toast.makeText(context, "جاري استعادة النسخة الاحتياطية...", Toast.LENGTH_SHORT).show()
                            viewModel.restoreBackupFromFile(context, backupFile) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "استعادة",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = backupFile.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "الحجم: ${backupFile.length() / 1024} KB",
                                fontSize = 12.sp,
                                color = textSecColor
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "ملف",
                            tint = textSecColor
                        )
                    }
                }
            }
        }
    }
}
