package com.etrak.scaleusb.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val customers by viewModel.customers.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = viewModel.cabAngle.toString(),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            items(items = customers) {
                Text(
                    text = it.name,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}