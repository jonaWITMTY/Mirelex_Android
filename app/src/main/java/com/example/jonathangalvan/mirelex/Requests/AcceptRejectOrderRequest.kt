package com.example.jonathangalvan.mirelex.Requests

class AcceptRejectOrderRequest(
    var orderId: String?,
    var ownerDelivery: Boolean = false
): GlobalRequest() {}