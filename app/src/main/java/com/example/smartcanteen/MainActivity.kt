package com.example.smartcanteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcanteen.ui.theme.SmartCanteenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CanteenViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            
            SmartCanteenTheme(themeMode = themeMode) {
                SmartCanteenApp(viewModel = viewModel)
            }
        }
    }
}
