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
import com.example.jonathangalvan.mirelex.Interfaces.BottomAlertInterface
import com.example.jonathangalvan.mirelex.Interfaces.ProductInterface
import com.example.jonathangalvan.mirelex.Interfaces.ProductsInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ProductDetailActivity
import com.example.jonathangalvan.mirelex.ProductActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.CustomerProductRequest
import com.example.jonathangalvan.mirelex.Requests.SetOneaSignalIdRequest
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import com.onesignal.OneSignal
import android.app.Activity
import android.widget.FrameLayout
import com.example.jonathangalvan.mirelex.Requests.ProductFilterRequest


class Products : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    var filterAction = false
    var productAdapter: ProductsAdapter? = null

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
        val user = SessionModel(activity!!).getUser()
        if(user.person?.phoneVerified == "0" || user.person?.phoneVerified == null){
            val ba = UtilsModel.getGson().toJson(BottomAlertInterface(
                alertType = "confirmAccountPhone"
            ))
            val alert = CustomBottomAlert().bottomSheetDialogInstance(ba)
            alert.isCancelable = false
            alert.show(activity!!.supportFragmentManager, "alert")
        }

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
    }

    override fun onResume() {
        super.onResume()
        if(!filterAction){
            getProducts()
        }else{
            filterAction = false
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
        val user = SessionModel(activity!!).getUser()
        var isClient = "0"
        if(user.person?.userTypeId == UserType.Customer.userTypeId){
            isClient = "1"
        }
        if(filterAction){
            var filterProductObj = UtilsModel.getGson().fromJson(filterProduct, ProductFilterRequest::class.java)
            filterProductObj.isClient = isClient
            requestStr = UtilsModel.getGson().toJson(filterProductObj)
        }else{
            requestStr = UtilsModel.getGson().toJson(CustomerProductRequest(
                user.characteristics?.sizeId,
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
                val responseObj = UtilsModel.getPostResponse(responseStr)
                val responseProducts: ProductsInterface
                when(responseObj.status){
                    "success" -> {
                        responseProducts = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ProductsInterface::class.java)
                        activity?.runOnUiThread {
                            if(activity!!.findViewById<ViewGroup>(R.id.viewCenteredMessage) != null) {
                                activity?.findViewById<ViewGroup>(R.id.customerTabsFrameLayout)?.removeView(activity?.findViewById(R.id.viewCenteredMessage))
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
                                        activity!!.findViewById(R.id.customerTabsFrameLayout),
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

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        /*Store tabs icons*/
        menu?.findItem(R.id.storeTabsFilterIcon)?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.storeTabsAddIcon ->{
                startActivity(Intent(activity!!, ProductActivity::class.java))
            }
            R.id.customerTabsFilterIcon -> {
                startActivityForResult(Intent(activity!!, FilterProducts::class.java), 1)
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

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
