package com.example.jonathangalvan.mirelex.Interfaces

class ServiceInterface(
    var serviceOrderId: String ?,
    var folio: String ?,
    var clientId: String ?,
    var storeId: String ?,
    var startDate: String ?,
    var endDate: String ?,
    var startHour: String ?,
    var endHour: String ?,
    var acceptedDate: String ?,
    var rejectedDate: String ?,
    var orderStatusId: String ?,
    var orderTypeId: String ?,
    var orderType: String ?,
    var orderStatus: String ?,
    var total: String ?,
    var totalFormatted: String ?,
    var clientDelivery: String ?,
    var productStyleId: String ?,
    var productStyle: String ?,
    var clientName: String ?,
    var storeName: String ?,
    var deliveryAddress: String ?,
    var paymentInformation: OrderPaymentInformation,
    var ownerDelivery: String ?,
    var sewingTypes: ArrayList<SewingType>,
    var client: OrderPersonInformation,
    var owner: OrderPersonInformation,
    var updates: ArrayList<OrderUpdates>,
    var statusHistory: ArrayList<OrderStatusUpdates>
) {}

class SewingType(
    var sewingTypeId: String?,
    var name: String?
){}

class ServicesInterface(
    var data: ArrayList<ServiceInterface>
){}

class ServiceStoresInterface(
    var data: ArrayList<ServiceStore>
){}

class ServiceStore(
    var userId: String?,
    var name: String,
    var profilePictureUrl: String?,
    var total: String?,
    var totalFormatted: String?
) {
    override fun toString(): String {
        if(total == null){
            return name
        }else{
            return "$name ($totalFormatted)"
        }
    }
}

class SewingInterface(
    var sewingTypeId: Int?,
    var name: String
) {

    override fun toString(): String {
        return name
    }
}

class SewingArrayInterface(
    var data: ArrayList<SewingInterface>
) {}