package com.example.jonathangalvan.mirelex.Requests

class UpdateServiceOrderStatusRequest(
    var statusId: String?,
    var serviceOrderId: String?
): GlobalRequest() {}