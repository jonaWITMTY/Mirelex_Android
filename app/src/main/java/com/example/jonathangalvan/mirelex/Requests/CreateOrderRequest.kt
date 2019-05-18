package com.example.jonathangalvan.mirelex.Requests

class CreateOrderRequest(
    var productId: String? = "",
    var startDate: String? = "",
    var endDate: String? = "",
    var startHour: String = "0",
    var endHour: String = "0",
    var orderType: String?,
    var total: String? = "0",
    var cardId: String? = "-1",
    var clientDelivery: String = "0",
    var ownerDelivery: String = "0",
    var addressId: String? = ""
): GlobalRequest(){
    override fun toString(): String {
        return "CreateOrderRequest(productId=$productId, startDate=$startDate, endDate=$endDate, startHour='$startHour', endHour='$endHour', orderType=$orderType, total=$total, cardId=$cardId, clientDelivery='$clientDelivery', ownerDelivery='$ownerDelivery', addressId=$addressId)"
    }
}