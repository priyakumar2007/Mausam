package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.domain.LanguageManager
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamIndigo
import com.example.ui.theme.MausamPurple

@Composable
fun AssistantScreen(
  messages: List<ChatMessage>,
  isReplying: Boolean,
  language: String,
  onSendMessage: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }

  val quickPrompts = listOf(
    "Can I go for a run?",
    "Will it rain tonight?",
    "Is my crop at risk?",
    "Which beach is better today?",
    "Which route has less traffic?",
    "What should I carry for my trip?"
  )

  Column(modifier = modifier.fillMaxSize()) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MausamPurple.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = null,
          tint = MausamPurple,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "🤖 Ask MAUSAM",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Context-aware companion (Home • Field • Route • Beach)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Message History
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(messages) { msg ->
        ChatMessageBubble(message = msg)
      }

      if (isReplying) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.dp,
              color = MausamPurple
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "MAUSAM is checking current weather context...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Quick Prompts Chips
    val scrollState = rememberScrollState()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .horizontalScroll(scrollState),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      quickPrompts.forEach { prompt ->
        Surface(
          modifier = Modifier.clickable { onSendMessage(prompt) },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = prompt,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }

    // Input Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = {
          Text(
            LanguageManager.getString("ask_placeholder", language),
            fontSize = 13.sp
          )
        },
        modifier = Modifier
          .weight(1f)
          .testTag("chat_input_field"),
        shape = RoundedCornerShape(20.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (inputText.isNotBlank()) {
            onSendMessage(inputText)
            inputText = ""
          }
        },
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MausamPurple)
          .testTag("send_chat_button")
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Send",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
  val isUser = message.isUser

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    Surface(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
      ),
      color = if (isUser) MausamBlue else MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium,
          color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
          lineHeight = 20.sp
        )
      }
    }
  }
}
