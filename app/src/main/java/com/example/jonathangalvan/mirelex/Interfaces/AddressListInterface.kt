package com.example.jonathangalvan.mirelex.Interfaces

class AddressListInterface(
    var businessName: String?,
    var email: String?,
    var personalPhone: String?,
    var street: String?,
    var numExt: String?,
    var neighborhood: String?,
    var postalCode: String?,
    var city: String?,
    var state: String?,
    var country: String?,
    var addressId: String?
) {}

class AddressListArrayInterface(
    var data: ArrayList<AddressListInterface>?
) {}