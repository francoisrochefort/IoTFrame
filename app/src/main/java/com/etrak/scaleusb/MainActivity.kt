package com.etrak.scaleusb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.etrak.scaleusb.ScaleApp.Companion.appModule
import com.etrak.scaleusb.ui.main.MainScreen
import com.etrak.scaleusb.ui.main.MainViewModel
import com.etrak.scaleusb.ui.theme.ScaleUsbTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {

    // https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(
            appModule.customerRepository,
            appModule.scale,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScaleUsbTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(mainViewModel)
                }
            }
        }
    }
}
