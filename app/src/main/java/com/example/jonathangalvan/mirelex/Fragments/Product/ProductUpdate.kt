package com.example.jonathangalvan.mirelex.Fragments.Product

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Adapters.SliderPagerAdapter
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalog
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalogs
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ProductActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.GetProductCatalogsRequest
import com.example.jonathangalvan.mirelex.Requests.GetProductInfoRequest
import com.example.jonathangalvan.mirelex.ViewModels.ProductViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_product_update.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import com.thomashaertel.widget.MultiSpinner


class ProductUpdate : Fragment()  {

    private var listener: OnFragmentInteractionListener? = null
    var productId: String = ""
    var productObj: ProductInfoInterface? = null
    var catalogs: ProductCatalogs? = null
    var imgPreview: String? = null
    var spinner: MultiSpinner? = null
    var adapter: ArrayAdapter<String>? = null
    var selectedIds: ArrayList<Long> = ArrayList()
    var viewModel: ProductViewModel = ProductViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_update, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            ProductUpdate().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Get product viewmodel*/
        viewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        viewModel.productProcessType = "update"
        productId = viewModel.productId

        /*Get product info*/
        getProductInfo()

        /*On price checkbox click*/
        updateProductSellable.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                updateProductSellablePrice.visibility = View.VISIBLE
                viewModel.productObjRequest.sellable = "1"
            }else{
                updateProductSellablePrice.visibility = View.GONE
                viewModel.productObjRequest.sellable = "0"
            }
        }

        updateProductLeasable.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                updateProductLeaseablePrice.visibility = View.VISIBLE
                viewModel.productObjRequest.leaseable = "1"
            }else{
                updateProductLeaseablePrice.visibility = View.GONE
                viewModel.productObjRequest.leaseable = "0"
            }
        }

        updateProductIsStretch.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                viewModel.productObjRequest.isStretch = "1"
            }else{
                viewModel.productObjRequest.isStretch = "0"
            }
        }

        /*Clicks measures "?"*/
        imagePreviewHeight.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.heightImage)).show(fragmentManager, "alertDialog")
        })

        imagePreviewBust.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.bustImage)).show(fragmentManager, "alertDialog")
        })

        imagePreviewWaist.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.waistImage)).show(fragmentManager, "alertDialog")
        })

        imagePreviewHip.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.hipImage)).show(fragmentManager, "alertDialog")
        })

        /*On press continue button*/
        updateProductContinue.setOnClickListener(View.OnClickListener {
            if(inputValidations()){

                /*Fill update product request*/
                viewModel.productObjRequest.productId = productObj?.productInformation?.productId.toString()
                viewModel.productObjRequest.brand = updateProductBrand.editText?.text.toString()
                viewModel.productObjRequest.productTypeId = productObj?.productInformation?.productTypeId
                viewModel.productObjRequest.originalPrice = productObj?.productInformation?.originalPrice
                viewModel.productObjRequest.productConditionId = catalogs!!.conditions[updateProductCondition.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.sellPrice = productObj?.productInformation?.sellPrice
                viewModel.productObjRequest.price = productObj?.productInformation?.price
                viewModel.productObjRequest.description = updateProductDescription.editText?.text.toString()
                viewModel.productObjRequest.productStyleId = catalogs!!.styles[updateProductStyle.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productMaterialId = catalogs!!.materials[updateProductMaterial.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productOccasionId= catalogs!!.occasions[updateProductOcation.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productSleeveStyleId = catalogs!!.sleeveStyles[updateProductSleeveStyle.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productColors = selectedIds

                /*Fill fields depending on category type*/
                when(productObj?.productInformation?.productTypeId){
                    ProductType.Dress.productTypeId -> {
                        viewModel.productObjRequest.productDecorationId = catalogs!!.decorations[updateProductDecoration.selectedItemPosition].productCatalogId
                        viewModel.productObjRequest.productLengthId = catalogs!!.lengths[updateProductLength.selectedItemPosition].productCatalogId
                        viewModel.productObjRequest.productSilhouetteId = catalogs!!.silhouettes[updateProductSilouete.selectedItemPosition].productCatalogId
                        viewModel.productObjRequest.bust = updateProductBust.editText?.text.toString()
                        viewModel.productObjRequest.waist = updateProductWaist.editText?.text.toString()
                        viewModel.productObjRequest.hip = updateProductHip.editText?.text.toString()
                        viewModel.productObjRequest.height = updateProductHeight.editText?.text.toString()
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
            updateProductBrand.editText?.text.toString()!!.isEmpty() ||
            selectedIds.size < 1
        ){
            isCorrect = false
        }else{
            when(productObj?.productInformation?.productTypeId){
                ProductType.Dress.productTypeId -> {
                    if(
                        updateProductBust.editText?.text.toString()!!.isEmpty() ||
                        updateProductWaist.editText?.text.toString()!!.isEmpty() ||
                        updateProductHip.editText?.text.toString()!!.isEmpty() ||
                        updateProductHeight.editText?.text.toString()!!.isEmpty() ||
                        updateProductDescription.editText?.text.toString()!!.isEmpty()
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

    fun getProductInfo(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity!!.findViewById(android.R.id.content), true)
        var getProductInfoObj = UtilsModel.getGson().toJson(GetProductInfoRequest(productId))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, resources.getString(R.string.getProductInfo), getProductInfoObj)).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity!!.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(activity, responseStr)
                if(responseObj.status == "success"){
                    productObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), ProductInfoInterface::class.java)
                    viewModel?.productObj = productObj
                        activity?.runOnUiThread {
                        run {
                            getProductCatalogs()
                        }
                    }
                }
            }
        })
    }

    fun getProductCatalogs(){
        val productCatalogsObj = UtilsModel.getGson().toJson(GetProductCatalogsRequest(productObj?.productInformation?.productTypeId.toString()))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( activity!!, resources.getString(R.string.getProducCatalogs), productCatalogsObj)).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity!!.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(activity, responseStr)
                if(responseObj.status == "success"){
                    val productCatalogObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), ProductCatalogs::class.java)
                    catalogs = productCatalogObj
                    activity?.runOnUiThread {
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

        /*Slider Gallery*/
        var images: ArrayList<String> = ArrayList()
        if(imgPreview != null){
            images.add(productObj?.productInformation?.productFeaturedImage.toString())
        }
        for(productImage in productObj!!.productImages){
            images.add(productImage.imageUrl.toString())
        }
        val sliderAdapter = SliderPagerAdapter(images, activity?.supportFragmentManager)
        updateProductImageSlider.adapter = sliderAdapter

        /*Fill common product fields*/
//        Picasso.with(activity!!).load(productObj?.productInformation?.productFeaturedImage).into(updateProductImageView)
        updateProductBrand.editText?.setText(productObj!!.productInformation.brand)
        updateProductCategory.editText?.setText(productObj!!.productInformation.productType)
        updateProductPrice.editText?.setText(productObj!!.productInformation.originalPrice)
        updateProductDescription.editText?.setText(productObj!!.productInformation.description)
        updateProductLeaseablePrice.editText?.setText(productObj!!.productInformation.price)
        updateProductSellablePrice.editText?.setText(productObj!!.productInformation.sellPrice)
        if(productObj?.productInformation?.leaseable == "1"){
            updateProductLeasable.isChecked = true
        }else{
            updateProductLeaseablePrice.visibility = View.GONE
        }

        if(productObj?.productInformation?.sellable == "1"){
            updateProductSellable.isChecked = true
        }else{
            updateProductSellablePrice.visibility = View.GONE
        }

        if(productObj?.productInformation?.isStretch == "1"){
            updateProductIsStretch.isChecked = true
        }


        when(productObj?.productInformation?.productTypeId){
            ProductType.Dress.productTypeId -> {
                (activity as ProductActivity).setActionBarTitle("${productObj?.productInformation?.brand} - ${productObj?.productInformation?.productStyle}")

                /*Fill dress fields*/
                updateProductBust.editText?.setText(productObj!!.productInformation.bust)
                updateProductWaist.editText?.setText(productObj!!.productInformation.waist)
                updateProductHip.editText?.setText(productObj!!.productInformation.hip)
                updateProductHeight.editText?.setText(productObj!!.productInformation.height)
            }
            else -> {
                (activity as ProductActivity).setActionBarTitle("${productObj?.productInformation?.brand}")
            }
        }
    }

    fun fillProductsCatalogs(){
        fillSpinner(catalogs?.conditions, activity?.findViewById(R.id.updateProductCondition))
        updateProductCondition.setSelection(getAdapterItemPosition( productObj?.productInformation?.productConditionId?.toLong(), catalogs?.conditions))

        fillSpinner(catalogs?.styles, activity?.findViewById(R.id.updateProductStyle))
        updateProductStyle.setSelection(getAdapterItemPosition( productObj?.productInformation?.productStyleId?.toLong(), catalogs?.styles))

        fillSpinner(catalogs?.decorations, activity?.findViewById(R.id.updateProductDecoration))
        updateProductDecoration.setSelection(getAdapterItemPosition( productObj?.productInformation?.productDecorationId?.toLong(), catalogs?.decorations))

        fillSpinner(catalogs?.lengths, activity?.findViewById(R.id.updateProductLength))
        updateProductLength.setSelection(getAdapterItemPosition( productObj?.productInformation?.productLengthId?.toLong(), catalogs?.lengths))

        fillSpinner(catalogs?.materials, activity?.findViewById(R.id.updateProductMaterial))
        updateProductMaterial.setSelection(getAdapterItemPosition( productObj?.productInformation?.productMaterialId?.toLong(), catalogs?.materials))

        fillSpinner(catalogs?.silhouettes, activity?.findViewById(R.id.updateProductSilouete))
        updateProductSilouete.setSelection(getAdapterItemPosition( productObj?.productInformation?.productSilhouetteId?.toLong(), catalogs?.silhouettes))

        fillSpinner(catalogs?.sleeveStyles, activity?.findViewById(R.id.updateProductSleeveStyle))
        updateProductSleeveStyle.setSelection(getAdapterItemPosition( productObj?.productInformation?.productSleeveStyleId?.toLong(), catalogs?.sleeveStyles))

        fillSpinner(catalogs?.occasions, activity?.findViewById(R.id.updateProductOcation))
        updateProductOcation.setSelection(getAdapterItemPosition( productObj?.productInformation?.productOccasionId?.toLong(), catalogs?.occasions))

        /*Get colors*/
        var selectedItems = BooleanArray(catalogs?.colors!!.size)
        adapter =  ArrayAdapter<String>(activity!!, android.R.layout.simple_spinner_item)
        for((index, color) in catalogs?.colors!!.withIndex()){
            adapter!!.add(color.name)
            if(checkIfColorExist(color.productCatalogId!!.toLong())){
                selectedItems[index] = true
                selectedIds.add(catalogs?.colors!![index].productCatalogId!!.toLong())
            }else{
                selectedItems[index] = false
            }
        }

        /*Fill spinner with colors*/
        spinner = activity!!.findViewById(R.id.spinnerMulti)
        spinner!!.setAdapter(adapter, false, onSelectedListener)

       /*set selected colors*/
        spinner!!.setSelected(selectedItems)

    }

    private val onSelectedListener = MultiSpinner.MultiSpinnerListener {
        selectedIds = ArrayList()
        for ((index, value) in it.withIndex()){
            if(value){
                selectedIds.add(catalogs?.colors!![index].productCatalogId!!.toLong())
            }
        }
    }

    fun fillSpinner(data: ArrayList<ProductCatalog>?, adapterView: AdapterView<ArrayAdapter<ProductCatalog>>?){
        if(data != null){
            val adapter = ArrayAdapter<ProductCatalog>(activity!!, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
            adapterView?.adapter = adapter
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

    fun checkIfColorExist(colorId: Long): Boolean{
        var isInColorArr = false
        if(!isInColorArr){
            for(color in productObj?.productColors!!){
                if(color.productColorId?.toLong() == colorId){
                    isInColorArr = true
                }
            }
        }
        return isInColorArr
    }
}
