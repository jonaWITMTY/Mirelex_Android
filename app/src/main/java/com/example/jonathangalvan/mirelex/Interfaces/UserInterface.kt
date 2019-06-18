package com.example.jonathangalvan.mirelex.Interfaces

class UserInterface(
    var person: Person?,
    var address: ArrayList<Address>?,
    var characteristics: Characteristics?,
    var paymentCards: ArrayList<PaymentCard>
) {}

class Person(
    var userId: String?,
    var employeeId: String?,
    var userTypeId: String?,
    var userType: String?,
    var superAdmin: String?,
    var firstName: String?,
    var secondName: String?,
    var paternalLastName: String?,
    var maternalLastName: String?,
    var profilePictureUrl: String?,
    var email: String?,
    var active: String?,
    var userPositionId: String?,
    var userPosition: String?,
    var userCategoryId: String?,
    var userCategory: String?,
    var personalPhone: String?,
    var birthDate: String?,
    var userGenderId: String?,
    var userGender: String?,
    var userStatusId: String?,
    var userStatus: String?,
    var planStatus: String?,
    var personTypeId: String?,
    var personType: String?,
    var companyName: String?,
    var rfc: String?,
    var emails: String?,
    var notifications: String?,
    var textMessages: String?,
    var isMirelexStore: String?,
    var homePhone: String?,
    var oneSignalId: String?,
    var facebookUrl: String?,
    var instagramUrl: String?,
    var userFacebookId: String?
) {}

class Address (
    var addressId: String?,
    var addressTypeId: String?,
    var addressTypeName: String?,
    var street: String?,
    var numInt: String?,
    var numExt: String?,
    var betweenStreet: String?,
    var floor: String?,
    var office: String?,
    var neighborhoodId: String?,
    var neighborhoodName: String?,
    var postalCode: String?,
    var cityId: String?,
    var cityName: String?,
    var stateId: String?,
    var stateName: String?,
    var countryId: String?,
    var countryName: String?
){}

class Characteristics (
    var characteristicsId: String?,
    var height: String?,
    var breast: String?,
    var hip: String?,
    var waist: String?,
    var shoulderFloor: String?,
    var shoulderShoulder: String?,
    var skinColorId: String?,
    var bodyTypeId: String?,
    var hairColorId: String?,
    var size: String?,
    var sizeId: String?,
    var skinColor: String?,
    var bodyType: String?,
    var hairColor: String?,
    var bust: String?
){}

class PaymentCard(
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