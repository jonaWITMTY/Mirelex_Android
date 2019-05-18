package com.example.jonathangalvan.mirelex.Interfaces

class ProductInterface (
    var productId: Long?,
    var userId: String?,
    var name: String?,
    var description: String?,
    var brand: String?,
    var price: String?,
    var sizeId: String?,
    var size: String?,
    var productConditionId: String?,
    var productTypeId: String?,
    var productStyleId: String?,
    var productDecorationId: String?,
    var productMaterialId: String?,
    var productSleeveLengthId: String?,
    var productSleeveStyleId: String?,
    var productLengthId: String?,
    var productCondition: String?,
    var productType: String?,
    var productStyle: String?,
    var productDecoration: String?,
    var productMaterial: String?,
    var productSleeveLength: String?,
    var productSleeveStyle: String?,
    var productLength: String?,
    var sellPrice: String?,
    var fittable: String?,
    var sellable: String?,
    var leaseable: String?,
    var priceFormatted: String?,
    var sellPriceFormatted: String?,
    var productColors: String?,
    var isFavorite: String?,
    var productFeaturedImage: String?,
    var active: String?,
    var leaseableAvailable: String?,
    var originalPrice: String?
){}

class ProductsInterface(
  var data: ArrayList<ProductInterface>
){}

class ProductInfoInterface(
    var productInformation: ProductInterface,
    var productColors: ArrayList<ProductColorInterface>,
    var productOwner: UserInterface
){}

class ProductColorInterface(
    var productColorsId: String?,
    var productColorId: String?,
    var color: String?
){}
