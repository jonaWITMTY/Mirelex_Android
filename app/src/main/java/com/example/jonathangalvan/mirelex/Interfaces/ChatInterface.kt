package com.example.jonathangalvan.mirelex.Interfaces

class ConversationInterface(
    var conversationId: String? = null,
    var userIdTo: String? = null,
    var userTo: String? = null,
    var userToProfile: String? = null,
    var userIdFrom: String? = null,
    var userFrom: String? = null,
    var userFromProfile: String? = null,
    var message: String? = null
){}

class ConversationsInterface(
    var data: ArrayList<ConversationInterface>
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