package com.example.kotlinmultiplatform1.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlinmultiplatform1.models.Student

@Composable
fun StudentDetailsPage(
    student: Student,
    onBack: () -> Unit,
    onUpdate: (Student) -> Unit,
    onDelete: () -> Unit
) {

    var showEdit by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    if (showEdit) {

        EditStudentPage(

            student = student,

            onBack = {
                showEdit = false
            },

            onUpdate = {
                onUpdate(it)

            }
        )

        return
    }

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text("Student Details")
                }
            )
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .padding(paddingValues)
                .padding(20.dp)
        ) {

            Text(
                text = student.name,

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Student ID: ${student.id}"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Major: ${student.major}"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Year: ${student.year}"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Email: ${student.email}"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Phone: ${student.phone}"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text =
                    "Status: ${
                        if (student.isActive)
                            "Active"
                        else
                            "Inactive"
                    }"
            )

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Button(

                onClick = {
                    showEdit = true
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Edit Student")
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedButton(

                onClick = {
                    showDeleteDialog = true
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Delete Student")
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedButton(

                onClick = onBack,

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Back")
            }
        }
    }

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text("Delete Student?")
            },

            text = {
                Text(
                    "Delete ${student.name}?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        showDeleteDialog = false

                        onDelete()
                    }
                ) {

                    Text("Delete")
                }
            },

            dismissButton = {

                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}