package com.example.kotlinmulti.Models

data class Student(
    var name: String,
    var id: String,
    var major: String,
    var year: String,
    var email: String,
    var phone: String,
    var isActive: Boolean = true,
    var isFavorite: Boolean = false
)
