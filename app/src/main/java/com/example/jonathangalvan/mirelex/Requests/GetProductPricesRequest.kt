package com.example.jonathangalvan.mirelex.Requests

class GetProductPricesRequest(
    var originalPrice: String?,
    var usedTimes: String? = "0",
    var productConditionId: String? = "1"
): GlobalRequest() {}