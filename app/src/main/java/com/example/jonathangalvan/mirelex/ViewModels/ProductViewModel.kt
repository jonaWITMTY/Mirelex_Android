package com.example.jonathangalvan.mirelex.ViewModels

import androidx.lifecycle.ViewModel
import com.example.jonathangalvan.mirelex.Interfaces.ProductImageInterface
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Requests.UpdateProductRequest
import java.io.File

class ProductViewModel: ViewModel() {
    var productId = ""
    var productObjRequest = UpdateProductRequest()
    var productObj: ProductInfoInterface? = null
    var productProcessType = ""
    var featuredImage: File? = null
    var secondaryImgs: ArrayList<ProductImageInterface> = ArrayList()
}