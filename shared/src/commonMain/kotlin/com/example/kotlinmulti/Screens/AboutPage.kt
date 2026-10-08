package com.example.kotlinmulti.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutPage(
    onBack: () -> Unit
) {

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "Student Management App"
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text =
                "A beginner-level application " +
                        "for managing student information."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Built with Kotlin and Jetpack Compose"
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedButton(
            onClick = onBack
        ) {

            Text("Back")
        }
    }
}