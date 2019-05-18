package com.example.jonathangalvan.mirelex.Enums


enum class OrderStatus(val orderStatusId: String) {
    Open("1"),
    Gathering("2"),
    Processing("3"),
    DeliveringProcess("4"),
    Delivered("5"),
    Received("6"),
    Finished("7")
}

enum class OrderType(val orderTypeId: String) {
    Purchase("1"),
    Lease("2"),
    Fitting("3"),
    Cleaning("4"),
    Sewing("5")
}

enum class ProductType(val productTypeId: String) {
    Dress("1"),
    Suit("2"),
}