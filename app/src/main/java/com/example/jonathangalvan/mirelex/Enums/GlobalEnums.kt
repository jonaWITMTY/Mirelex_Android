package com.example.jonathangalvan.mirelex.Enums


enum class OrderStatus(val orderStatusId: String) {
    Open("1"),
    Gathering("2"),
    Processing("3"),
    DeliveringProcess("4"),
    Delivered("5"),
    Received("6"),
    Finished("7"),
    Section("00000000"),
    AcceptDate("1"),
    YesItCould("2"),
    RentInCourse("4"),
    ReadyInStore("5"),
    IsInStore("6"),
    IsCancelled("9"),
    SeeYouSoon("10"),
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

enum class UserType(val userTypeId: String) {
    SuperAdmin("1"),
    Admin("2"),
    Customer("3"),
    Store("4"),
}

enum class Gender(val genderId: String) {
    Male("1"),
    Female("2"),
}