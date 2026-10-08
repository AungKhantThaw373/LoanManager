package com.example.kotlinmultiplatform1.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudentSearch(
    value: String,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(

        value = value,

        onValueChange = {
            onValueChange(it)
        },

        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),

        label = {
            Text("Search Student")
        },

        placeholder = {
            Text("Enter name or ID")
        },

        singleLine = true
    )
}