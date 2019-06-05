package com.example.jonathangalvan.mirelex.Interfaces

import com.example.jonathangalvan.mirelex.Enums.ProductType

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
    var isStretch: String?,
    var productFeaturedImage: String?,
    var active: String?,
    var leaseableAvailable: String?,
    var originalPrice: String?,
    var bust: String?,
    var waist: String?,
    var hip: String?,
    var height: String?,
    var productSilhouetteId: String?,
    var productOccasionId: String?
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


class ProductCatalogs(
    var colors: ArrayList<ProductCatalog>,
    var conditions: ArrayList<ProductCatalog>,
    var materials: ArrayList<ProductCatalog>,
    var sleeveStyles: ArrayList<ProductCatalog>,
    var decorations: ArrayList<ProductCatalog>,
    var styles: ArrayList<ProductCatalog>,
    var lengths: ArrayList<ProductCatalog>,
    var sizes: ArrayList<ProductCatalog>,
    var occasions: ArrayList<ProductCatalog>,
    var silhouettes: ArrayList<ProductCatalog>
){}

class ProductCatalog(
    var productCatalogId: String?,
    var name: String
){
    override fun toString(): String {
        return name
    }
}

class ProductTypes(
    var data: ArrayList<ProductTypeInterface>
) {}

class ProductTypeInterface(
    var productTypeId: String?,
    var name: String
){
    override fun toString(): String {
        return name
    }
}

class GetProductPricesInterface(
    var totalSell: String?,
    var totalRent: String?
) {}

class CreateProductResponseInterface(
    var productId: String?
) {}
