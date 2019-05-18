package com.example.jonathangalvan.mirelex.Requests

class RegisterRequest(
    var email: String ,
    var password: String,
    var firstName: String?,
    var paternalLastName: String?,
    var companyName: String?,
    var genderId: String?,
    var isStore: Int
): GlobalRequest() {

}