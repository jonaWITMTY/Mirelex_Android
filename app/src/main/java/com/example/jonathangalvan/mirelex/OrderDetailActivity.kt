package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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

                            val orderFolio = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailOrderInfo, false)
                            orderFolio.detailInfoNameView.text = resources.getString(R.string.folio)
                            orderFolio.detailInfoValueView.text = orderInfo.orderInformation.folio
                            detailOrderInfo.addView(orderFolio)

                            val orderType = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailOrderInfo, false)
                            orderType.detailInfoNameView.text = resources.getString(R.string.type)
                            orderType.detailInfoValueView.text = orderInfo.orderInformation.orderType
                            detailOrderInfo.addView(orderType)

                            val orderStartDate = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailOrderInfo, false)
                            orderStartDate.detailInfoNameView.text = resources.getString(R.string.date)
                            orderStartDate.detailInfoValueView.text = orderInfo.orderInformation.startDate
                            detailOrderInfo.addView(orderStartDate)

                            val orderStatus = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailOrderInfo, false)
                            orderStatus.detailInfoNameView.text = resources.getString(R.string.status)
                            orderStatus.detailInfoValueView.text = orderInfo.orderInformation.orderStatus
                            detailOrderInfo.addView(orderStatus)

                            val orderTotal = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailOrderInfo, false)
                            orderTotal.detailInfoNameView.text = resources.getString(R.string.total)
                            orderTotal.detailInfoValueView.text = orderInfo.orderInformation.totalFormatted
                            detailOrderInfo.addView(orderTotal)
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
