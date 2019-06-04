package com.example.jonathangalvan.mirelex.Requests

class CreateProductRequest(
    var brand: String? = "",
    var leaseable: String? = "0",
    var sellable: String? = "0",
    var productConditionId: String? = "",
    var originalPrice: String? = "",
    var productTypeId: String? = "",
    var productStyleId: String? = "",
    var productMaterialId: String? = "",
    var productColors: String? = "",
    var productSleeveStyleId: String? = "",
    var price: String? = "",
    var sellPrice: String? = "",
    var productOccasionId: String? = "",
    var productDecorationId: String? = "",
    var productLengthId: String? = "",
    var productSilhouetteId: String? = "",
    var bust: String? = "",
    var waist: String? = "",
    var hip: String? = "",
    var height: String? = "",
    var sizeId: String? = ""
): GlobalRequest(){}