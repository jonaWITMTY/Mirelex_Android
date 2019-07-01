package com.example.jonathangalvan.mirelex.Requests

class RegisterRequest(
    var email: String ,
    var password: String,
    var firstName: String?,
    var paternalLastName: String?,
    var companyName: String?,
    var isStore: Int,
    var genderId: String? = null
): GlobalRequest() {

}