package com.example.jonathangalvan.mirelex.Interfaces

class ConversationInterface(
    var userIdTo: String? = null,
    var userTo: String? = null,
    var userIdFrom: String? = null,
    var conversationId: String? = null
){}

class ConverationMessagesInterface(
    var data: ArrayList<ConversationMessageInterface>
) {}

class ConversationMessageInterface(
    var conversationId: String?,
    var messageId: String?,
    var userIdTo: String?,
    var userTo: String?,
    var userToProfile: String?,
    var userIdFrom: String?,
    var userFrom: String?,
    var userFromProfile: String?,
    var message: String?,
    var created: String
){}