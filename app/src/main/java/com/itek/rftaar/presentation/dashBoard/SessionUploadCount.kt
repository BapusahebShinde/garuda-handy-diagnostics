package com.itek.rftaar.presentation.dashBoard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.data.model.EanFoundQty
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.ui.theme.BackGround

@Composable
fun SessionUploadCount(modifier: Modifier.Companion, navController: NavHostController) {

    BackHandler(enabled = true) {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
    }
    Scaffold(
        bottomBar = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(BackGround),
                contentAlignment = Alignment.BottomCenter
            ) { AboutAppBottomBar() }
        },
        modifier = modifier.background(BackGround)
    ) { innerPadding ->
        Box(
            modifier = modifier
                .background(BackGround)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NotificationScreenContent(navController)
        }
    }
}

@Composable
fun NotificationScreenContent(navController: NavHostController) {
    val context = LocalContext.current
    val db = AppDatabase.getDbInstance(context)

    val data = db.tagInfoDao().getSessionWiseUploadedCounts()
        .collectAsState(initial = emptyList()).value

    Scaffold(
        topBar = {
            TopBarContent(
                "Session Upload Count(s)",
                onBackClickL = {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                }, onSettingClick = {
                },
                isSetting = false
            )
        }
    ) { padding ->

        if (data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No Data Available")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(data) { item ->
                    NotificationItem(item)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(item: EanFoundQty) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = item.barcode.replace(";","\n").trim(),
                style = MaterialTheme.typography.body2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Found: ${item.foundQty}")
                Text("Uploaded: ${item.totalQty}")
            }
        }
    }
}