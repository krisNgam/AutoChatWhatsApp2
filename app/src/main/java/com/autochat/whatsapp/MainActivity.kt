package com.autochat.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AutoChatApp() } }
    }
}

@Composable
fun AutoChatApp() {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    fun openWhatsApp(text: String) {
        val number = phone.filter(Char::isDigit)
        if (number.isBlank()) { status = "Entre le numéro au format international."; return }
        val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number?text=$encoded")))
    }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("AutoChat WhatsApp", style = MaterialTheme.typography.headlineMedium); Text("Prototype gratuit : messages, programmation et assistant de réponse") }
        item { OutlinedTextField(phone, { phone = it }, label = { Text("Numéro WhatsApp international") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(message, { message = it }, label = { Text("Message / message reçu") }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    reply = when {
                        message.contains("bonjour", true) || message.contains("salut", true) -> "Salut ! J’espère que tu vas bien 😊"
                        message.contains("ça va", true) || message.contains("ca va", true) -> "Oui, ça va bien merci 😊 Et toi ?"
                        message.isBlank() -> ""
                        else -> "Merci pour ton message. Je te réponds dès que possible."
                    }
                }) { Text("Proposer une réponse") }
                OutlinedButton(onClick = { if (message.isNotBlank()) openWhatsApp(message) }) { Text("WhatsApp") }
            }
        }
        item {
            if (reply.isNotBlank()) Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) {
                Text("Réponse proposée", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(6.dp)); Text(reply); Spacer(Modifier.height(8.dp))
                Button(onClick = { openWhatsApp(reply) }) { Text("Utiliser cette réponse") }
            }}
        }
        item { HorizontalDivider(); Text("Programmer un message", style = MaterialTheme.typography.titleLarge) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(date, { date = it }, label = { Text("JJ/MM/AAAA") }, modifier = Modifier.weight(1f))
            OutlinedTextField(time, { time = it }, label = { Text("HH:MM") }, modifier = Modifier.weight(1f))
        }}
        item { Button(onClick = {
            if (phone.isBlank() || message.isBlank()) { status = "Numéro et message requis."; return@Button }
            val millis = runCatching { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("$date $time")!!.time }.getOrNull()
            if (millis == null || millis <= System.currentTimeMillis()) { status = "Date/heure invalide ou déjà passée."; return@Button }
            schedule(context, phone, message, millis)
            status = "Programmation enregistrée. À l’heure prévue, Android déclenchera le rappel." 
        }) { Text("Programmer le message") } }
        item { if (status.isNotBlank()) Text(status) }
        item { Text("À propos", style = MaterialTheme.typography.titleLarge); Text("Cette première version prépare les messages et ouvre WhatsApp avec le texte prérempli. Elle n’envoie pas silencieusement des messages dans WhatsApp. L’assistant IA réel et l’apprentissage de ton style seront ajoutés dans l’étape suivante.", style = MaterialTheme.typography.bodySmall) }
    }
}

private fun schedule(context: Context, phone: String, message: String, at: Long) {
    val data = workDataOf("phone" to phone, "message" to message)
    val request = OneTimeWorkRequestBuilder<MessageReminderWorker>()
        .setInitialDelay((at - System.currentTimeMillis()).coerceAtLeast(0), java.util.concurrent.TimeUnit.MILLISECONDS)
        .setInputData(data).build()
    WorkManager.getInstance(context).enqueue(request)
}

class MessageReminderWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val phone = inputData.getString("phone") ?: return Result.failure()
        val message = inputData.getString("message") ?: return Result.failure()
        val number = phone.filter(Char::isDigit)
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number?text=$encoded")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(intent)
        return Result.success()
    }
}
