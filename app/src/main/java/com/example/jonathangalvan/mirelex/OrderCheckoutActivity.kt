package com.example.jonathangalvan.mirelex

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Fragments.Utils.SelectItems
import com.example.jonathangalvan.mirelex.Interfaces.*
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Listeners.SelectedItemsListener
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreateOrderRequest
import com.example.jonathangalvan.mirelex.Requests.GetMirelexStores
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order_checkout.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class OrderCheckoutActivity : AppCompatActivity(), SelectItems.OnFragmentInteractionListener, SelectedItemsListener.SelectedItemsListenerInterface {

    private var orderRequestObj: CreateOrderRequest? = null
    private var productObj: ProductInfoInterface? = null
    private var sessionUser: UserInterface? = null
    private var defaultCard: PaymentCard? = null
    private var storeList: String = ""
    private var states: StatesInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_checkout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.orderDetails)

//      Hide general values
        orderCheckoutStoreTitle.visibility = View.GONE

        /*Get bundle information*/
        val bundleFromCalendar = intent.extras
        productObj = UtilsModel.getGson().fromJson(bundleFromCalendar.getString("productInfo"), ProductInfoInterface::class.java)
        orderRequestObj = UtilsModel.getGson().fromJson(bundleFromCalendar.getString("orderRequestObj"), CreateOrderRequest::class.java)

        /*Fill fields*/
//        Picasso.with(this).load(productObj?.productInformation?.productFeaturedImage).into(orderCheckoutFeatureImage)
        if(!productObj?.productInformation?.productFeaturedImage.isNullOrEmpty()){
            Glide.with(this).load(productObj?.productInformation?.productFeaturedImage).apply( RequestOptions().override(800, 0)).into(orderCheckoutFeatureImage)
        }
        orderCheckoutBrand.text = productObj?.productInformation?.brand
        orderCheckoutSize.text = productObj?.productInformation?.size
        orderCheckoutColors.text = productObj?.productInformation?.brand
        orderCheckoutMaterial.text = productObj?.productInformation?.productMaterial
        var decorations: String = ""
        productObj?.productDecorations?.forEach {
            decorations += " ${it.decoration},"
        }
        orderCheckoutDecoration.text = decorations.dropLast(1)
        orderCheckoutLength.text = productObj?.productInformation?.productLength
        var colors: String = ""
        productObj?.productColors?.forEach {
            colors += " ${it.color},"
        }
        orderCheckoutColors.text = colors.dropLast(1)

        when(orderRequestObj?.orderType){
            OrderType.Purchase.orderTypeId -> {
                orderCheckoutOrderType.text = resources.getString(R.string.sell)
                orderCheckoutTotal.text = "Total: ${productObj?.productInformation?.sellPriceFormatted}"
                orderCheckoutOrderProduct.text = resources.getString(R.string.toBuy)
                orderRequestObj?.total = productObj?.productInformation?.sellPrice.toString()
            }
            OrderType.Lease.orderTypeId -> {
                orderCheckoutOrderType.text = resources.getString(R.string.leaseable)
                orderCheckoutTotal.text = "Total: ${productObj?.productInformation?.priceFormatted}"
                orderRequestObj?.total = productObj?.productInformation?.price.toString()
            }
            OrderType.Fitting.orderTypeId -> {
                orderCheckoutOrderType.text = resources.getString(R.string.fitting)
                orderCheckoutTotal.text = "Total: $0.00"
                orderCheckoutPaymentCard.visibility= View.GONE
            }
        }
        orderCheckoutDate.text = orderRequestObj?.startDate
        getOrderTotal()

        /*PaymentsCard*/
        paymentCardButtonAction()

        /*Hide delivery and Payments*/
        orderCheckoutDelivery.visibility = View.GONE
