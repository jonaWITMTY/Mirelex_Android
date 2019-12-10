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
import com.example.jonathangalvan.mirelex.UI.MultiSpinnerCustom


class FilterProducts : AppCompatActivity() {

    var catalogs: ProductCatalogs? = null
    var sizes: ArrayList<CatalogInterface>? = null
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

        /*On recommendations switch change event*/
        filterProductRecomendations.setOnCheckedChangeListener { _, isChecked ->
            showFilterFields(isChecked)
        }


        /*Set catalogs depending on session usertypeid*/
        val bundleFromProducts = intent.extras
        val sizesObj = UtilsModel.getGson().fromJson(bundleFromProducts.getString("sizes"), CatalogArrayInterface::class.java)
        catalogs = UtilsModel.getGson().fromJson(bundleFromProducts.getString("catalogs"), ProductCatalogs::class.java)
        sizes = sizesObj?.data
        fillProductsCatalogs()
    }

    fun showFilterFields(isChecked: Boolean){
        if(isChecked){
            filterProductFilterFieldsLayout.visibility = View.GONE
        }else{
            filterProductFilterFieldsLayout.visibility = View.VISIBLE
        }
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

        if(filterProductRecomendations.isChecked){
            productFilterObj.mirelexSuggestion = filterProductRecomendations.isChecked
        }else{
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
        }

        val strFilter = UtilsModel.getGson().toJson(productFilterObj)
        SessionModel.saveSessionValue(this, "filterProductRequest", strFilter)
        val returnIntent = Intent()
        returnIntent.putExtra("filterProductRequest", strFilter)
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

        /*Fill spinner with colors*/
        val multiSpinner = findViewById<MultiSpinnerCustom>(R.id.spinnerMulti)
        multiSpinner.setItems("", "colors", UtilsModel.getGson().toJson(catalogs), onSelectedListener)

        if(SessionModel.existSessionValue(this, "filterProductRequest")){
            fillProductCatalogsInputs()
        }
    }

    fun fillProductCatalogsInputs(){
        val filterContent = UtilsModel.getGson().fromJson(SessionModel.getSessionValue(this, "filterProductRequest"), ProductFilterRequest::class.java)

        if(filterContent.mirelexSuggestion == true){
            filterProductRecomendations.isChecked = true
            showFilterFields(true)
        }else{
            filterProductSize.setSelection(getSizeItemPosition(filterContent.sizeId?.toLong()))
            filterProductSearch.setQuery(filterContent.name, false)
            filterProductCondition.setSelection(getCatalogItemPosition(filterContent.productConditionId?.toLong(), catalogs!!.conditions))
            filterProductStyle.setSelection(getCatalogItemPosition(filterContent.productStyleId?.toLong(), catalogs!!.styles))
            filterProductMaterial.setSelection(getCatalogItemPosition(filterContent.productMaterialId?.toLong(), catalogs!!.materials))
            filterProductSleeveStyle.setSelection(getCatalogItemPosition(filterContent.productSleeveStyleId?.toLong(), catalogs!!.sleeveStyles))
            filterProductLength.setSelection(getCatalogItemPosition(filterContent.productLengthId?.toLong(), catalogs!!.lengths))
            filterProductDecoration.setSelection(getCatalogItemPosition(filterContent.productDecorationId?.toLong(), catalogs!!.decorations))
            filterProductSilouete.setSelection(getCatalogItemPosition(filterContent.productSilhouetteId?.toLong(), catalogs!!.silhouettes))
            filterProductOcation.setSelection(getCatalogItemPosition(filterContent.productOccasionId?.toLong(), catalogs!!.occasions))

            /*Get colors*/
            var selectedItems = BooleanArray(catalogs?.colors!!.size)
            for((index, color) in catalogs?.colors!!.withIndex()){
                if(checkIfColorExist(color.productColorCatalogId!!.toLong(), filterContent?.productColors)){
                    selectedItems[index] = true
                    selectedIds.add(catalogs?.colors!![index].productColorCatalogId!!.toLong())
                }else{
                    selectedItems[index] = false
                }
            }

            /*Fill spinner with colors*/
            val multiSpinner = findViewById<MultiSpinnerCustom>(R.id.spinnerMulti)
            multiSpinner.setItems("", "colors", UtilsModel.getGson().toJson(catalogs), onSelectedListener, selectedItems)
        }
    }

    fun checkIfColorExist(colorId: Long, colorArr: ArrayList<Long>?): Boolean{
        var isInColorArr = false
        if(!isInColorArr){
            if (colorArr != null) {
                for(color in colorArr){
                    if(color == colorId){
                        isInColorArr = true
                    }
                }
            }
        }
        return isInColorArr
    }

    private val onSelectedListener = object:  MultiSpinnerCustom.MultiSpinnerListener {
        override fun onItemsSelected(selected: BooleanArray?) {
            selectedIds = ArrayList()
            for ((index, value) in selected!!.withIndex()){
                if(value){
                    selectedIds.add(catalogs?.colors!![index].productColorCatalogId!!.toLong())
                }
            }
        }
    }

    fun fillSpinner(data: ArrayList<ProductCatalog>?, adapterView: AdapterView<ArrayAdapter<ProductCatalog>>){
        if(data != null){
            data.add(0, ProductCatalog("0", resources!!.getString(R.string.selectOptionDropdown)))
            val adapter = ArrayAdapter<ProductCatalog>(this, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
            adapterView.adapter = adapter
        }
    }

    fun fillSpinnerSize(data: ArrayList<CatalogInterface>?, adapterView: AdapterView<ArrayAdapter<CatalogInterface>>){
        if(data != null){
            data.add(0, CatalogInterface(0, resources!!.getString(R.string.selectOptionDropdown)))
            val adapter = ArrayAdapter<CatalogInterface>(this, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
            adapterView.adapter = adapter
        }
    }

    fun getCatalogItemPosition(id: Long?, arr: ArrayList<ProductCatalog>): Int {
        for (position in 0 until arr.size)
            if (arr?.get(position)?.productCatalogId?.toLong() == id)
                return position
        return 0
    }

    fun getSizeItemPosition(id: Long?): Int {
        for (position in 0 until sizes!!.size)
            if (sizes!![position].productCatalogId?.toLong() == id)
                return position
        return 0
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
