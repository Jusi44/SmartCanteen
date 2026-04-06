package com.example.smartcanteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcanteen.data.FirestoreCanteenRepository
import com.example.smartcanteen.ui.theme.SmartCanteenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Using OOP: Injecting the repository into the ViewModel via a Factory
            val repository = FirestoreCanteenRepository()
            val viewModel: CanteenViewModel = viewModel(
                factory = CanteenViewModel.Factory(application, repository)
            )

            val themeMode by viewModel.themeMode.collectAsState()
            
            SmartCanteenTheme(themeMode = themeMode) {
                SmartCanteenApp(viewModel = viewModel)
            }
        }
    }
}
