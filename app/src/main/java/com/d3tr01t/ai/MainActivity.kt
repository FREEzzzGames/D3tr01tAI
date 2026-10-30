package com.d3tr01t.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF090B10)
private val Panel = Color(0xFF151A22)
private val Cyan = Color(0xFF66FCF1)
private val Muted = Color(0xFF87919F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { D3tr01tApp(isOnline = { hasNetwork(this) }) }
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
private fun D3tr01tApp(isOnline: () -> Boolean) {
    var online by remember { mutableStateOf(isOnline()) }
    var mode by remember { mutableStateOf("LOCAL") }
    var prompt by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf("d3tr01t AI готов к запуску.")) }

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
            Modifier.fillMaxSize().background(Bg).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("d3tr01t AI", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("PERSONAL INTELLIGENCE SYSTEM", fontSize = 10.sp, color = Muted, letterSpacing = 1.5.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (online) "● ONLINE" else "● OFFLINE", color = if (online) Cyan else Color(0xFFFFB703), fontSize = 11.sp)
                    Text("ANDROID CORE", color = Muted, fontSize = 9.sp)
                }
            }

            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(5.dp)) {
                listOf("LOCAL", "CLOUD").forEach { item ->
                    val enabled = item == "LOCAL" || online
                    Button(
                        onClick = { if (enabled) mode = item },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == item) Cyan else Panel,
                            contentColor = if (mode == item) Bg else Color.White,
                            disabledContainerColor = Panel,
                            disabledContentColor = Muted
                        )
                    ) { Text(item, fontWeight = FontWeight.Bold) }
                }
            }

            Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (mode == "LOCAL") "ЛОКАЛЬНОЕ ЯДРО" else "ОБЛАЧНОЕ ЯДРО", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (mode == "LOCAL") "Локальный движок ещё не подключён. Следующий этап — интеграция llama.cpp и установка GGUF-модели."
                        else "Подключение облачного провайдера будет добавлено через защищённый backend. API-ключи не хранятся в приложении.",
                        color = Color.White, fontSize = 13.sp
                    )
                    Text(if (mode == "LOCAL") "STATUS: RUNTIME NOT INSTALLED" else "STATUS: PROVIDER NOT CONFIGURED", color = Muted, fontSize = 10.sp)
                }
            }

            Text("ДИАЛОГ", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages.size) { index ->
                    Text(messages[index], color = Color.White, fontSize = 14.sp)
                }
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Сообщение...", color = Muted) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan, unfocusedBorderColor = Muted,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )
            Button(
                onClick = {
                    if (prompt.isNotBlank()) {
                        messages = messages + "Вы: $prompt" + "Система: выбран режим $mode, но AI runtime ещё не подключён."
                        prompt = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Bg)
            ) { Text("ОТПРАВИТЬ", fontWeight = FontWeight.Bold) }
        }
    }
}
