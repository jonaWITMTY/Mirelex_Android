package com.example.jonathangalvan.mirelex.Requests

class SendContactMessageRequest (
    var firstName: String?,
    var subject: String?,
    var email: String?,
    var message: String?
): GlobalRequest() {}