package com.example.chatapp.Screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.CommonDivider
import com.example.chatapp.CommonImage
import com.example.chatapp.CommonProgressBar
import com.example.chatapp.DestinationScreen
import com.example.chatapp.LCViewModel
import com.example.chatapp.navigateTo

@Composable
fun ProfileScreen(navController: NavController, vm: LCViewModel) {
    val inProgress = vm.inProcess.value
    if (inProgress) {
        CommonProgressBar()
    } else {
        val userData = vm.userDate.value
        var name by rememberSaveable {
                mutableStateOf(userData?.name?:"")
        }
        var number by rememberSaveable {
            mutableStateOf(userData?.number?:"")
        }
        Column {
            ProfileContent(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                onBack = {
                         navigateTo(navController= navController, route = DestinationScreen.ChatList.route)
                },
                onSave = {
                     vm.cretaeOrUpdateProfile(name = name,number = number)
                },
                vm = vm,
                name = name,
                number = number,
                onNameChange = {name = it},
                onNumberChange = {number = it},
                onLogOut = {
                    vm.logOut()
                     navigateTo(navController = navController,route = DestinationScreen.Login.route)
                }
            )
            BottomNavMenu(selectedItem = BottomNavItem.PROFILE, navController = navController)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    modifier: Modifier,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogOut:() -> Unit,
    vm: LCViewModel,
    name:String,
    number: String,
    onNameChange:(String)->Unit,
    onNumberChange:(String)->Unit,

) {
    val imageUrl = vm.userDate.value?.imageUrl
    Column (modifier = modifier){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", modifier = Modifier.clickable {
                onBack.invoke()
            })
            Text(text = "Save", modifier = Modifier.clickable {
                onSave.invoke()
            })
        }
            CommonDivider()
            ProfileImage(imageUrl = imageUrl, vm = vm)
            CommonDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Name", modifier = Modifier.width(100.dp))
                TextField(value = name, onValueChange =onNameChange, colors = TextFieldDefaults.textFieldColors(
                    focusedTextColor = Color.Black,
                    containerColor = Color.Transparent
                )
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Number", modifier = Modifier.width(100.dp))
                TextField(value = number, onValueChange =onNumberChange, colors = TextFieldDefaults.textFieldColors(
                    focusedTextColor = Color.Black,
                    containerColor = Color.Transparent
                )
                )
            }
            CommonDivider()
            Row (modifier = Modifier
                .fillMaxWidth()
                .padding(60.dp),
                horizontalArrangement = Arrangement.Center){
                Text(text = "LogOut", color = Color.Red,modifier = Modifier.clickable {
                    onLogOut.invoke()
                })
            }

    }
}

@Composable
fun ProfileImage(imageUrl: String?, vm: LCViewModel) {
    var launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
//            onResult =
        ) { uri ->
            uri?.let {
                vm.uploadProfileImage(uri)
            }
        }
    Box(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
        Column(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                launcher.launch("image/*")
            }, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                CommonImage(data = imageUrl)
            }
            Text(text = "Chage Profile Picture")
        }
        if (vm.inProcess.value) {
            CommonProgressBar()
        }
    }
}
