package com.example.jonathangalvan.mirelex.Requests

class SendContactMessageRequest (
    var firstName: String?,
    var email: String?,
    var subject: String?,
    var message: String?
): GlobalRequest() {}