package com.example.jonathangalvan.mirelex.Interfaces

class NotificationInterface(
    var notificationId: String?,
    var message: String?,
    var read: String?,
    var data: String?,
    var created: String?
){}

class NotificationsInterface(
    var data: ArrayList<NotificationInterface>
){}

class NotificationDataInterface(
    var orderId: String? = null,
    var serviceOrderId: String? = null,
    var conversationId: String? = null
){}