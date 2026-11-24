package app.photowidgets

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val AppBackground = Color(0xFF111216)
private val CardBackground = Color(0xFF202228)
private val AccentSurface = Color(0xFF343840)
private val MainText = Color(0xFFF4F4F5)
private val MutedText = Color(0xFFA5A8B0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = AppBackground,
                    surface = CardBackground,
                    primary = MainText,
                    onPrimary = MainText,
                    onBackground = MainText,
                    onSurface = MainText
                )
            ) {
                var selectedTab by remember { mutableIntStateOf(0) }
                var creating by remember { mutableIntStateOf(0) }
                var selectedPhoto by remember { mutableStateOf<Uri?>(null) }

                val photoPicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        selectedPhoto = uri
                        creating = 1
                    }
                }

                fun goBack() {
                    if (creating > 0) {
                        creating = 0
                        selectedPhoto = null
                    } else {
                        selectedTab = 0
                    }
                }

                BackHandler(enabled = creating > 0 || selectedTab != 0) {
                    goBack()
                }

                Scaffold(
                    containerColor = AppBackground,
                    bottomBar = {
                        NavigationBar(containerColor = Color(0xFF181A1F)) {
                            NavigationBarItem(
                                selected = selectedTab == 0 && creating == 0,
                                onClick = { selectedTab = 0; creating = 0 },
                                icon = { Text("⌂", fontSize = 21.sp) },
                                label = { Text("Главная") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1 && creating == 0,
                                onClick = { selectedTab = 1; creating = 0 },
                                icon = { Text("▦", fontSize = 19.sp) },
                                label = { Text("Мои виджеты") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = AppBackground
                    ) {
                        when {
                            creating > 0 -> PhotoSelectionScreen(
                                photo = selectedPhoto,
                                onBack = ::goBack,
                                onChooseAgain = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            selectedTab == 0 -> HomeScreen(
                                onCreate = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            else -> MyWidgetsScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 22.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Text(
            text = "Виджеты",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = MainText
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(24.dp))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("ТВОИ ФОТОВИДЖЕТЫ", color = MutedText, fontSize = 11.sp, letterSpacing = 1.4.sp)
            Text("Любимые фотографии\nна рабочем столе", color = MainText, fontSize = 23.sp, fontWeight = FontWeight.Medium)
            Text("Создай виджет из своей фотографии", color = MutedText, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Button(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentSurface,
                contentColor = MainText
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("+", fontSize = 23.sp, fontWeight = FontWeight.Light)
                Text("Создать виджет", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MyWidgetsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Мои виджеты", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = MainText)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(22.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Здесь пока пусто", color = MainText, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            Text("Созданные виджеты появятся здесь", color = MutedText, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PhotoSelectionScreen(
    photo: Uri?,
    onBack: () -> Unit,
    onChooseAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "‹  Назад",
            color = MutedText,
            fontSize = 16.sp,
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 8.dp)
        )
        Text(
            text = "Выбор фотографии",
            color = MainText,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = "Выбранная фотография",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )
            Button(
                onClick = onChooseAgain,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentSurface)
            ) {
                Text("Выбрать другую фотографию")
            }
            Text(
                "Следующий этап — настройка формы и кадрирования фотографии.",
                color = MutedText,
                fontSize = 13.sp
            )
        } else {
            Text("Выбери фотографию из галереи телефона.", color = MutedText, fontSize = 14.sp)
        }
    }
}
