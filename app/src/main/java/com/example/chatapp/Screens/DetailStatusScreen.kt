package com.example.chatapp.Screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.CommonImage
import com.example.chatapp.LCViewModel

enum class StatusEnum {
    INITIAL,
    ACTIVE,
    COMPLETED
}


@Composable
fun DetailStatusScreen(navController: NavController, vm: LCViewModel, userId: String) {
    val statuses = vm.status.value.filter {
        it.user.userId == userId
    }
    if (statuses.isNotEmpty()) {
        val currentStatus = remember {
            mutableIntStateOf(0)
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            CommonImage(
                data = statuses[currentStatus.intValue].imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                statuses.forEachIndexed { index, status ->
                    CustomProgressIndicator(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .padding(1.dp),
                        status = if (currentStatus.intValue < index) StatusEnum.INITIAL else if (currentStatus.intValue == index) StatusEnum.ACTIVE else StatusEnum.COMPLETED
                    ){
                        if(currentStatus.intValue < statuses.size -1) currentStatus.value++ else navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun CustomProgressIndicator(modifier: Modifier, status: StatusEnum, onComplete: () -> Unit) {
    var progress = if (status == StatusEnum.INITIAL) 0f else 1f
    if (status == StatusEnum.ACTIVE) {
        val toggleStatus = remember {
            mutableStateOf(false)
        }
        LaunchedEffect(toggleStatus) {
            toggleStatus.value = true
        }
        val p: Float by animateFloatAsState(
            if (toggleStatus.value) 1f else 0f,
            animationSpec = tween(5000),
            finishedListener = { onComplete.invoke() })
        progress = p
    }

    LinearProgressIndicator(modifier = modifier, color = Color.Red, progress = progress)
}