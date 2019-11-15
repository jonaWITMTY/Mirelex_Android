package com.example.jonathangalvan.mirelex.Interfaces

class GenderInterface (
    val genderId: String,
    val genderName: String
) {

    override fun toString(): String {
        return genderName
    }
}