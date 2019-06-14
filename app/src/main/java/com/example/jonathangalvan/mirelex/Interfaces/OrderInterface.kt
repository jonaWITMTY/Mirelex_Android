package com.example.jonathangalvan.mirelex.Interfaces

class OrderInterface(
    var orderId: String?,
    var folio: String?,
    var clientId: String?,
    var startDate: String?,
    var endDate: String?,
    var locationRange: String?,
    var orderStatusId: String?,
    var orderTypeId: String?,
    var total: String?,
    var orderType: String?,
    var orderStatus: String?,
    var clientName: String?,
    var paymentDate: String?,
    var suppliers: String?,
    var totalFormatted: String?,
    var acceptedDate: String?,
    var rejectedDate: String?,
    var clientDelivery: String?,
    var ownerDelivery: String?,
    var product: ProductInterface
) { }

class OrdersInterface(
    var data: ArrayList<OrderInterface>
){}

class OrderProductInfo(
    var orderInformation: OrderInterface,
    var orderProducts: ArrayList<OrderProducts>,
    var orderClientInformation: OrderPersonInformation,
    var orderOwnerInformation: OrderPersonInformation,
    var orderUpdates: ArrayList<OrderUpdates>,
    var orderPaymentInformation: OrderPaymentInformation
){}

class OrderProducts(
    var productId: String?,
    var userId: String?,
    var name: String?,
    var brand: String?,
    var price: String?,
    var sellPrice: String?,
    var priceFormatted: String?,
    var sellPriceFormatted: String?,
    var productFeaturedImage: String?,
    var active: String?,
    var fittings: ArrayList<ProductFittingsInterface>?
){}

class OrderUpdates(
    var orderUpdateId: String?,
    var orderId: String?,
    var originalOrderStatusId: String?,
    var newOrderStatusId: String?,
    var created: String?,
    var originalStatus: String?,
    var newStatus: String?
){}

class OrderPaymentInformation(
    var cardId: String?,
    var conektaCardId: String?,
    var name: String?,
    var lastDigits: String?,
    var bin: String?,
    var expireMonth: String?,
    var expireYear: String?,
    var brand: String?,
    var default: String?
){}

class OrderPersonInformation(
    var userId: String?,
    var firstName: String?,
    var secondName: String?,
    var paternalLastName: String?,
    var maternalLastName: String?,
    var email: String?,
    var personalPhone: String?,
    var homePhone: String?,
    var conektaId: String?,
    var companyName: String?,
    var isMirelexStore: String?,
    var profilePictureUrl: String?,
    var address: Address
){}

class OrderTotal(
    var total: String?,
    var totalFormatted: String?,
    var delivery: String?,
    var deliveryFormatted: String?
){}