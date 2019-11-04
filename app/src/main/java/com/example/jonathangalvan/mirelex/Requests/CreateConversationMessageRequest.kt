package com.example.jonathangalvan.mirelex.Requests

class CreateConversationMessageRequest(
    var conversationId: String? = null,
    var userIdFrom: String? = null,
    var userIdTo: String? = null,
    var message: String?
): GlobalRequest() {}