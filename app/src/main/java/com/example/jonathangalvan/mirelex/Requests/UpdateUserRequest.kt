package com.example.jonathangalvan.mirelex.Requests

class UpdateUserRequest (
    var email: String = "" ,
    var password: String = "",
    var firstName: String? = null,
    var paternalLastName: String? = null,
    var maternalLastName: String? = null,
    var companyName: String? = null,
    var gender: String? = "",
    var genderId: String? = "",
    var isStore: Int = 0,
    var homePhone: String? = null,
    var personalPhone: String? = null,
    var street: String? = null,
    var numExt: String? = null,
    var numInt: String? = null,
    var zipCode: String? = null,
    var neighborhoodId: String? = null,
    var height: String? = null,
    var sizeId: String? = null,
    var bodyTypeId: String? = null,
    var skinColorId: String? = null,
    var hairColorId: String? = null,
    var bust: String? = null,
    var waist: String? = null,
    var hip: String? = null,
    var facebookProfile: String? = null,
    var instagramProfile: String? = null,
    var characteristicsId: String? = null,
    var userId: String? = "",
    var addressId: String? = null,
    var userTypeId: String? = "",
    var isMirelexStore: String? = "0",
    var birthDate: String? = "",
    var isDesigner: Int = 0

): GlobalRequest(){

    override fun toString(): String {
        return "UpdateUserRequest(email='$email', password='$password', firstName=$firstName, paternalLastName=$paternalLastName, maternalLastName=$maternalLastName, companyName=$companyName, gender=$gender, genderId=$genderId, isStore=$isStore, homePhone=$homePhone, personalPhone=$personalPhone, street=$street, numExt=$numExt, numInt=$numInt, zipCode=$zipCode, neighborhoodId=$neighborhoodId, height=$height, sizeId=$sizeId, bodyTypeId=$bodyTypeId, skinColorId=$skinColorId, hairColorId=$hairColorId, bust=$bust, waist=$waist, hip=$hip, facebookProfile=$facebookProfile, instagramProfile=$instagramProfile, characteristicsId=$characteristicsId, userId=$userId, addressId=$addressId, userTypeId=$userTypeId)"
    }
}