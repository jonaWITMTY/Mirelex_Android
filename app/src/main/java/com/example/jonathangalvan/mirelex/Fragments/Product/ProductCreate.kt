package com.example.jonathangalvan.mirelex.Fragments.Product

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Interfaces.*
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ProductActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.GetProductCatalogs
import com.example.jonathangalvan.mirelex.Requests.GetProductPricesRequest
import com.example.jonathangalvan.mirelex.ViewModels.ProductViewModel
import com.rey.material.widget.Spinner
import kotlinx.android.synthetic.main.fragment_product_create.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class ProductCreate : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    var productId: String = ""
    var catalogs: ProductCatalogs? = null
    var productTypes: ArrayList<ProductTypeInterface>? = null
    var multiSpinnerView: MultiSpinnerSearch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_create, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Get product viewmodel*/
        val viewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)

        /*Fill fields*/
        (activity as ProductActivity).setActionBarTitle(resources.getString(R.string.newProduct))
        getProductTypes()

        /*On price checkbox click*/
        createProductSellable.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                createProductSellablePrice.visibility = View.VISIBLE
                viewModel.productObjRequest.sellable = "1"
            }else{
                createProductSellablePrice.visibility = View.GONE
                viewModel.productObjRequest.sellable = "0"
            }
        }

        createProductLeasable.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                createProductLeaseablePrice.visibility = View.VISIBLE
                viewModel.productObjRequest.leaseable = "1"
            }else{
                createProductLeaseablePrice.visibility = View.GONE
                viewModel.productObjRequest.leaseable = "0"
            }
        }

        createProductIsStretch.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                viewModel.productObjRequest.isStretch = "1"
            }else{
                viewModel.productObjRequest.isStretch = "0"
            }
        }

        /*Change catalogs deppending on productype*/
        createProductCategory.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getProductCatalogs(productTypes!![position].productTypeId.toString())
                when(productTypes!![position].productTypeId.toString()){
                    ProductType.Dress.productTypeId -> {
                    }
                    else -> {
                    }
                }
            }
        }

        /*On type price and hide timesUsedField*/
        createProductUsedTimes.visibility = View.GONE

        createProductPrice.editText?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getProductPrices()
            }
        })

        createProductCondition.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getProductPrices()
                if(catalogs!!.conditions[createProductCondition.selectedItemPosition].productCatalogId == "1"){
                    createProductUsedTimes.visibility = View.GONE
                }else{
                    createProductUsedTimes.visibility = View.VISIBLE
                }
            }
        }

        createProductUsedTimes.editText?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getProductPrices()
            }
        })

        /*On press continue button*/
        createProductContinue.setOnClickListener(View.OnClickListener {
            if(inputValidations()){

                /*Fill update product request*/
                viewModel.productObjRequest.brand = createProductBrand.editText?.text.toString()
                viewModel.productObjRequest.productTypeId = productTypes!![createProductCategory.selectedItemPosition].productTypeId.toString()
                viewModel.productObjRequest.originalPrice = createProductPrice.editText?.text.toString()
                viewModel.productObjRequest.productConditionId = catalogs!!.conditions[createProductCondition.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.sellPrice = createProductSellablePrice.editText?.text.toString()
                viewModel.productObjRequest.price = createProductLeaseablePrice.editText?.text.toString()
                viewModel.productObjRequest.description = createProductDescription.editText?.text.toString()
                viewModel.productObjRequest.productStyleId = catalogs!!.styles[createProductStyle.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productMaterialId = catalogs!!.materials[createProductMaterial.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productOccasionId= catalogs!!.occasions[createProductOcation.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productSleeveStyleId = catalogs!!.sleeveStyles[createProductSleeveStyle.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productColors = multiSpinnerView?.selectedIds

                /*Fill fields depending on category type*/
                when(productTypes!![createProductCategory.selectedItemPosition].productTypeId.toString()){
                    ProductType.Dress.productTypeId -> {
                        viewModel.productObjRequest.productDecorationId = catalogs!!.decorations[createProductDecoration.selectedItemPosition].productCatalogId
                        viewModel.productObjRequest.productLengthId = catalogs!!.lengths[createProductLength.selectedItemPosition].productCatalogId
                        viewModel.productObjRequest.productSilhouetteId = catalogs!!.silhouettes[createProductSilouete.selectedItemPosition].productCatalogId
                        viewModel.productObjRequest.bust = createProductBust.editText?.text.toString()
                        viewModel.productObjRequest.waist = createProductWaist.editText?.text.toString()
                        viewModel.productObjRequest.hip = createProductHip.editText?.text.toString()
                        viewModel.productObjRequest.height = createProductHeight.editText?.text.toString()
                    }
                    else -> {

                    }
                }

                (activity as ProductActivity).openTab(ProductImagePicker(), "productImagePicker")

            }else{
                val text = resources.getText(R.string.fillRequiredFields)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(activity, text, duration).show()
            }
        })
    }

    fun inputValidations(): Boolean{
        var isCorrect = true
        if(
            createProductBrand.editText?.text.toString()!!.isEmpty() ||
            createProductPrice.editText?.text.toString()!!.isEmpty() ||
            createProductColors.selectedIds.size == 0
        ){
            isCorrect = false
        }else{
            when(productTypes!![createProductCategory.selectedItemPosition].productTypeId.toString()){
                ProductType.Dress.productTypeId -> {
                    if(
                        createProductBust.editText?.text.toString()!!.isEmpty() ||
                        createProductWaist.editText?.text.toString()!!.isEmpty() ||
                        createProductHip.editText?.text.toString()!!.isEmpty() ||
                        createProductHeight.editText?.text.toString()!!.isEmpty() ||
                        createProductDescription.editText?.text.toString()!!.isEmpty()
                    ){
                        isCorrect = false
                    }
                }
                else -> {

                }
            }
        }
        return isCorrect
    }

    fun getProductPrices(){
        if(createProductPrice.editText?.text.toString().isNotEmpty()){
            val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
            val getProductPricesObj = UtilsModel.getGson().toJson(GetProductPricesRequest(
                createProductPrice.editText?.text.toString(),
                createProductUsedTimes.editText?.text.toString(),
                catalogs!!.conditions[createProductCondition.selectedItemPosition]?.productCatalogId
            ))
            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, resources.getString(R.string.getProductPrices), getProductPricesObj)).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity!!.findViewById(R.id.view_progressbar))}}
                }

                override fun onResponse(call: Call, response: Response) {
                    activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity!!.findViewById(R.id.view_progressbar))}}
                    val responseStr = response.body()?.string()
                    val responseObj = UtilsModel.getPostResponse(responseStr)
                    if(responseObj.status == "success"){
                        val productPrices = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), GetProductPricesInterface::class.java)
                        activity?.runOnUiThread {
                            run {
                                createProductLeaseablePrice.editText?.setText(productPrices.totalRent)
                                createProductSellablePrice.editText?.setText(productPrices.totalSell)
                            }
                        }
                    }
                }
            })
        }
    }

    fun getProductTypes(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( activity!!, resources.getString(R.string.getProductTypes))).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity!!.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity!!.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val productTypesobj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ProductTypes::class.java)
                    productTypes = productTypesobj.data
                    activity?.runOnUiThread {
                        run {
                            fillProductTypesSpinner(productTypes, activity!!.findViewById(R.id.createProductCategory))
                        }
                    }
                }
            }
        })
    }

    fun getProductCatalogs(productTypeId: String = "1"){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
        val productCatalogsObj = UtilsModel.getGson().toJson(GetProductCatalogs(productTypeId))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( activity!!, resources.getString(R.string.getProducCatalogs), productCatalogsObj)).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity!!.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val productCatalogObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), ProductCatalogs::class.java)
                    catalogs = productCatalogObj
                    activity?.runOnUiThread {
                        run{
                            fillProductsCatalogs()
                        }
                    }
                }
            }
        })
    }

    fun fillProductsCatalogs(){
        createProductLeasable.isChecked = true
        createProductSellable.isChecked = true
        createProductIsStretch.isChecked = true
        fillSpinner(catalogs?.conditions, activity!!.findViewById(R.id.createProductCondition))
        fillSpinner(catalogs?.styles, activity!!.findViewById(R.id.createProductStyle))
        fillSpinner(catalogs?.decorations, activity!!.findViewById(R.id.createProductDecoration))
        fillSpinner(catalogs?.lengths, activity!!.findViewById(R.id.createProductLength))
        fillSpinner(catalogs?.materials, activity!!.findViewById(R.id.createProductMaterial))
        fillSpinner(catalogs?.silhouettes, activity!!.findViewById(R.id.createProductSilouete))
        fillSpinner(catalogs?.sleeveStyles, activity!!.findViewById(R.id.createProductSleeveStyle))
        fillSpinner(catalogs?.occasions, activity!!.findViewById(R.id.createProductOcation))

        var colorArr: ArrayList<KeyPairBoolData> = ArrayList()
        for(color in catalogs?.colors!!){
            val colorObj = KeyPairBoolData()
            colorObj.id = color.productCatalogId!!.toLong()
            colorObj.name = color.name
            colorArr?.add(colorObj)
        }

        multiSpinnerView = activity!!.findViewById<MultiSpinnerSearch>(R.id.createProductColors)
        multiSpinnerView!!.setItems(colorArr, -1) { items -> }
        multiSpinnerView!!.setLimit(5, MultiSpinnerSearch.LimitExceedListener {})
    }

    fun fillSpinner(data: ArrayList<ProductCatalog>?, adapterView: AdapterView<ArrayAdapter<ProductCatalog>>){
        if(data != null){
            val adapter = ArrayAdapter<ProductCatalog>(activity!!, R.layout.view_spinner_item_black, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black)
            adapterView.adapter = adapter
        }
    }

    fun fillProductTypesSpinner(data: ArrayList<ProductTypeInterface>?, adapterView: AdapterView<ArrayAdapter<ProductTypeInterface>>){
        if(data != null){
            val adapter = ArrayAdapter<ProductTypeInterface>(activity!!, R.layout.view_spinner_item_black, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black)
            adapterView.adapter = adapter
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            ProductCreate().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
