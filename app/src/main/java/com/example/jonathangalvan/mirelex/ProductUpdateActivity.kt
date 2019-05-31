package com.example.jonathangalvan.mirelex

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalog
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalogs
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.GetProductCatalogs
import com.example.jonathangalvan.mirelex.Requests.GetProductInfoRequest
import kotlinx.android.synthetic.main.activity_product_update.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch
import com.squareup.picasso.Picasso
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview


class ProductUpdateActivity : AppCompatActivity() {

    var productId: String = ""
    var productObj: ProductInfoInterface? = null
    var catalogs: ProductCatalogs? = null
    var imgPreview: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_update)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Get intent bunlde*/
        val bunldeFromProducts = intent.extras
        productId = bunldeFromProducts.getString("productId")

        /*Get product info*/
        getProductInfo()

        /*Image preview*/
        updateProductImageView.setOnClickListener( View.OnClickListener {
            ImagePreview().newInstance(imgPreview).show(supportFragmentManager,"alertDialog")
        })
    }

    fun getProductInfo(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        var getProductInfoObj = UtilsModel.getGson().toJson(GetProductInfoRequest(productId))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getProductInfo), getProductInfoObj)).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    productObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), ProductInfoInterface::class.java)
                    runOnUiThread {
                        run {
                            getProductCatalogs()
                        }
                    }
                }
            }
        })
    }

    fun getProductCatalogs(){
        val productCatalogsObj = UtilsModel.getGson().toJson(GetProductCatalogs(productObj?.productInformation?.productTypeId.toString()))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( this, resources.getString(R.string.getProducCatalogs), productCatalogsObj)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val productCatalogObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), ProductCatalogs::class.java)
                    catalogs = productCatalogObj
                    runOnUiThread {
                        run{
                            fillProductFields()
                        }
                    }
                }
            }
        })
    }

    fun fillProductFields(){
        /*Fill products catalogs*/
        fillProductsCatalogs()

        /*Set image for image preview*/
        imgPreview = productObj?.productInformation?.productFeaturedImage.toString()

        /*Fill common product fields*/
        Picasso.with(this).load(productObj?.productInformation?.productFeaturedImage).into(updateProductImageView)
        updateProductBrand.editText?.setText(productObj!!.productInformation.brand)
        updateProductCategory.editText?.setText(productObj!!.productInformation.productType)
        updateProductPrice.editText?.setText(productObj!!.productInformation.originalPrice)
        updateProductDescription.editText?.setText(productObj!!.productInformation.description)

        when(productObj?.productInformation?.productTypeId){
            ProductType.Dress.productTypeId -> {
                supportActionBar?.title = "${productObj?.productInformation?.brand} - ${productObj?.productInformation?.productStyle}"

                /*Fill dress fields*/
                updateProductBust.editText?.setText(productObj!!.productInformation.bust)
                updateProductWaist.editText?.setText(productObj!!.productInformation.waist)
                updateProductHip.editText?.setText(productObj!!.productInformation.hip)
                updateProductHeight.editText?.setText(productObj!!.productInformation.height)

            }
            else -> {
                supportActionBar?.title = "${productObj?.productInformation?.brand}"
            }
        }
    }

    fun fillProductsCatalogs(){
        fillSpinner(catalogs?.conditions, findViewById(R.id.updateProductCondition))
        updateProductCondition.setSelection(getAdapterItemPosition( productObj?.productInformation?.productConditionId?.toLong(), catalogs?.conditions))

        fillSpinner(catalogs?.styles, findViewById(R.id.updateProductStyle))
        updateProductStyle.setSelection(getAdapterItemPosition( productObj?.productInformation?.productStyleId?.toLong(), catalogs?.styles))

        fillSpinner(catalogs?.decorations, findViewById(R.id.updateProductDecoration))
        updateProductDecoration.setSelection(getAdapterItemPosition( productObj?.productInformation?.productDecorationId?.toLong(), catalogs?.decorations))

        fillSpinner(catalogs?.lengths, findViewById(R.id.updateProductLength))
        updateProductLength.setSelection(getAdapterItemPosition( productObj?.productInformation?.productLengthId?.toLong(), catalogs?.lengths))

        fillSpinner(catalogs?.materials, findViewById(R.id.updateProductMaterial))
        updateProductMaterial.setSelection(getAdapterItemPosition( productObj?.productInformation?.productMaterialId?.toLong(), catalogs?.materials))

        fillSpinner(catalogs?.silhouettes, findViewById(R.id.updateProductSilouete))
        updateProductSilouete.setSelection(getAdapterItemPosition( productObj?.productInformation?.productSilhouetteId?.toLong(), catalogs?.silhouettes))

        fillSpinner(catalogs?.sleeveStyles, findViewById(R.id.updateProductSleeveStyle))
        updateProductSleeveStyle.setSelection(getAdapterItemPosition( productObj?.productInformation?.productSleeveStyleId?.toLong(), catalogs?.sleeveStyles))

        fillSpinner(catalogs?.occasions, findViewById(R.id.updateProductOcation))
        updateProductOcation.setSelection(getAdapterItemPosition( productObj?.productInformation?.productOccasionId?.toLong(), catalogs?.occasions))

        var colorArr: ArrayList<KeyPairBoolData> = ArrayList()
        for(color in catalogs?.colors!!){
            val colorObj = KeyPairBoolData()
            colorObj.id = color.productCatalogId!!.toLong()
            colorObj.name = color.name
            colorArr?.add(colorObj)
        }

        val multiSpinnerView = findViewById<MultiSpinnerSearch>(R.id.updateProductColors)
        multiSpinnerView.setItems(colorArr, -1) { items ->
            for (i in items.indices) {
                if (items[i].isSelected) {
                }
            }
        }
    }

    fun fillSpinner(data: ArrayList<ProductCatalog>?, adapterView: AdapterView<ArrayAdapter<ProductCatalog>>){
        if(data != null){
            val adapter = ArrayAdapter<ProductCatalog>(this@ProductUpdateActivity, R.layout.view_spinner_item_black, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black)
            adapterView.adapter = adapter
        }
    }

    private fun getAdapterItemPosition(id: Long?, mListAdapter: ArrayList<ProductCatalog>?): Int {
        var currentPosition = 0
        if(mListAdapter != null) {
            for (position in 0 until mListAdapter!!.size) {
                if (mListAdapter.get(position).productCatalogId?.toLong() == id) {
                    currentPosition = position
                }
            }
        }
        return currentPosition
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
