package com.example.jonathangalvan.mirelex

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Interfaces.*
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreateOrderRequest
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order_checkout.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class OrderCheckoutActivity : AppCompatActivity() {

    private var orderRequestObj: CreateOrderRequest? = null
    private var productObj: ProductInfoInterface? = null
    private var sessionUser: UserInterface? = null
    private var defaultCard: PaymentCard? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_checkout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.orderDetails)

        /*Get bundle information*/
        val bundleFromCalendar = intent.extras
        productObj = UtilsModel.getGson().fromJson(bundleFromCalendar.getString("productInfo"), ProductInfoInterface::class.java)
        orderRequestObj = UtilsModel.getGson().fromJson(bundleFromCalendar.getString("orderRequestObj"), CreateOrderRequest::class.java)

        /*Fill fields*/
        Picasso.with(this).load(productObj?.productInformation?.productFeaturedImage).into(orderCheckoutFeatureImage)
        orderCheckoutBrand.text = productObj?.productInformation?.brand
        orderCheckoutSize.text = productObj?.productInformation?.size
        orderCheckoutColors.text = productObj?.productInformation?.brand
        orderCheckoutMaterial.text = productObj?.productInformation?.productMaterial
        orderCheckoutDecoration.text = productObj?.productInformation?.productDecoration
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
                orderCheckoutOrderProduct.text = resources.getString(R.string.sellable)
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

        /*Create order event*/
        orderCheckoutOrderProduct.setOnClickListener(View.OnClickListener {
            val loader = layoutInflater.inflate(R.layout.view_progressbar,findViewById(android.R.id.content), true)
            if(orderCheckoutTerms.isChecked && defaultCard != null){
                orderRequestObj!!.clientDelivery = orderCheckoutDelivery.isChecked
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
                val responseObj = UtilsModel.getPostResponse(responseStr)
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
                val responseObj = UtilsModel.getPostResponse(responseStr)
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
                val responseUserObj = UtilsModel.getGson().fromJson(response.body()!!.string(), ResponseInterface::class.java)
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
