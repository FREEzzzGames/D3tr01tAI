package com.d3tr01t.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val Bg = Color(0xFF090B10)
private val Panel = Color(0xFF151A22)
private val Cyan = Color(0xFF66FCF1)
private val Muted = Color(0xFF87919F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { D3tr01tApp(this, isOnline = { hasNetwork(this) }) }
    }
}

private fun hasNetwork(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

@Composable
private fun D3tr01tApp(context: Context, isOnline: () -> Boolean) {
    var online by remember { mutableStateOf(isOnline()) }
    var prompt by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var modelPath by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("Выберите GGUF-модель для запуска локального ядра.") }
    var messages by remember { mutableStateOf(listOf("d3tr01t AI: локальный режим готов. Импортируйте GGUF-модель.")) }
    val scope = rememberCoroutineScope()
    val engine = remember { AiChat.getInferenceEngine(context) }
    val engineState by engine.state.collectAsState()

    val modelPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            busy = true
            status = "Копирование модели во внутреннее хранилище…"
            scope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        val dir = File(context.filesDir, "models").apply { mkdirs() }
                        val target = File(dir, "active-model.gguf")
                        context.contentResolver.openInputStream(uri).use { input ->
                            requireNotNull(input) { "Не удалось открыть выбранный файл" }
                            target.outputStream().use { output -> input.copyTo(output) }
                        }
                        target
                    }
                    modelPath = file.absolutePath
                    status = "Модель скопирована. Загрузка в локальный движок…"
                    withContext(Dispatchers.IO) {
                        engine.loadModel(file.absolutePath)
                        engine.setSystemPrompt(
                            "Ты d3tr01t AI — персональный локальный помощник. Отвечай по-русски, точно и по делу."
                        )
                    }
                    status = "Модель загружена. Диалог работает локально, без интернета."
                    messages = messages + "Система: модель загружена и готова к работе."
                } catch (e: Exception) {
                    status = "Ошибка загрузки: ${e.message ?: "неизвестная ошибка"}"
                } finally {
                    busy = false
                }
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Cyan,
        onBackground = Color.White, onSurface = Color.White
    )) {
        LaunchedEffect(Unit) {
            while (true) {
                online = isOnline()
                kotlinx.coroutines.delay(2500)
            }
        }
        Column(
            Modifier.fillMaxSize().background(Bg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("d3tr01t AI", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("OFFLINE-FIRST PERSONAL AI", fontSize = 10.sp, color = Muted, letterSpacing = 1.2.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (online) "● ONLINE" else "● OFFLINE", color = if (online) Cyan else Color(0xFFFFB703), fontSize = 11.sp)
                    Text("LOCAL ENGINE", color = Muted, fontSize = 9.sp)
                }
            }

            Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("ЛОКАЛЬНАЯ МОДЕЛЬ", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(modelPath?.substringAfterLast("/") ?: "Модель не выбрана", color = Color.White, fontSize = 13.sp)
                    Text(status, color = Muted, fontSize = 11.sp)
                    Button(
                        onClick = { modelPicker.launch(arrayOf("*/*")) },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Bg)
                    ) { Text("ВЫБРАТЬ GGUF-МОДЕЛЬ", fontWeight = FontWeight.Bold) }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ДИАЛОГ", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    when (engineState) {
                        is InferenceEngine.State.ModelReady -> "MODEL READY"
                        is InferenceEngine.State.Generating -> "GENERATING"
                        is InferenceEngine.State.Error -> "ENGINE ERROR"
                        else -> "ENGINE INITIALIZING"
                    },
                    color = Cyan, fontSize = 10.sp
                )
            }

            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(messages) { message -> Text(message, color = Color.White, fontSize = 14.sp) }
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Сообщение…", color = Muted) },
                shape = RoundedCornerShape(14.dp),
                enabled = !busy && engineState is InferenceEngine.State.ModelReady,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan, unfocusedBorderColor = Muted,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )
            Button(
                onClick = {
                    val question = prompt.trim()
                    if (question.isNotEmpty() && engineState is InferenceEngine.State.ModelReady) {
                        prompt = ""
                        messages = messages + "Вы: $question" + "d3tr01t AI: "
                        busy = true
                        scope.launch {
                            try {
                                val answer = StringBuilder()
                                engine.sendUserPrompt(question, 384).collect { token ->
                                    answer.append(token)
                                    messages = messages.dropLast(1) + "d3tr01t AI: $answer"
                                }
                            } catch (e: Exception) {
                                messages = messages + "Ошибка генерации: ${e.message}"
                            } finally {
                                busy = false
                            }
                        }
                    }
                },
                enabled = !busy && engineState is InferenceEngine.State.ModelReady,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Bg)
            ) { Text(if (busy) "ОБРАБОТКА…" else "ОТПРАВИТЬ", fontWeight = FontWeight.Bold) }
        }
    }
}
