package app.photowidgets

import android.net.Uri
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.app.PendingIntent
import android.widget.Toast
import android.os.Bundle
import android.view.WindowManager
import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Slider
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
import androidx.compose.runtime.mutableFloatStateOf
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
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
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
                var cropScale by remember { mutableFloatStateOf(1f) }
                var cropX by remember { mutableFloatStateOf(0f) }
                var cropY by remember { mutableFloatStateOf(0f) }
                var photoAlpha by remember { mutableFloatStateOf(1f) }
                var shapeChoice by remember { mutableIntStateOf(0) }

                val photoPicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        selectedPhoto = uri
                        try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: SecurityException) { }
                        creating = 1
                        cropScale = 1f
                        cropX = 0f
                        cropY = 0f
                        photoAlpha = 1f
                    }
                }

                fun goBack() {
                    if (creating > 1) { creating-- } else if (creating == 1) {
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
                    containerColor = if (creating == 1) Color.Transparent else AppBackground,
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
                        color = if (creating == 1) Color.Transparent else AppBackground
                    ) {
                        when {
                            creating == 1 -> PhotoEditorScreen(
                                photo = selectedPhoto,
                                scale = cropScale, onScaleChange = { cropScale = it },
                                offsetX = cropX, onOffsetXChange = { cropX = it },
                                offsetY = cropY, onOffsetYChange = { cropY = it },
                                alpha = photoAlpha, onAlphaChange = { photoAlpha = it },
                                onBack = ::goBack, onNext = { creating = 2 }, onChooseAgain = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            creating == 2 -> ShapeSelectionScreen(shapeChoice, { shapeChoice = it }, { creating = 1 }, { creating = 3 })
                            creating == 3 -> SizeSelectionScreen(onBack = { creating = 2 }, onPublish = { publishPhotoWidget(selectedPhoto) })
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


private fun ComponentActivity.publishPhotoWidget(photoUri: Uri?) {
    if (photoUri == null) {
        Toast.makeText(this, "Сначала выбери фотографию", Toast.LENGTH_SHORT).show()
        return
    }
    val manager = AppWidgetManager.getInstance(this)
    val provider = ComponentName(this, PhotoWidgetProvider::class.java)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
        val callbackIntent = Intent(this, PhotoWidgetProvider::class.java).apply {
            action = PhotoWidgetProvider.ACTION_PIN_WIDGET
            putExtra(PhotoWidgetProvider.EXTRA_PHOTO_URI, photoUri.toString())
        }
        val callback = PendingIntent.getBroadcast(this, photoUri.hashCode(), callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        if (!manager.requestPinAppWidget(provider, null, callback)) {
            Toast.makeText(this, "Этот лаунчер не поддерживает добавление виджетов из приложения", Toast.LENGTH_LONG).show()
        }
    } else {
        Toast.makeText(this, "Добавление из приложения не поддерживается этим лаунчером", Toast.LENGTH_LONG).show()
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
private fun PhotoEditorScreen(
    photo: Uri?,
    scale: Float, onScaleChange: (Float) -> Unit,
    offsetX: Float, onOffsetXChange: (Float) -> Unit,
    offsetY: Float, onOffsetYChange: (Float) -> Unit,
    alpha: Float, onAlphaChange: (Float) -> Unit,
    onBack: () -> Unit, onNext: () -> Unit, onChooseAgain: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹  Назад", color = MutedText, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Редактирование фото", color = MainText, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Text("Перемещай фото одним пальцем и меняй масштаб двумя пальцами.", color = MutedText, fontSize = 13.sp)
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .background(CardBackground, RoundedCornerShape(22.dp))
        ) {
            // Transparent preview window reveals the live system wallpaper behind this area.
            AsyncImage(
                model = photo, contentDescription = "Редактируемая фотография",
                modifier = Modifier.fillMaxSize()
                    .pointerInput(scale, offsetX, offsetY) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            onScaleChange((scale * zoom).coerceIn(1f, 5f))
                            onOffsetXChange(offsetX + pan.x)
                            onOffsetYChange(offsetY + pan.y)
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale; scaleY = scale
                        translationX = offsetX; translationY = offsetY
                        this.alpha = alpha
                    },
                contentScale = ContentScale.Crop
            )
        }
        Text("Прозрачность", color = MainText, fontSize = 14.sp)
        Slider(value = alpha, onValueChange = onAlphaChange, valueRange = 0.1f..1f)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { onScaleChange(1f); onOffsetXChange(0f); onOffsetYChange(0f); onAlphaChange(1f) },
                modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = CardBackground)) {
                Text("Сбросить")
            }
            Button(onClick = onChooseAgain, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)) {
                Text("Другое фото")
            }
        }
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentSurface)) {
            Text("Далее")
        }
    }
}

@Composable
private fun ShapeSelectionScreen(
    selected: Int, onSelect: (Int) -> Unit, onBack: () -> Unit, onNext: () -> Unit
) {
    val names = listOf("Квадрат", "Прямоугольник", "Капсула")
    Column(Modifier.fillMaxSize().background(AppBackground).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("‹  Назад", color = MutedText, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Выбор формы", color = MainText, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        names.forEachIndexed { index, name ->
            Column(
                Modifier.fillMaxWidth().background(if (selected == index) AccentSurface else CardBackground,
                    RoundedCornerShape(20.dp)).clickable { onSelect(index) }.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(when(index) { 0 -> "▢"; 1 -> "▭"; else -> "⬭" }, color = MainText, fontSize = 42.sp)
                Text(name, color = MainText)
                if (selected == index) Text("Выбрано", color = MutedText, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.fillMaxWidth().height(76.dp)
                .background(AccentSurface, RoundedCornerShape(18.dp))
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Далее",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SizeSelectionScreen(onBack: () -> Unit, onPublish: () -> Unit) {
    var size by remember { mutableFloatStateOf(2f) }
    Column(Modifier.fillMaxSize().background(AppBackground).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("‹  Назад", color = MutedText, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 6.dp))
        Text("Размер виджета", color = MainText, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Slider(value = size, onValueChange = { size = it }, valueRange = 1f..4f, steps = 2, modifier = Modifier.weight(0.7f))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(Modifier.size((65 + size * 22).dp), shape = RoundedCornerShape(18.dp), color = AccentSurface) {}
                Text("${size.toInt()} × ${size.toInt()}", color = MainText, fontSize = 18.sp)
                Text("Предпросмотр", color = MutedText, fontSize = 12.sp)
            }
        }
        Text("Android попросит подтвердить размещение виджета на рабочем столе.", color = MutedText, fontSize = 13.sp)
        Button(onClick = onPublish, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentSurface, contentColor = Color.White)) {
            Text("Добавить на рабочий стол", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}
