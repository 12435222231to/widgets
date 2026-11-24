package app.photowidgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

                Scaffold(
                    containerColor = AppBackground,
                    bottomBar = {
                        NavigationBar(containerColor = Color(0xFF181A1F)) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0; creating = 0 },
                                icon = { Text("⌂", fontSize = 21.sp) },
                                label = { Text("Главная") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
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
                            creating > 0 -> CreatePlaceholder(
                                step = creating,
                                onBack = { if (creating > 1) creating-- else creating = 0 }
                            )
                            selectedTab == 0 -> HomeScreen(onCreate = { creating = 1 })
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
private fun CreatePlaceholder(step: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("‹  Назад", color = MutedText, fontSize = 16.sp, modifier = Modifier.padding(vertical = 6.dp))
        Text(
            text = when (step) {
                1 -> "Выбор фотографии"
                2 -> "Выбор формы"
                else -> "Размер виджета"
            },
            color = MainText,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Экран подготовки. Следующим этапом подключим выбор фото и редактор.",
            color = MutedText,
            fontSize = 14.sp
        )
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = AccentSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Вернуться")
        }
    }
}
