package com.example.kotlinmulti

//import model.Student
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kotlinmulti.loanmanager.ui.navigation.AppNavigation


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
           // App()
            //Assignment2()
            //Problem1()
            //Login()
            //AppNavigation()
            AppNavigation()
            //StudentManagementApp()
            //TikTokAppScreen()
            //CustomersScreen()
        }
    }
}

