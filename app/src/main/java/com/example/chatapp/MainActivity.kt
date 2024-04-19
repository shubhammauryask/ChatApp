package com.example.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.Screens.ChatListScreen
import com.example.chatapp.Screens.DetailChatScreen
import com.example.chatapp.Screens.DetailStatusScreen
import com.example.chatapp.Screens.LoginScreen
import com.example.chatapp.Screens.ProfileScreen
import com.example.chatapp.Screens.SignUpScreen
import com.example.chatapp.Screens.StatusListScreen
import com.example.chatapp.ui.theme.ChatAppTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(var route: String) {
    object SignUp : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object ChatList : DestinationScreen("chatList")
    object DetailChat : DestinationScreen("detailChat/{chatId}") {
        fun createRoute(id: String) = "detailchat/$id"
    }

    object StatueList : DestinationScreen("statusList")
    object DetailStatus : DestinationScreen("detailStatus/{userId}") {
        fun createRoute(userId: String) = "detailstatus/$userId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation()
                }
            }
        }
    }

    @Composable
    fun ChatAppNavigation() {
        val navController = rememberNavController()
        var vm = hiltViewModel<LCViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {
            composable(DestinationScreen.SignUp.route) {
                SignUpScreen(navController, vm)
            }
            composable(DestinationScreen.Login.route) {
                LoginScreen(navController, vm)
            }
            composable(DestinationScreen.ChatList.route) {
                ChatListScreen(navController, vm)
            }
            composable(DestinationScreen.DetailChat.route) {
                val chatId = it.arguments?.getString("chatId")
                chatId?.let {
                    DetailChatScreen(
                        navController = navController,
                        vm = vm,
                        chatId = chatId
                    ) //  pass value with using variable
                }

            }
            composable(DestinationScreen.StatueList.route) {
                StatusListScreen(navController, vm)
            }
            composable(DestinationScreen.Profile.route) {
                ProfileScreen(navController, vm)// pass value without using  variable
            }

            composable(DestinationScreen.DetailStatus.route) {
                val userId = it.arguments?.getString("userId")
                userId?.let {
                    DetailStatusScreen(navController, vm, userId = it)
                }
            }
        }
    }

}

