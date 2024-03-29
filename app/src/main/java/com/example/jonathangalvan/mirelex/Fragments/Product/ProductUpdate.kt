package com.example.jonathangalvan.mirelex.Fragments.Product

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Adapters.SliderPagerAdapter
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalog
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalogs
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ProductActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.DeleteProductRequest
import com.example.jonathangalvan.mirelex.Requests.GetProductCatalogsRequest
import com.example.jonathangalvan.mirelex.Requests.GetProductInfoRequest
import com.example.jonathangalvan.mirelex.UI.MultiSpinnerCustom
import com.example.jonathangalvan.mirelex.ViewModels.ProductViewModel
import kotlinx.android.synthetic.main.fragment_product_update.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import com.thomashaertel.widget.MultiSpinner


class ProductUpdate : androidx.fragment.app.Fragment()  {

    private var listener: OnFragmentInteractionListener? = null
    var productId: String = ""
    var productObj: ProductInfoInterface? = null
    var catalogs: ProductCatalogs? = null
    var imgPreview: String? = null
    var spinner: MultiSpinner? = null
    var spinnerDecorations: MultiSpinner? = null
    var adapter: ArrayAdapter<String>? = null
    var adapterDecorations: ArrayAdapter<String>? = null
    var selectedIds: ArrayList<Long> = ArrayList()
    var selectedDecorationsIds: ArrayList<Long> = ArrayList()
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

        /*Hide field depending on usertype*/
        when(SessionModel(activity!!).getUser().person?.userTypeId){
            UserType.Customer.userTypeId ->{
                updateProductMaterialLayout.visibility = View.GONE
            }
        }

