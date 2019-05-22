package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Interfaces.OrderInterface
import com.example.jonathangalvan.mirelex.Interfaces.OrderProductInfo
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.GetOrderInfoRequest
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.view_detail_info_row_with_title.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class OrderDetailActivity : AppCompatActivity() {
    var orderInfoForBundle: OrderProductInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Getorder info*/
        val bundleFromOrders = intent.extras
        val orderId = bundleFromOrders.getString("orderId")

        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val orderInfoObj = GetOrderInfoRequest(orderId)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getOrderInfo), UtilsModel.getGson().toJson(orderInfoObj))).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val orderInfo = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), OrderProductInfo::class.java)
                    orderInfoForBundle = orderInfo
                    runOnUiThread {
                        run {
                            supportActionBar?.title = orderInfo.orderInformation.folio
                            Picasso.with(this@OrderDetailActivity).load(orderInfo.orderProducts!![0].productFeaturedImage).into(detailOrderFeaturedImage)
                            Picasso.with(this@OrderDetailActivity).load(orderInfo.orderOwnerInformation.profilePictureUrl).into(detailOrderOwnerImage)
                            Picasso.with(this@OrderDetailActivity).load(orderInfo.orderProducts!![0].productFeaturedImage).into(detailOrderProductImage)
                            if(orderInfo.orderOwnerInformation.companyName == null){
                                detailOrderOwnerName.text = "${orderInfo.orderOwnerInformation.firstName} ${orderInfo.orderOwnerInformation.paternalLastName}"
                            }else{
                                detailOrderOwnerName.text = orderInfo.orderOwnerInformation.companyName
                            }
                            detailOrderProductName.text = orderInfo.orderProducts!![0].brand

                            detailOrderGoToProduct.setOnClickListener(View.OnClickListener {
                                val goToProductDetail = Intent(this@OrderDetailActivity, ProductDetailActivity::class.java)
                                val b = Bundle()
                                b.putString("productId", orderInfo.orderProducts!![0].productId)
                                goToProductDetail.putExtras(b)
                                startActivity(goToProductDetail)
                            })

                            addRow(resources.getString(R.string.folio), orderInfo.orderInformation.folio!!)
                            addRow(resources.getString(R.string.type), orderInfo.orderInformation.orderType!!)
                            addRow(resources.getString(R.string.date), orderInfo.orderInformation.startDate!!)
                            addRow(resources.getString(R.string.status), orderInfo.orderInformation.orderStatus!!)
                            addRow(resources.getString(R.string.total), orderInfo.orderInformation.totalFormatted!!)
                        }
                    }
                }
            }
        })

        /*Go to store detail*/
        detailOrderGoToStoreProfile.setOnClickListener(View.OnClickListener {
            val goToStoreDetailShort = Intent(this, StoreDetailShortActivity::class.java)
            val bundleToStoreDetailShort = Bundle()
            bundleToStoreDetailShort.putString("ownerObj",UtilsModel.getGson().toJson(orderInfoForBundle?.orderOwnerInformation))
            goToStoreDetailShort.putExtras(bundleToStoreDetailShort)
            startActivity(goToStoreDetailShort)
        })
    }

    fun addRow(title: String, value: String){
        val insertRow = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailOrderInfo, false)
        insertRow.detailInfoNameView.text = title
        insertRow.detailInfoValueView.text = value
        detailOrderInfo.addView(insertRow)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
