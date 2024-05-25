package com.next.sync.ui.options

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.next.sync.ui.theme.AppTheme

@Composable
fun DashboardScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(text="options", fontSize = 100.sp)
    }
}

@Composable
@Preview
fun DashboardScreenPreview()
{
    AppTheme(false) {
        DashboardScreen()
    }
}