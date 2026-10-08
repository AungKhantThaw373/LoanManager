package com.example.kotlinmultiplatform1.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.kotlinmultiplatform1.components.DashboardCard
import com.example.kotlinmultiplatform1.components.StudentCard
import com.example.kotlinmultiplatform1.components.StudentSearch
import com.example.kotlinmultiplatform1.models.Student

@Composable
fun HomePage() {

    var students by remember {

        mutableStateOf(
            listOf(

                Student(
                    name = "Alice",
                    id = "IT001",
                    major = "Computer Science",
                    year = "Year 2",
                    email = "alice@gmail.com",
                    phone = "09123456789"
                ),

                Student(
                    name = "Casandra",
                    id = "IT002",
                    major = "Computer Science",
                    year = "Year 2",
                    email = "casandra@gmail.com",
                    phone = "09987654321"
                ),

                Student(
                    name = "Elena",
                    id = "IT003",
                    major = "Electronic Communication",
                    year = "Year 3",
                    email = "elena@gmail.com",
                    phone = "09222222222"
                )
            )
        )
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedStudent by remember {
        mutableStateOf<Student?>(null)
    }

    var showAddScreen by remember {
        mutableStateOf(false)
    }

    var showAboutScreen by remember {
        mutableStateOf(false)
    }

    // Student Detail
    if (selectedStudent != null) {

        StudentDetailsPage(

            student = selectedStudent!!,

            onBack = {
                selectedStudent = null
            },

            onUpdate = { updatedStudent ->

                students = students.map {

                    if (it.id == updatedStudent.id) {
                        updatedStudent
                    } else {
                        it
                    }
                }

                selectedStudent = null
            },

            onDelete = {

                students = students.filter {
                    it.id != selectedStudent!!.id
                }

                selectedStudent = null
            }
        )

        return
    }

    // Add Student
    if (showAddScreen) {

        AddStudentPage(

            onBack = {
                showAddScreen = false
            },

            onSave = { newStudent ->

                students = students + newStudent

                showAddScreen = false
            }
        )

        return
    }

    // About
    if (showAboutScreen) {

        AboutPage(
            onBack = {
                showAboutScreen = false
            }
        )

        return
    }

    val filteredStudents =
        students.filter {

            it.name.contains(
                searchText,
                ignoreCase = true
            ) ||

                    it.id.contains(
                        searchText,
                        ignoreCase = true
                    )
        }

    val activeStudents =
        students.count {
            it.isActive
        }

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text("Student Management")
                }
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showAddScreen = true
                }
            ) {

                Text("+")
            }
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            Text(
                text = "Welcome, Teacher",

                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    DashboardCard(
                        title = "Students",
                        value = students.size.toString()
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    DashboardCard(
                        title = "Active",
                        value = activeStudents.toString()
                    )
                }
            }

            StudentSearch(

                value = searchText,

                onValueChange = {
                    searchText = it
                }
            )

            Text(
                text = "Students",

                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            )

            LazyColumn {

                items(filteredStudents) { student ->

                    StudentCard(

                        student = student,

                        onClick = {
                            selectedStudent = student
                        },

                        onFavorite = {

                            students =
                                students.map {

                                    if (it.id == student.id) {

                                        it.copy(
                                            isFavorite =
                                                !it.isFavorite
                                        )

                                    } else {
                                        it
                                    }
                                }
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(

                onClick = {
                    showAboutScreen = true
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text("About")
            }
        }
    }
}