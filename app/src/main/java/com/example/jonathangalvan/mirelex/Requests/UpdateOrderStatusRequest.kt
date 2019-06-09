package com.example.jonathangalvan.mirelex.Requests

class UpdateOrderStatusRequest(
    var statusId: String?,
    var orderId: String?
): GlobalRequest(){}