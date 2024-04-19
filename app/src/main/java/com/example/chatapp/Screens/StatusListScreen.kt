package com.example.chatapp.Screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatapp.CommonDivider
import com.example.chatapp.CommonProgressBar
import com.example.chatapp.CommonRow
import com.example.chatapp.DestinationScreen
import com.example.chatapp.LCViewModel
import com.example.chatapp.navigateTo

@Composable
fun StatusListScreen(navController: NavController, vm: LCViewModel) {
    val inProcess = vm.inProcess.value
    if (inProcess) {
        CommonProgressBar()
    } else {
        val statuses = vm.status.value
        val userData = vm.userDate.value
        val myStatus = statuses.filter {
            it.user.userId == userData?.userId
        }
        val otherStatus = statuses.filter {
            it.user.userId != userData?.userId
        }
        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            uri ->
            uri?.let {
                vm.uploadSatatus(uri)
            }
        }



        Scaffold(
            floatingActionButton = {
                FAB {
                    launcher.launch("image/*")
                }

            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it).padding(
                            start = 10.dp,
                            top =  10.dp
                        )
                ) {
                    Text(
                        text = "Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    if (statuses.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Status available")
                        }
                    } else {
                        if (myStatus.isNotEmpty()) {
                            CommonRow(
                                imageUrl = myStatus[0].user.imageUrl,
                                name = myStatus[0].user.name
                            ) {
                                navigateTo(
                                    navController = navController,
                                    DestinationScreen.DetailStatus.createRoute(myStatus[0].user.userId!!)
                                )
                            }
                            CommonDivider()
                            val uniqueUser = otherStatus.map {
                                it.user
                            }.toSet().toList()
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(uniqueUser) { user ->
                                    CommonRow(imageUrl = user.imageUrl, name = user.name) {
                                        navigateTo(
                                            navController = navController,
                                            DestinationScreen.DetailStatus.createRoute(user.userId!!)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    BottomNavMenu(selectedItem = BottomNavItem.STATUSLIST, navController = navController)
                }
            }
        )

    }

}

@Composable
fun FAB(onFabClick: () -> Unit) {

    FloatingActionButton(
        onClick = onFabClick,
        contentColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Add Status",
            tint = Color.White
        )
    }

}