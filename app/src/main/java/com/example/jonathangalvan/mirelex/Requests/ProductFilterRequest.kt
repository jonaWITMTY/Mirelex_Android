package com.example.jonathangalvan.mirelex.Requests

class ProductFilterRequest(
    var name: String? = null,
    var sizeId: String? = null,
    var productConditionId: String? = null,
    var productStyleId: String? = null,
    var productMaterialId: String? = null,
    var productColors: ArrayList<Long>? = null,
    var productSleeveStyleId: String? = null,
    var productLengthId: String? = null,
    var productDecorationId: String? = null,
    var productSilhouetteId: String? = null,
    var productOccasionId: String? = null,
    var minPrice: String? = null,
    var maxPrice: String? = null,
    var productTypeId: String? = null

): GlobalRequest() {}