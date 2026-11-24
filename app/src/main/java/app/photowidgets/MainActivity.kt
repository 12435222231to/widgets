package app.photowidgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("PhotoWidgets", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            "Добавляй фотографии прямо на главный экран телефона. " +
                                "Чтобы создать виджет, зажми пустое место на рабочем столе, " +
                                "открой раздел «Виджеты» и выбери PhotoWidgets."
                        )
                        Text(
                            "После добавления выбери фотографию. Виджет можно менять по размеру " +
                                "и создавать несколько виджетов с разными снимками."
                        )
                    }
                }
            }
        }
    }
}
