package com.example

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.example.ui.StudyTaskApp
import com.example.util.TaskNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setBackgroundDrawable(ColorDrawable(AndroidColor.parseColor("#F8FAFC")))
    enableEdgeToEdge()
    
    lifecycleScope.launch(Dispatchers.Default) {
      try {
        TaskNotificationHelper.scheduleTwiceDailyAlarms(applicationContext)
      } catch (e: Throwable) {
        e.printStackTrace()
      }
    }

    setContent {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
      ) {
        StudyTaskApp(modifier = Modifier.fillMaxSize())
      }
    }
  }
}

