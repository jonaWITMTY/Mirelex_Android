package com.example.jonathangalvan.mirelex.Requests

class GetConversationMessagesRequest(
    var conversationId: String? = null,
    var userIdFrom: String? = null,
    var userIdTo: String? = null
): GlobalRequest() {}