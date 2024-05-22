package com.next.sync.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.next.sync.ui.theme.AppTheme

@Composable
fun HomeScreen(paddingModifier: Modifier) {
    LazyColumn(
        modifier = Modifier
            .padding(5.dp)
    ){
        item {
            MainCard()
        }

        item {
            Card {

            }
        }
    }
}

@Composable
fun MainCard()
{
    Card(modifier = Modifier.fillMaxWidth()){
        Column(modifier = Modifier.padding(8.dp)) {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.aspectRatio(1f).weight(1f)
                ){
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        progress = 66f,
                        modifier = Modifier.fillMaxSize())
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "All time stats")
                    Text(text = "Upload")
                    Text(text = "1.1GB")
                    Text(text = "Download")
                    Text(text = "270MB")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                    Text(text = "Synchronize now")
                }
                Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                    Text(text = "Add task")
                }
            }
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    AppTheme(false) {
        HomeScreen(Modifier.padding(0.dp))
    }
}