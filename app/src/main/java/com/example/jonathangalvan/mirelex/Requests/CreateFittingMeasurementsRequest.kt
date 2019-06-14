package com.example.jonathangalvan.mirelex.Requests

class CreateFittingMeasurementsRequest(
    var productId: String?,
    var userId: String?,
    var orderId: String?,
    var bust: String?,
    var waist: String?,
    var hip: String?,
    var height: String?
): GlobalRequest() {}