//        orderCheckoutPaymentCard.visibility = View.GONE

        /*Create order event*/
        orderCheckoutOrderProduct.setOnClickListener(View.OnClickListener {
            val loader = layoutInflater.inflate(R.layout.view_progressbar,findViewById(android.R.id.content), true)

            /*Hide delivery and Payments - conditional*/
            if(orderCheckoutTerms.isChecked && defaultCard != null){

            /*Hide delivery and Payments - Conditional without delivery and payments*/
//            if(orderCheckoutTerms.isChecked){
                orderRequestObj!!.clientDelivery = orderCheckoutDelivery.isChecked

                if(productObj?.productOwner?.person?.userTypeId == UserType.Customer.userTypeId){
                    if(orderRequestObj!!.addressId.isNullOrEmpty()){
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                        val text = resources.getText(R.string.fillRequiredFields)
                        val duration = Toast.LENGTH_SHORT
                        Toast.makeText(this, text, duration).show()
                    }else{
                        /*Hide delivery and Payments - without delivery and payments*/
//                        createOrderRequest()
                    }
                }else{
                    /*Hide delivery and Payments - without delivery and payments*/
//                    createOrderRequest()
                }

                /*Hide delivery and Payments - switch*/
                when(orderRequestObj?.orderType){
                    OrderType.Lease.orderTypeId, OrderType.Purchase.orderTypeId -> {
                        if(defaultCard != null){
                            orderRequestObj!!.cardId = defaultCard?.cardId
                            createOrderRequest()
                        }else{
                            val text = resources.getText(R.string.fillRequiredFields)
                            val duration = Toast.LENGTH_SHORT
                            Toast.makeText(this, text, duration).show()
                        }
                    }
                    OrderType.Fitting.orderTypeId -> {
                        createOrderRequest()
                    }
                }
            }else{
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorMissingTerms(), 1, 0).show(supportFragmentManager, "alertView")
            }
        })

        /*Select store for customer - customer order*/
        if(
            productObj?.productOwner?.person?.userTypeId == UserType.Customer.userTypeId ||
            (productObj?.productOwner?.person?.userTypeId == UserType.Store.userTypeId && productObj?.productOwner?.person?.isMirelexStore == "0")
        ){
            if(sessionUser?.address!!.size > 0){
                val getStoresObj = UtilsModel.getGson().toJson(GetMirelexStores(
                    sessionUser?.address!![0].stateId.toString()
                ))

                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getStores), getStoresObj)).enqueue( object: Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    override fun onResponse(call: Call, response: Response) {
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getPostResponse(this@OrderCheckoutActivity, responseStr)
                        if(responseObj.status == "success"){
                            storeList = responseStr!!
                        }
                    }
                })

                orderCheckoutStoreSelection.setOnClickListener(View.OnClickListener {
                    val sil = SelectedItemsListener(this, supportFragmentManager)
                    showSupportActionBar(false)
                    sil.openTab(SelectItems(), "storesList", "storesList", storeList)
                })
            }else{
                /*Fill states spinner*/
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getStates))).enqueue(object: Callback{
                    override fun onFailure(call: Call, e: IOException) {}

                    override fun onResponse(call: Call, response: Response) {
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getPostResponse(this@OrderCheckoutActivity, responseStr)
                        when(responseObj.status){
                            "success" -> {
                                states = UtilsModel.getGson().fromJson(responseStr, StatesInterface::class.java)

                                val adapterStates = ArrayAdapter<StateInterface>(this@OrderCheckoutActivity, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, states?.data)
                                adapterStates.setDropDownViewResource(R.layout.view_spinner_item_black_select)
                                runOnUiThread{
                                    run {
                                        orderCheckoutStateSelection.adapter = adapterStates
                                    }
                                }
                            }
                        }
                    }
                })

                /*When click store event*/
                orderCheckoutStoreSelection.setOnClickListener(View.OnClickListener {
                    orderCheckoutStateSelection.performClick()
                })

                /*When selecting state*/
                var firstTime = false
                orderCheckoutStateSelection.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if(firstTime){
                            val getStoresObj = UtilsModel.getGson().toJson(GetMirelexStores(
                                states?.data!![position].catalogId
                            ))

                            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@OrderCheckoutActivity, resources.getString(R.string.getStores), getStoresObj)).enqueue( object: Callback {
                                override fun onFailure(call: Call, e: IOException) {}

                                override fun onResponse(call: Call, response: Response) {
                                    val responseStr = response.body()?.string()
                                    val responseObj = UtilsModel.getPostResponse(this@OrderCheckoutActivity, responseStr)
                                    if(responseObj.status == "success"){
                                        storeList = responseStr!!
                                        val sil = SelectedItemsListener(this@OrderCheckoutActivity, supportFragmentManager)
                                        runOnUiThread { run {
                                            showSupportActionBar(false)
                                            sil.openTab(SelectItems(), "storesList", "storesList", storeList)
                                        }}
                                    }
                                }
                            })
                        }else{
                            firstTime = true
                        }
                    }
                }
            }
        }else{
            orderCheckoutStoreSelection.visibility = View.GONE
        }

        /*Click event to go to terms and conditions*/
        orderCheckoutTermsLink.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, WebviewActivity::class.java))
        })

    }

    override fun onResume() {
        super.onResume()
        updateUser()
    }

    fun createOrderRequest(){
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.createOrder), UtilsModel.getGson().toJson(orderRequestObj))).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@OrderCheckoutActivity, responseStr)
                if(responseObj.status == "success"){
                    val newAlert = UtilsModel.getAlertView().newInstance(responseStr, 4, 0)
                    newAlert.show(supportFragmentManager, "alertView")
                }else{
                    val newAlert = UtilsModel.getAlertView().newInstance(responseStr, 1, 0)
                    newAlert.show(supportFragmentManager, "alertView")
                }
            }
        })
    }

    fun getOrderTotal(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar,findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getOrderTotal), UtilsModel.getGson().toJson(orderRequestObj))).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@OrderCheckoutActivity, responseStr)
                if(responseObj.status == "success"){
                    val totalObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), OrderTotal::class.java)
                    orderRequestObj?.total = totalObj.total
                    runOnUiThread {
                        run {
                            orderCheckoutTotal.text = "Total: ${totalObj.totalFormatted}"
                        }
                    }
                }
            }
        })
    }

    fun paymentCardButtonAction(){
        sessionUser = SessionModel(this).getUser()
        getDefaultCard()
        if(sessionUser?.paymentCards!!.isNotEmpty()){
            orderCheckoutPaymentCard.text = "${defaultCard?.brand} - ${defaultCard?.lastDigits}"
            orderCheckoutPaymentCard.setOnClickListener(null)
        }else{
            orderCheckoutPaymentCard.text = resources.getString(R.string.addPaymentCard)
            orderCheckoutPaymentCard.setOnClickListener(View.OnClickListener {
                val goToServiceCreate = Intent(this, PaymentCardCreateActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(this,  R.anim.abc_slide_in_bottom,  R.anim.abc_fade_out)
                startActivity(goToServiceCreate, options.toBundle())
            })
        }
    }

    fun getDefaultCard(){
        for (card in sessionUser!!.paymentCards){
            if(card.default == "1"){
                defaultCard = card
            }
        }
    }

    fun updateUser(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this,resources.getString(R.string.getUserInfo))).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseUserObj = UtilsModel.getPostResponse(this@OrderCheckoutActivity, response.body()!!.string())
                if(responseUserObj.status == "success"){
                    SessionModel.saveSessionValue(this@OrderCheckoutActivity, "user", UtilsModel.getGson().toJson(responseUserObj.data!![0]))
                    val user = SessionModel(this@OrderCheckoutActivity).getUser()
                    sessionUser = user
                    runOnUiThread {
                        run {
                            paymentCardButtonAction()
                        }
                    }
                }
            }
        })
    }

    fun confirmBtnCallback(){
       startActivity(Intent(this, CustomerTabsActivity::class.java))
    }

    override fun onFragmentInteraction(uri: Uri) {}

    override fun callback(tag: String, inputName: String, id: String, inputText: String, obj: Any) {
        showSupportActionBar()
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
//            supportFragmentManager.beginTransaction().remove(fragment).commit()
            supportFragmentManager.popBackStack()
        }
        var address: AddressListInterface = obj as AddressListInterface
        orderCheckoutStoreSelection.text = inputText + " " +
                "\n${address.email.toString()} " +
                "\n${address.personalPhone.toString()}" +
                "\n\n${address.street}, ${address.neighborhood}, ${address.postalCode}, ${address.city}, ${address.state}, ${address.country}"

        orderCheckoutStoreTitle.visibility = View.VISIBLE
        orderCheckoutStoreSelection.setTypeface(null, Typeface.NORMAL)
        orderRequestObj?.addressId = id
    }

    override fun callbackClose(tag: String){
        showSupportActionBar()
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
//            supportFragmentManager.beginTransaction().remove(fragment).commit()
            supportFragmentManager.popBackStack()
        }
    }

    fun showSupportActionBar(sab: Boolean = true){
        if(sab){
            supportActionBar?.show()
        }else{
            supportActionBar?.hide()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
