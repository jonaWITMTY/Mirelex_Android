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
import com.example.jonathangalvan.mirelex.Requests.SizesRequest
import com.thomashaertel.widget.MultiSpinner
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


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
        getProductCatalogs()
    }

    fun doFilterProducts(query: String?){
        println("********")
        println(query)
    }

    fun getProductCatalogs(){
        var productTypeId = ""
        when(SessionModel(this).getUser().person?.userGenderId){
            Gender.Male.genderId -> {
                productTypeId = ProductType.Suit.productTypeId
            }
            Gender.Female.genderId -> {
                productTypeId = ProductType.Dress.productTypeId
            }
        }
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val productCatalogsObj = UtilsModel.getGson().toJson(GetProductCatalogsRequest(productTypeId))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( this, resources.getString(R.string.getProducCatalogs), productCatalogsObj)).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val productCatalogObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), ProductCatalogs::class.java)
                    catalogs = productCatalogObj
                    runOnUiThread {
                        run{
                            getSizes()
                        }
                    }
                }
            }
        })
    }

    fun getSizes(){
        var productTypeId = ""
        when(SessionModel(this).getUser().person?.userGenderId){
            Gender.Male.genderId -> {
                productTypeId = ProductType.Suit.productTypeId
            }
            Gender.Female.genderId -> {
                productTypeId = ProductType.Dress.productTypeId
            }
        }
        val sizesRequest = UtilsModel.getGson().toJson(SizesRequest(productTypeId.toInt()))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( this, resources.getString(R.string.userSizes), sizesRequest)).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val sizesObj = UtilsModel.getGson().fromJson(responseStr, CatalogArrayInterface::class.java)
                    sizes = sizesObj.data
                    runOnUiThread {
                        run{
                            fillProductsCatalogs()
                        }
                    }
                }
            }
        })
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