        /*Clicks measures "?"*/
        imagePreviewHeight.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.heightImage)).show(fragmentManager!!, "alertDialog")
        })

        imagePreviewBust.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.bustImage)).show(fragmentManager!!, "alertDialog")
        })

        imagePreviewWaist.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.waistImage)).show(fragmentManager!!, "alertDialog")
        })

        imagePreviewHip.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.hipImage)).show(fragmentManager!!, "alertDialog")
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
                viewModel.productObjRequest.productOccasionId= catalogs!!.occasions[updateProductOcation.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productSleeveStyleId = catalogs!!.sleeveStyles[updateProductSleeveStyle.selectedItemPosition].productCatalogId
                viewModel.productObjRequest.productColors = selectedIds

                if(UserType.Customer.userTypeId != SessionModel(activity!!).getUser().person?.userTypeId){
                    viewModel.productObjRequest.productMaterialId = catalogs!!.materials[updateProductMaterial.selectedItemPosition].productCatalogId
                }

                /*Fill fields depending on category type*/
                when(productObj?.productInformation?.productTypeId){
                    ProductType.Dress.productTypeId -> {
                        viewModel.productObjRequest.productDecorations = selectedDecorationsIds
                        viewModel.productObjRequest.productLengthId = catalogs!!.lengths[updateProductLength.selectedItemPosition].productCatalogId
                        viewModel.productObjRequest.productSilhouetteId = catalogs!!.silhouettes[updateProductSilouete.selectedItemPosition].productCatalogId
                        viewModel.productObjRequest.bust = updateProductBust.editText?.text.toString()
                        viewModel.productObjRequest.waist = updateProductWaist.editText?.text.toString()
                        viewModel.productObjRequest.hip = updateProductHip.editText?.text.toString()
                        viewModel.productObjRequest.height = updateProductHeight.editText?.text.toString()
                    }
                    else -> {
                        viewModel.productObjRequest.sizeId = catalogs!!.sizes[updateProductSize.selectedItemPosition].productCatalogId
                    }
                }

                (activity as ProductActivity).openTab(ProductImagePicker(), "productImagePicker")

            }else{
                val text = "${resources.getText(R.string.fillRequiredFields)}  ${validateMissingErrorFields()}"
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(activity, text, duration).show()
            }
        })

        /*On delete product*/
        updateProductDeleteProduct.setOnClickListener(View.OnClickListener {
            val loader = layoutInflater.inflate(R.layout.view_progressbar, activity!!.findViewById(android.R.id.content), true)
            val objReq = UtilsModel.getGson().toJson(DeleteProductRequest(productId))
            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, resources.getString(R.string.deleteProduct), objReq)).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    activity?.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                    UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity!!.supportFragmentManager,"alertDialog")
                }

                override fun onResponse(call: Call, response: Response) {
                    activity?.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                    val responseStr = response.body()?.string()
                    val response = UtilsModel.getPostResponse(activity!!, responseStr)
                    if(response.status == "success"){
                        UtilsModel.getAlertView().newInstance(responseStr, 1, 1).show(activity!!.supportFragmentManager,"alertDialog")
                    }else{
                        UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity!!.supportFragmentManager,"alertDialog")
                    }
                }
            })
        })
    }

    fun inputValidations(): Boolean{
        var isCorrect = true
        if(
            updateProductBrand.editText?.text.toString()!!.isEmpty() ||
            updateProductPrice.editText?.text.toString()!!.isEmpty() ||
            catalogs == null ||
            catalogs!!.conditions[updateProductCondition.selectedItemPosition].productCatalogId.toString().isEmpty() ||
            catalogs!!.styles[updateProductStyle.selectedItemPosition].productCatalogId.toString().isEmpty() ||
            selectedIds.size < 1 ||
            catalogs!!.sleeveStyles[updateProductSleeveStyle.selectedItemPosition].productCatalogId.toString().isEmpty() ||
            catalogs!!.occasions[updateProductOcation.selectedItemPosition].productCatalogId.toString().isEmpty() ||
            updateProductDescription.editText?.text.toString()!!.isEmpty()
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
                        selectedDecorationsIds.size < 1 ||
                        catalogs!!.lengths[updateProductLength.selectedItemPosition].productCatalogId.toString().isEmpty() ||
                        catalogs!!.silhouettes[updateProductSilouete.selectedItemPosition].productCatalogId.toString().isEmpty()

                    ){
                        isCorrect = false
                    }
                }
                else -> {
                    if(catalogs!!.sizes[updateProductSize.selectedItemPosition].productCatalogId.toString().isEmpty()){
                        isCorrect = false
                    }
                }
            }
        }
        return isCorrect
    }

    private fun validateMissingErrorFields(): String{
        var errorText = "\n"

        if(updateProductBrand.editText?.text.toString()!!.isEmpty() ){
            errorText += "\n ${activity?.resources?.getString(R.string.brand)} ${activity?.resources?.getString(R.string.isRequired)}"
        }

        if(updateProductPrice.editText?.text.toString()!!.isEmpty() ){
            errorText += "\n ${activity?.resources?.getString(R.string.price)} ${activity?.resources?.getString(R.string.isRequired)}"
        }

        if(catalogs == null || catalogs!!.conditions[updateProductCondition.selectedItemPosition].productCatalogId.toString().isEmpty()){
            errorText += "\n ${activity?.resources?.getString(R.string.condition)} ${activity?.resources?.getString(R.string.isRequired)}"
        }

        if(catalogs == null || catalogs!!.styles[updateProductStyle.selectedItemPosition].productCatalogId.toString().isEmpty()){
            errorText += "\n ${activity?.resources?.getString(R.string.style)} ${activity?.resources?.getString(R.string.isRequired)}"
        }

        if(selectedIds.size <1){
            errorText += "\n ${activity?.resources?.getString(R.string.colors)} ${activity?.resources?.getString(R.string.isRequired)}"
        }

        if(catalogs == null || catalogs!!.sleeveStyles[updateProductSleeveStyle.selectedItemPosition].productCatalogId.toString().isEmpty()){
            errorText += "\n ${activity?.resources?.getString(R.string.sleeveStyle)} ${activity?.resources?.getString(R.string.isRequired)}"
        }

        if(catalogs == null || catalogs!!.occasions[updateProductOcation.selectedItemPosition].productCatalogId.toString().isEmpty()){
            errorText += "\n ${activity?.resources?.getString(R.string.ocation)} ${activity?.resources?.getString(R.string.isRequired)}"
        }

        when(productObj?.productInformation?.productTypeId){
            ProductType.Dress.productTypeId -> {

                if(selectedDecorationsIds.size < 1){
                    errorText += "\n ${activity?.resources?.getString(R.string.decoration)} ${activity?.resources?.getString(R.string.isRequired)}"
                }

                if(catalogs == null || catalogs!!.lengths[updateProductLength.selectedItemPosition].productCatalogId.toString().isEmpty()){
                    errorText += "\n ${activity?.resources?.getString(R.string.length)} ${activity?.resources?.getString(R.string.isRequired)}"
                }

                if(catalogs == null || catalogs!!.silhouettes[updateProductSilouete.selectedItemPosition].productCatalogId.toString().isEmpty()){
                    errorText += "\n ${activity?.resources?.getString(R.string.dressSilhouette)} ${activity?.resources?.getString(R.string.isRequired)}"
                }

                if(updateProductBust.editText?.text.toString()!!.isEmpty()){
                    errorText += "\n ${activity?.resources?.getString(R.string.bust)} ${activity?.resources?.getString(R.string.isRequired)}"
                }

                if(updateProductWaist.editText?.text.toString()!!.isEmpty()){
                    errorText += "\n ${activity?.resources?.getString(R.string.waist)} ${activity?.resources?.getString(R.string.isRequired)}"
                }

                if(updateProductHip.editText?.text.toString()!!.isEmpty()){
                    errorText += "\n ${activity?.resources?.getString(R.string.hip)} ${activity?.resources?.getString(R.string.isRequired)}"
                }

                if(updateProductHeight.editText?.text.toString()!!.isEmpty()){
                    errorText += "\n ${activity?.resources?.getString(R.string.heightInCms)} ${activity?.resources?.getString(R.string.isRequired)}"
                }
            }
            else -> {
                if(catalogs == null || catalogs!!.sizes[updateProductSize.selectedItemPosition].productCatalogId.toString().isEmpty()){
                    errorText += "\n ${activity?.resources?.getString(R.string.size)} ${activity?.resources?.getString(R.string.isRequired)}"
                }
            }
        }

        if(updateProductDescription.editText?.text.toString()!!.isEmpty()){
            errorText += "\n ${activity?.resources?.getString(R.string.description)} ${activity?.resources?.getString(R.string.isRequired)}"
        }

        return errorText
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

        /*Set image for image preview*/
        imgPreview = productObj?.productInformation?.productFeaturedImage.toString()

        /*Slider Gallery*/
        var images: ArrayList<String> = ArrayList()
        if(productObj?.productInformation?.productFeaturedImage != null){
            images.add(productObj?.productInformation?.productFeaturedImage.toString())
        }
        for(productImage in productObj!!.productImages){
            images.add(productImage.imageUrl.toString())
        }
        val sliderAdapter = SliderPagerAdapter(images, activity?.supportFragmentManager)
        updateProductImageSlider.adapter = sliderAdapter
        indicator.setViewPager(updateProductImageSlider)

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

        fillCommonProductCatalogs()

        when(productObj?.productInformation?.productTypeId){
            ProductType.Dress.productTypeId -> {
                (activity as ProductActivity).setActionBarTitle("${productObj?.productInformation?.brand} - ${productObj?.productInformation?.productStyle}")

                /*Fill dress fields*/
                updateProductBust.editText?.setText(productObj!!.productInformation.bust)
                updateProductWaist.editText?.setText(productObj!!.productInformation.waist)
                updateProductHip.editText?.setText(productObj!!.productInformation.hip)
                updateProductHeight.editText?.setText(productObj!!.productInformation.height)
                updateProducSizeDress.editText?.setText(productObj!!.productInformation.size)

                /*Fill women products catalogs*/
                fillWomanProductsCatalogs()

                /*Hide*/
                updateProductSizeLayout.visibility = View.GONE

                /*Show*/
                updateProductBustLayout.visibility = View.VISIBLE
                updateProductWaistLayout.visibility = View.VISIBLE
                updateProductHipLayout.visibility = View.VISIBLE
                updateProductHeightLayout.visibility = View.VISIBLE
                updateProductDecorationLayout.visibility = View.VISIBLE
                updateProductLengthLayout.visibility = View.VISIBLE
                updateProductSiloueteLayout.visibility = View.VISIBLE
                updateProductSizeDressLayout.visibility = View.VISIBLE
            }
            else -> {
                (activity as ProductActivity).setActionBarTitle("${productObj?.productInformation?.brand}")

                /*Fill men products catalogs*/
                fillMenProductsCatalogs()

                /*Hide*/
                updateProductBustLayout.visibility = View.GONE
                updateProductWaistLayout.visibility = View.GONE
                updateProductHipLayout.visibility = View.GONE
                updateProductHeightLayout.visibility = View.GONE
                updateProductDecorationLayout.visibility = View.GONE
                updateProductLengthLayout.visibility = View.GONE
                updateProductSiloueteLayout.visibility = View.GONE

                /*Hide*/
                updateProductSizeDressLayout.visibility = View.GONE

                /*Show*/
                updateProductSizeLayout.visibility = View.VISIBLE
            }
        }
    }

    fun fillCommonProductCatalogs(){

        fillSpinner(catalogs?.conditions, activity?.findViewById(R.id.updateProductCondition))
        updateProductCondition.setSelection(getAdapterItemPosition( productObj?.productInformation?.productConditionId?.toLong(), catalogs?.conditions))

        fillSpinner(catalogs?.styles, activity?.findViewById(R.id.updateProductStyle))
        updateProductStyle.setSelection(getAdapterItemPosition( productObj?.productInformation?.productStyleId?.toLong(), catalogs?.styles))

        fillSpinner(catalogs?.materials, activity?.findViewById(R.id.updateProductMaterial))
        updateProductMaterial.setSelection(getAdapterItemPosition( productObj?.productInformation?.productMaterialId?.toLong(), catalogs?.materials))

        fillSpinner(catalogs?.sleeveStyles, activity?.findViewById(R.id.updateProductSleeveStyle))
        updateProductSleeveStyle.setSelection(getAdapterItemPosition( productObj?.productInformation?.productSleeveStyleId?.toLong(), catalogs?.sleeveStyles))

        fillSpinner(catalogs?.occasions, activity?.findViewById(R.id.updateProductOcation))
        updateProductOcation.setSelection(getAdapterItemPosition( productObj?.productInformation?.productOccasionId?.toLong(), catalogs?.occasions))

        /*Get colors*/
        var selectedItems = BooleanArray(catalogs?.colors!!.size)
        for((index, color) in catalogs?.colors!!.withIndex()){
            if(checkIfColorExist(color.productColorCatalogId!!.toLong())){
                selectedItems[index] = true
                selectedIds.add(catalogs?.colors!![index].productColorCatalogId!!.toLong())
            }else{
                selectedItems[index] = false
            }
        }

        /*Fill spinner with colors*/
        val multiSpinner = activity!!.findViewById<MultiSpinnerCustom>(R.id.spinnerMulti)
        multiSpinner.setItems("", "colors", UtilsModel.getGson().toJson(catalogs), onSelectedListener, selectedItems)
    }

    fun fillWomanProductsCatalogs(){
//        fillSpinner(catalogs?.decorations, activity?.findViewById(R.id.updateProductDecoration))
//        updateProductDecoration.setSelection(getAdapterItemPosition( productObj?.productInformation?.productDecorationId?.toLong(), catalogs?.decorations))

        fillSpinner(catalogs?.lengths, activity?.findViewById(R.id.updateProductLength))
        updateProductLength.setSelection(getAdapterItemPosition( productObj?.productInformation?.productLengthId?.toLong(), catalogs?.lengths))

        fillSpinner(catalogs?.silhouettes, activity?.findViewById(R.id.updateProductSilouete))
        updateProductSilouete.setSelection(getAdapterItemPosition( productObj?.productInformation?.productSilhouetteId?.toLong(), catalogs?.silhouettes))

        /*Get decorations*/
        var selectedDecorationsItems = BooleanArray(catalogs?.decorations!!.size)
        adapterDecorations =  ArrayAdapter<String>(activity!!, android.R.layout.simple_spinner_item)
        for((index, decoration) in catalogs?.decorations!!.withIndex()){
            adapterDecorations!!.add(decoration.name)
            if(checkIfDecorationExist(decoration.productCatalogId!!.toLong())){
                selectedDecorationsItems[index] = true
                selectedDecorationsIds.add(catalogs?.decorations!![index].productCatalogId!!.toLong())
            }else{
                selectedDecorationsItems[index] = false
            }
        }

        /*Fill spinner with decorations*/
        spinnerDecorations = activity!!.findViewById(R.id.updateProductDecoration)
        spinnerDecorations!!.setAdapter(adapterDecorations, false, onSelectedDecorationListener)

        /*set selected decorations*/
        spinnerDecorations!!.selected = selectedDecorationsItems
    }

    fun fillMenProductsCatalogs(){
        fillSpinner(catalogs?.sizes, activity?.findViewById(R.id.updateProductSize))
        updateProductSize.setSelection(getAdapterItemPosition( productObj?.productInformation?.sizeId?.toLong(), catalogs?.sizes))
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

    private val onSelectedDecorationListener = MultiSpinner.MultiSpinnerListener {
        selectedDecorationsIds = ArrayList()
        for ((index, value) in it.withIndex()){
            if(value){
                selectedDecorationsIds.add(catalogs?.decorations!![index].productCatalogId!!.toLong())
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

    fun checkIfDecorationExist(decorationId: Long): Boolean{
        var isInDecorationArr = false
        if(!isInDecorationArr){
            for(decoration in productObj?.productDecorations!!){
                if(decoration.productDecorationId?.toLong() == decorationId){
                    isInDecorationArr = true
                }
            }
        }
        return isInDecorationArr
    }
}
