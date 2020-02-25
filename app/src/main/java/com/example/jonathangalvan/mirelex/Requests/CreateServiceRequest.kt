package com.example.jonathangalvan.mirelex.Requests

class CreateServiceRequest(
    var startDate: String? = "",
    var endDate: String? = "",
    var startHour: String = "0",
    var endHour: String = "0",
    var orderType: String?,
    var total: String? = "0",
    var cardId: String? = "-1",
    var clientDelivery: Boolean = false,
    var addressId: String? = "",
    var storeId: String?,
    var productStyleId: ArrayList<Long>? = null,
    var sewingTypeId: ArrayList<Long>? = null
): GlobalRequest(){
    override fun toString(): String {
        return "CreateServiceRequest(startDate=$startDate, endDate=$endDate, startHour='$startHour', endHour='$endHour', orderType=$orderType, total=$total, cardId=$cardId, clientDelivery='$clientDelivery', addressId=$addressId, storeId=$storeId, productStyleId=$productStyleId, sewingTypeId=$sewingTypeId)"
    }
}