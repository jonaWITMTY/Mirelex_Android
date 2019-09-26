package com.example.jonathangalvan.mirelex.Fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.*
import com.example.jonathangalvan.mirelex.Adapters.ProductsAdapter
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.FilterProducts
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomBottomAlert
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ProductDetailActivity
import com.example.jonathangalvan.mirelex.ProductActivity
import com.example.jonathangalvan.mirelex.R
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import com.onesignal.OneSignal
import android.app.Activity
import android.support.v4.app.FragmentManager
import android.widget.FrameLayout
import com.example.jonathangalvan.mirelex.Enums.Gender
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomAlert
import com.example.jonathangalvan.mirelex.Interfaces.*
import com.example.jonathangalvan.mirelex.Requests.*


class Products : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    var filterAction = false
    var productAdapter: ProductsAdapter? = null
    var catalogs: ProductCatalogs? = null
    var sizes: String = ""
    var user: UserInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*Confirm phone*/
//        user = SessionModel(activity!!).getUser()
//        if(user?.person?.phoneVerified == "0" || user?.person?.phoneVerified == null){
//            val ba = UtilsModel.getGson().toJson(BottomAlertInterface(
//                alertType = "confirmAccountPhone"
//            ))
//            val alert = CustomBottomAlert().bottomSheetDialogInstance(ba)
//            alert.isCancelable = false
//            alert.show(activity!!.supportFragmentManager, "alert")
//        }

        /*Save onesignal id*/
        OneSignal.idsAvailable { userId, registrationId ->
            if(userId != null){
                val oneSignalObj = UtilsModel.getGson().toJson(SetOneaSignalIdRequest(
                    userId
                ))
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, resources.getString(R.string.setOneSignalId), oneSignalObj)).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) {}
                })
            }
        }

        /*Get filter objs*/
        getProductCatalogs()
        getSizes()
    }

    override fun onResume() {
        super.onResume()
        if(!filterAction){
            getProducts()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1) {
            if (resultCode === Activity.RESULT_OK) {
                val filterProduct = data?.getStringExtra("filterProductRequest")
                filterAction = true
                getProducts(filterProduct)
            }
        }
    }

    fun getProducts(filterProduct: String? = ""){
        var requestStr = ""
        var isClient = "0"
        if(user?.person?.userTypeId == UserType.Customer.userTypeId){
            isClient = "1"
        }
        if(filterAction){
            var filterProductObj = UtilsModel.getGson().fromJson(filterProduct, ProductFilterRequest::class.java)
            filterProductObj.isClient = isClient
            requestStr = UtilsModel.getGson().toJson(filterProductObj)
        }else{
            requestStr = UtilsModel.getGson().toJson(CustomerProductRequest(
                user?.characteristics?.sizeId,
                isClient
            ))
        }
        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!.applicationContext, activity!!.resources.getString(R.string.getProducts), requestStr)).enqueue( object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(activity, responseStr)
                val responseProducts: ProductsInterface
                when(responseObj.status){
                    "success" -> {
                        responseProducts = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ProductsInterface::class.java)
                        activity?.runOnUiThread {
                            if(activity!!.findViewById<ViewGroup>(R.id.viewCenteredMessage) != null) {
                                activity?.findViewById<ViewGroup>(R.id.contentTabsFrameLayout)?.removeView(activity?.findViewById(R.id.viewCenteredMessage))
                            }
                            productAdapter = ProductsAdapter(activity!!, responseProducts.data)
                            productsGrid?.adapter = productAdapter
                            productsGrid?.setOnItemClickListener { parent, view, position, id ->
                                val goToProductDetail: Intent
                                when(SessionModel(activity!!).getSessionUserType()){
                                    UserType.Store.userTypeId -> {
                                        goToProductDetail = Intent(activity!!, ProductActivity::class.java)
                                    }
                                    else -> {
                                        goToProductDetail = Intent(activity!!, ProductDetailActivity::class.java)
                                    }
                                }
                                val b = Bundle()
                                b.putString("productId", (productsGrid.adapter.getItem(position) as ProductInterface).productId.toString())
                                goToProductDetail.putExtras(b)
                                startActivity(goToProductDetail)
                            }
                        }
                    }
                    "noDataAvailable" -> {
                        activity?.runOnUiThread {
                            run {
                                productAdapter = ProductsAdapter(activity!!, ArrayList())
                                productsGrid?.adapter = productAdapter
                                if((activity!!.findViewById<ViewGroup>(R.id.viewCenteredMessage)) == null) {
                                    val ceneteredLayout = layoutInflater.inflate(
                                        R.layout.view_centered_message,
                                        activity!!.findViewById(R.id.contentTabsFrameLayout),
                                        true
                                    )
                                    ceneteredLayout.centeredMessage.text = responseObj.desc
                                }
                            }
                        }
                    }
                    else -> {
                        UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                    }
                }
            }
        })
    }

    fun getProductCatalogs(){
        var productTypeId = ""

        if(user?.person?.userGenderId.isNullOrEmpty()){
            productTypeId = ProductType.Dress.productTypeId
        }else{
            when(user?.person?.userGenderId){
                Gender.Male.genderId -> {
                    productTypeId = ProductType.Suit.productTypeId
                }
                Gender.Female.genderId -> {
                    productTypeId = ProductType.Dress.productTypeId
                }
            }
        }

        val productCatalogsObj = UtilsModel.getGson().toJson(GetProductCatalogsRequest(productTypeId))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( activity!!, resources.getString(R.string.getProducCatalogs), productCatalogsObj)).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(activity!!, responseStr)
                if(responseObj.status == "success"){
                    val productCatalogObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), ProductCatalogs::class.java)
                    catalogs = productCatalogObj
                }
            }
        })
    }

    fun getSizes(){
        var productTypeId = ""

        if(user?.person?.userGenderId.isNullOrEmpty()){
            productTypeId = ProductType.Dress.productTypeId
        }else{
            when(user?.person?.userGenderId){
                Gender.Male.genderId -> {
                    productTypeId = ProductType.Suit.productTypeId
                }
                Gender.Female.genderId -> {
                    productTypeId = ProductType.Dress.productTypeId
                }
            }
        }

        val sizesRequest = UtilsModel.getGson().toJson(SizesRequest(productTypeId.toInt()))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( activity!!, resources.getString(R.string.userSizes), sizesRequest)).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(activity!!, responseStr)
                if(responseObj.status == "success"){
                    sizes = responseStr!!
                }
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        /*Store tabs icons*/
        menu?.findItem(R.id.storeTabsFilterIcon)?.isVisible = false

        /*Clients tabs icons*/
        menu?.findItem(R.id.customerTabsAddProductIcon)?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.storeTabsAddIcon ->{
                when(user?.person?.userTypeId){
                    UserType.Store.userTypeId ->{
                        if(user?.person?.authorized == "1"){
                            startActivity(Intent(activity!!, ProductActivity::class.java))
                        }else{
                            CustomAlert().newInstance(
                                "{'status':'success', 'title':'${resources.getString(R.string.pendingApprovalAlertTitle)}', 'desc':'${resources.getString(R.string.pendingApprovalAlertDesc)}'}",
                                1,
                                0)
                                .show(activity?.supportFragmentManager,"alertDialog")
                        }
                    }
                    UserType.Customer.userTypeId ->{
                        startActivity(Intent(activity!!, ProductActivity::class.java))
                    }
                }
            }
            R.id.customerTabsFilterIcon -> {
                val goToFilterProducts = Intent(activity!!, FilterProducts::class.java)
                val b = Bundle()
                b.putString("catalogs", UtilsModel.getGson().toJson(catalogs))
                b.putString("sizes", sizes)
                goToFilterProducts.putExtras(b)
                startActivityForResult(goToFilterProducts, 1)
            }
        }
        return super.onOptionsItemSelected(item)
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

    override fun onDestroyView() {
        super.onDestroyView()
        SessionModel.deleteSessionValue(activity!!, "filterProductRequest")
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
