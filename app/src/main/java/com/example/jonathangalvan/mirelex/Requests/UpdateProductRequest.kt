package com.example.jonathangalvan.mirelex.Requests

class UpdateProductRequest(
    var productId: String? = "",
    var brand: String? = "",
    var leaseable: String? = "0",
    var sellable: String? = "0",
    var productConditionId: String? = "",
    var originalPrice: String? = "",
    var productTypeId: String? = "",
    var productStyleId: String? = "",
    var productMaterialId: String? = "",
    var productColors: List<Long>? = null,
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
    var sizeId: String? = "",
    var isStretch: String? = "0",
    var description: String? = ""
): GlobalRequest() {}