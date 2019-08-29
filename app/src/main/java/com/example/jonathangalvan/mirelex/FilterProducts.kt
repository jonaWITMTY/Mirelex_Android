package com.example.jonathangalvan.mirelex

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_filter_products.*
import android.databinding.adapters.SearchViewBindingAdapter.setOnQueryTextListener
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import com.example.jonathangalvan.mirelex.Enums.Gender
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Interfaces.CatalogArrayInterface
import com.example.jonathangalvan.mirelex.Interfaces.CatalogInterface
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalog
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalogs
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.GetProductCatalogsRequest
import com.example.jonathangalvan.mirelex.Requests.ProductFilterRequest
import com.example.jonathangalvan.mirelex.Requests.SizesRequest
import com.thomashaertel.widget.MultiSpinner
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import android.app.Activity
import android.content.Intent


class FilterProducts : AppCompatActivity() {

    var catalogs: ProductCatalogs? = null
    var sizes: ArrayList<CatalogInterface>? = null
    var spinner: MultiSpinner? = null
    var adapter: ArrayAdapter<String>? = null
    var selectedIds: ArrayList<Long> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_products)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0F

        /*Set actionbar title*/
        supportActionBar?.title = resources.getString(R.string.searchProductActionBarTitle)

        /*Filter products by submit keyboard*/
        filterProductSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                doFilterProducts(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        /*Search button filter*/
        filterProductBtn.setOnClickListener(View.OnClickListener {
            doFilterProducts(filterProductSearch.query.toString())
        })

        /*Set catalogs depending on session usertypeid*/
//        getProductCatalogs()
        val bundleFromProducts = intent.extras
        val sizesObj = UtilsModel.getGson().fromJson(bundleFromProducts.getString("sizes"), CatalogArrayInterface::class.java)
        catalogs = UtilsModel.getGson().fromJson(bundleFromProducts.getString("catalogs"), ProductCatalogs::class.java)
        sizes = sizesObj.data
        fillProductsCatalogs()
    }

    fun doFilterProducts(query: String?){
        val productFilterObj: ProductFilterRequest = ProductFilterRequest()
        val user = SessionModel(this).getUser()

        when(user.person?.userGenderId){
            Gender.Female.genderId -> {
                productFilterObj.productTypeId = ProductType.Dress.productTypeId
            }
            Gender.Male.genderId -> {
                productFilterObj.productTypeId = ProductType.Suit.productTypeId
            }
        }

        if(query.toString().isNotEmpty()){
            productFilterObj.name = query.toString()
        }

        if(sizes!![filterProductSize.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.sizeId = sizes!![filterProductSize.selectedItemPosition].productCatalogId.toString()
        }else{
            productFilterObj.sizeId = user.characteristics?.sizeId
        }

        if(catalogs?.conditions!![filterProductCondition.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.productConditionId = catalogs?.conditions!![filterProductCondition.selectedItemPosition].productCatalogId.toString()
        }

        if(catalogs?.styles!![filterProductStyle.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.productStyleId = catalogs?.styles!![filterProductStyle.selectedItemPosition].productCatalogId.toString()
        }

        if(catalogs?.materials!![filterProductMaterial.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.productMaterialId = catalogs?.materials!![filterProductMaterial.selectedItemPosition].productCatalogId.toString()
        }

        if(catalogs?.sleeveStyles!![filterProductSleeveStyle.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.productSleeveStyleId = catalogs?.sleeveStyles!![filterProductSleeveStyle.selectedItemPosition].productCatalogId.toString()
        }

        if(catalogs?.lengths!![filterProductLength.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.productLengthId = catalogs?.lengths!![filterProductLength.selectedItemPosition].productCatalogId.toString()
        }

        if(catalogs?.decorations!![filterProductDecoration.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.productDecorationId = catalogs?.decorations!![filterProductDecoration.selectedItemPosition].productCatalogId.toString()
        }

        if(catalogs?.silhouettes!![filterProductSilouete.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.productSilhouetteId = catalogs?.silhouettes!![filterProductSilouete.selectedItemPosition].productCatalogId.toString()
        }

        if(catalogs?.occasions!![filterProductOcation.selectedItemPosition].productCatalogId.toString() != "0"){
            productFilterObj.productOccasionId = catalogs?.occasions!![filterProductOcation.selectedItemPosition].productCatalogId.toString()
        }

        if(selectedIds != null){
            productFilterObj.productColors = selectedIds
        }

        val returnIntent = Intent()
        returnIntent.putExtra("filterProductRequest", UtilsModel.getGson().toJson(productFilterObj))
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    fun fillProductsCatalogs(){
        fillSpinnerSize(sizes, findViewById(R.id.filterProductSize))
        fillSpinner(catalogs?.conditions, findViewById(R.id.filterProductCondition))
        fillSpinner(catalogs?.styles, findViewById(R.id.filterProductStyle))
        fillSpinner(catalogs?.decorations, findViewById(R.id.filterProductDecoration))
        fillSpinner(catalogs?.lengths, findViewById(R.id.filterProductLength))
        fillSpinner(catalogs?.materials, findViewById(R.id.filterProductMaterial))
        fillSpinner(catalogs?.silhouettes, findViewById(R.id.filterProductSilouete))
        fillSpinner(catalogs?.sleeveStyles, findViewById(R.id.filterProductSleeveStyle))
        fillSpinner(catalogs?.occasions, findViewById(R.id.filterProductOcation))

        /*Get colors*/
        adapter =  ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        for((index, color) in catalogs?.colors!!.withIndex()){
            adapter!!.add(color.name)
        }

        /*Fill spinner with colors*/
        spinner = findViewById(R.id.spinnerMulti)
        spinner!!.setAdapter(adapter, false, onSelectedListener)
    }

    private val onSelectedListener = MultiSpinner.MultiSpinnerListener {
        selectedIds = ArrayList()
        for ((index, value) in it.withIndex()){
            if(value){
                selectedIds.add(catalogs?.colors!![index].productCatalogId!!.toLong())
            }
        }
    }

    fun fillSpinner(data: ArrayList<ProductCatalog>?, adapterView: AdapterView<ArrayAdapter<ProductCatalog>>){
        if(data != null){
            data.add(0, ProductCatalog("0", "--Seleccione--"))
            val adapter = ArrayAdapter<ProductCatalog>(this, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
            adapterView.adapter = adapter
        }
    }

    fun fillSpinnerSize(data: ArrayList<CatalogInterface>?, adapterView: AdapterView<ArrayAdapter<CatalogInterface>>){
        if(data != null){
            data.add(0, CatalogInterface(0, "--Seleccione--"))
            val adapter = ArrayAdapter<CatalogInterface>(this, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
            adapterView.adapter = adapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
