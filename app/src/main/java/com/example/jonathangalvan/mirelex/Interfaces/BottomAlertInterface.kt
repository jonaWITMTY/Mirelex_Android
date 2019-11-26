package com.example.jonathangalvan.mirelex.Interfaces

class BottomAlertInterface(
    /*Payment alert*/
    var cardId: String? = null,
    var cardDefault: String? = null,
    var alertType: String? = null,

    /*Fitting sizes*/
    var productId: String? = null,
    var orderId: String? = null,
    var userId: String? = null,

    /*Twilio*/
    var formProfilePage: String = "0"
) {}