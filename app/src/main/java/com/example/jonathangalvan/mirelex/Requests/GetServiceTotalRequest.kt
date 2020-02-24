package com.example.jonathangalvan.mirelex.Requests

class GetServiceTotalRequest(
   var startDate: String?,
   var endDate: String?,
   var orderType: String?,
   var clientDelivery: String?,
   var storeId: String?,
   var productStyleId: ArrayList<Long>?,
   var sewingTypeId: ArrayList<Long>?
): GlobalRequest() {}