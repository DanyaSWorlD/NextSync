package com.next.sync.ui.options

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.next.sync.BuildConfig
import com.next.sync.ui.components.review.ReviewHandler
import com.next.sync.ui.theme.AppTheme

@Composable
fun OptionsScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Item(icon = Icons.Rounded.Settings, text = "Settings") }
        item { Item(icon = Icons.AutoMirrored.Rounded.HelpOutline, text = "Support") }
        item {
            Item(
                icon = Icons.Rounded.StarOutline,
                text = "Rate app",
                onClick = {
                    if (activity != null) {
                        ReviewHandler.launchInAppReview(activity)
                    } else {
                        ReviewHandler.openPlayStoreForRating(context)
                    }
                }
            )
        }
        item {
            Item(
                icon = Icons.Rounded.Code,
                text = "Source code",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/DanyaSWorlD/NextSync".toUri()
                    )
                    context.startActivity(intent)
                }
            )
        }
        item {
            Item(
                text = "Confidential policy",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://docs.google.com/document/d/e/2PACX-1vQVMNU_TWAzAQCK64rhNRBmWfUaFQ5OaOoq3PPrU9AEPsZDbVMamEKfsEqtD0IJOsrLQHqwjnqi4EY_/pub".toUri()
                    )
                    context.startActivity(intent)
                })
        }
        item { Footer() }
    }
}

@Composable
fun Item(icon: ImageVector, text: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Icon(
            painter = rememberVectorPainter(icon),
            contentDescription = null, // decorative element
            modifier = Modifier.padding(24.dp)
        )

        Box(
            modifier = Modifier.height(72.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = text, textAlign = TextAlign.Start)
        }
    }
}

@Composable
fun Item(text: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Spacer(modifier = Modifier.width(72.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = text, textAlign = TextAlign.Start)
        }
    }
}

@Composable
fun Footer() {
    Spacer(modifier = Modifier.height(24.dp))
    Row {
        Spacer(modifier = Modifier.width(72.dp))
        Column {
            Text(text = "Next Sync", color = MaterialTheme.colorScheme.outline)
            Text(
                text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
@Preview
fun DashboardScreenPreview() {
    AppTheme(false) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            OptionsScreen()
        }
    }
}

@Composable
@Preview
fun DashboardScreenPreviewDark() {
    AppTheme(true) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            OptionsScreen()
        }
    }
}