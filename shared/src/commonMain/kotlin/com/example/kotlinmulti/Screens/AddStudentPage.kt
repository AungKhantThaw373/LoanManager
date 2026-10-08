package com.example.kotlinmulti.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlinmulti.Models.Student

@Composable
fun AddStudentPage(
    onBack: () -> Unit,
    onSave: (Student) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var id by remember {
        mutableStateOf("")
    }

    var major by remember {
        mutableStateOf("")
    }

    var year by remember {
        mutableStateOf("Year 1")
    }

    var email by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text("Add Student")
                }
            )
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                label = {
                    Text("Student Name")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            OutlinedTextField(
                value = id,
                onValueChange = {
                    id = it
                },
                label = {
                    Text("Student ID")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            OutlinedTextField(
                value = major,
                onValueChange = {
                    major = it
                },
                label = {
                    Text("Major")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Text("Year")

            Row {

                listOf(
                    "Year 1",
                    "Year 2",
                    "Year 3",
                    "Year 4"
                ).forEach { item ->

                    Row {

                        androidx.compose.material3.RadioButton(

                            selected = year == item,

                            onClick = {
                                year = item
                            }
                        )

                        Text(
                            text = item,

                            modifier = Modifier.padding(
                                top = 12.dp
                            )
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                },
                label = {
                    Text("Email")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                },
                label = {
                    Text("Phone")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(50.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick = onBack,

                    modifier = Modifier.weight(1f)
                ) {

                    Text("Cancel")
                }

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Button(

                    onClick = {

                        val student = Student(

                            name = name,
                            id = id,
                            major = major,
                            year = year,
                            email = email,
                            phone = phone
                        )

                        onSave(student)
                    },

                    modifier = Modifier.weight(1f)
                ) {

                    Text("Save")
                }
            }
        }
    }
}