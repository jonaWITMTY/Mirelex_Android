package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Interfaces.OrderInterface
import com.example.jonathangalvan.mirelex.Interfaces.OrderProductInfo
import com.example.jonathangalvan.mirelex.Models.SessionModel
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

                            /*Fill order and product info*/
                            Picasso.with(this@OrderDetailActivity).load(orderInfo.orderProducts!![0].productFeaturedImage).into(detailOrderFeaturedImage)
                            Picasso.with(this@OrderDetailActivity).load(orderInfo.orderProducts!![0].productFeaturedImage).into(detailOrderProductImage)
                            detailOrderProductName.text = orderInfo.orderProducts!![0].brand

                            /*Set data depending on session user*/
                            var pictureUrl = ""
                            var name = ""
                            var json = ""
                            when(SessionModel(this@OrderDetailActivity).getSessionUserType()){
                                UserType.Store.userTypeId -> {
                                    pictureUrl = orderInfo.orderClientInformation.profilePictureUrl.toString()
                                    name = "${orderInfo.orderClientInformation.firstName} ${orderInfo.orderClientInformation.paternalLastName}"
                                    json = UtilsModel.getGson().toJson(orderInfo.orderClientInformation)
                                }
                                else -> {
                                    pictureUrl = orderInfo.orderOwnerInformation.profilePictureUrl.toString()
                                    name = orderInfo.orderOwnerInformation.companyName.toString()
                                    json = UtilsModel.getGson().toJson(orderInfo.orderOwnerInformation)
                                }
                            }
                            Picasso.with(this@OrderDetailActivity).load(pictureUrl).into(detailOrderOwnerImage)
                            detailOrderOwnerName.text = name

                            /*Fill order info*/
                            addRow(resources.getString(R.string.folio), orderInfo.orderInformation.folio!!)
                            addRow(resources.getString(R.string.type), orderInfo.orderInformation.orderType!!)
                            addRow(resources.getString(R.string.date), orderInfo.orderInformation.startDate!!)
                            addRow(resources.getString(R.string.status), orderInfo.orderInformation.orderStatus!!)
                            addRow(resources.getString(R.string.total), orderInfo.orderInformation.totalFormatted!!)

                            /*Go to detail product*/
                            detailOrderGoToProduct.setOnClickListener(View.OnClickListener {
                                val goToProductDetail = Intent(this@OrderDetailActivity, ProductDetailActivity::class.java)
                                val b = Bundle()
                                b.putString("productId", orderInfo.orderProducts!![0].productId)
                                goToProductDetail.putExtras(b)
                                startActivity(goToProductDetail)
                            })

                            /*Go to store detail*/
                            detailOrderGoToStoreProfile.setOnClickListener(View.OnClickListener {
                                val goToStoreDetailShort = Intent(this@OrderDetailActivity, StoreDetailShortActivity::class.java)
                                val bundleToStoreDetailShort = Bundle()
                                bundleToStoreDetailShort.putString("personObj", json)
                                goToStoreDetailShort.putExtras(bundleToStoreDetailShort)
                                startActivity(goToStoreDetailShort)
                            })
                        }
                    }
                }
            }
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
