package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Interfaces.OrderTotal
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreateOrderRequest
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order_checkout.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class OrderCheckoutActivity : AppCompatActivity() {

    private var orderRequestObj: CreateOrderRequest? = null
    private var productObj: ProductInfoInterface? = null

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
            }
        }
        orderCheckoutDate.text = orderRequestObj?.startDate
        getOrderTotal()

        /*Create order event*/
        orderCheckoutOrderProduct.setOnClickListener(View.OnClickListener {
            val loader = layoutInflater.inflate(R.layout.view_progressbar,findViewById(android.R.id.content), true)
            if(orderCheckoutTerms.isChecked){
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
            }else{
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorMissingTerms(), 1, 0).show(supportFragmentManager, "alertView")
            }
        })
    }

    fun confirmBtnCallback(){
       startActivity(Intent(this, CustomerTabsActivity::class.java))
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
