package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Enums.OrderStatus
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.OrderProductInfo
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.AcceptRejectOrderRequest
import com.example.jonathangalvan.mirelex.Requests.GetOrderInfoRequest
import com.example.jonathangalvan.mirelex.Requests.UpdateOrderStatusRequest
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.view_detail_info_row_with_title.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class OrderDetailActivity : AppCompatActivity() {
    var orderInfoForBundle: OrderProductInfo? = null
    var orderFutureStatus = ""
    var inputValue = ""
    var displayForm = false
    var orderId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Getorder info*/
        val bundleFromOrders = intent.extras
        orderId = bundleFromOrders.getString("orderId")
        getOrderInfo()

        /*Hide action button and form*/
        detailOrderActionButton.visibility = View.GONE
        detailOrderActionAceptReject.visibility = View.GONE

    }

    fun getOrderInfo(){
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

                            /*Event for action button*/
                            val sessionUser = SessionModel(this@OrderDetailActivity).getUser()
                            when(sessionUser.person?.userId){
                                orderInfoForBundle!!.orderOwnerInformation.userId -> {
                                    if(
                                        (orderInfoForBundle?.orderInformation?.acceptedDate == null && orderInfoForBundle?.orderInformation?.rejectedDate == null) ||
                                        orderInfoForBundle?.orderInformation?.orderStatusId == OrderStatus.Open.orderStatusId
                                    ){
                                        detailOrderActionAceptReject.visibility = View.VISIBLE

                                        detailOrderActionAceptOrder.setOnClickListener(View.OnClickListener {
                                            aceptOrder()
                                        })

                                        detailOrderActionRejectOrder.setOnClickListener(View.OnClickListener {
                                            aceptOrder(false)
                                        })
                                    }else{
                                        when(orderInfoForBundle!!.orderInformation.orderTypeId){
                                            OrderType.Fitting.orderTypeId -> {
                                                orderStatusFittingProcess()
                                            }
                                            OrderType.Lease.orderTypeId, OrderType.Purchase.orderTypeId -> {
                                                orderStatusLeasePurchaseProcess()
                                            }
                                        }

                                        /*Show or hide status form*/
                                        if(displayForm){
                                            detailOrderActionButton.visibility = View.VISIBLE
                                            detailOrderActionButton.text = inputValue
                                            detailOrderActionButton.setOnClickListener(View.OnClickListener {
                                                changeOrderStatus()
                                            })
                                        }
                                    }
                                }
                                else -> {
                                    getOrderActionStatusClient()
                                }
                            }

                            /*Fill order and product info*/
                            if(orderInfo.orderProducts!![0].productFeaturedImage != null){
                                Picasso.with(this@OrderDetailActivity).load(orderInfo.orderProducts!![0].productFeaturedImage).into(detailOrderFeaturedImage)
                                detailOrderFeaturedImage.setOnClickListener( View.OnClickListener {
                                    ImagePreview().newInstance(orderInfo.orderProducts!![0].productFeaturedImage).show(supportFragmentManager,"alertDialog")
                                })
                            }

                            if(orderInfo.orderProducts!![0].productFeaturedImage != null){
                                Picasso.with(this@OrderDetailActivity).load(orderInfo.orderProducts!![0].productFeaturedImage).into(detailOrderProductImage)
                            }
                            detailOrderProductName.text = orderInfo.orderProducts!![0].brand

                            /*Set data depending on session user*/
                            var pictureUrl = ""
                            var name = ""
                            var json = ""
                            var isThereImage: String? = null
                            when(SessionModel(this@OrderDetailActivity).getSessionUserType()){
                                UserType.Store.userTypeId -> {
                                    pictureUrl = orderInfo.orderClientInformation.profilePictureUrl.toString()
                                    name = "${orderInfo.orderClientInformation.firstName} ${orderInfo.orderClientInformation.paternalLastName}"
                                    json = UtilsModel.getGson().toJson(orderInfo.orderClientInformation)
                                    isThereImage = orderInfo.orderClientInformation.profilePictureUrl
                                }
                                else -> {
                                    pictureUrl = orderInfo.orderOwnerInformation.profilePictureUrl.toString()
                                    name = orderInfo.orderOwnerInformation.companyName.toString()
                                    json = UtilsModel.getGson().toJson(orderInfo.orderOwnerInformation)
                                    isThereImage = orderInfo.orderOwnerInformation.profilePictureUrl
                                }
                            }
                            if(isThereImage != null){
                                Picasso.with(this@OrderDetailActivity).load(pictureUrl).into(detailOrderOwnerImage)
                            }
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

    fun aceptOrder(isAcepted: Boolean = true){
        var endpoint = ""
        if(isAcepted){
            endpoint = resources.getString(R.string.acceptOrder)
        }else{
            endpoint = resources.getString(R.string.rejectOrder)
        }

        /*Accept denied order service*/
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val aceptOrderObj= UtilsModel.getGson().toJson(AcceptRejectOrderRequest(
            orderInfoForBundle?.orderInformation?.orderId
        ))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@OrderDetailActivity, endpoint, aceptOrderObj)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    finish()
                }else{
                    UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                }
            }
        })
    }

    fun changeOrderStatus(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val updateOrderStatus= UtilsModel.getGson().toJson(UpdateOrderStatusRequest(
            orderFutureStatus,
            orderInfoForBundle?.orderInformation?.orderId
        ))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@OrderDetailActivity, resources.getString(R.string.updateOrderStatus), updateOrderStatus)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    finish()
                }else{
                    UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                }
            }
        })
    }

    fun orderStatusFittingProcess(){
        var currentDeliveryStatusWay = 1
        var currentDeliveryStatusCount = 0
        var currentDeliveryStatusDifference = 0
        val sessionUserId = SessionModel(this).getUser().person?.userId

        for(update in orderInfoForBundle!!.orderUpdates){
            if(update.newOrderStatusId == "2"){
                currentDeliveryStatusCount++
            }
        }

        when("${orderInfoForBundle!!.orderInformation.ownerDelivery}${orderInfoForBundle!!.orderInformation.clientDelivery}"){
            "00", "10", "01" -> {
                val coutnDeliveryStatus = 2
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
            "11" -> {
                val coutnDeliveryStatus = 3
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
        }

        if(currentDeliveryStatusDifference == 0){
            currentDeliveryStatusWay = 0
        }

        if(orderInfoForBundle!!.orderOwnerInformation.isMirelexStore == "0"){
            if(orderInfoForBundle!!.orderOwnerInformation.userId == sessionUserId){
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.Gathering.orderStatusId, OrderStatus.DeliveringProcess.orderStatusId -> {
                        if (currentDeliveryStatusWay == 0){
                            orderFutureStatus = OrderStatus.Finished.orderStatusId
                            inputValue = "Finalizar"
                            displayForm = true
                        }
                    }
                }
            }else{
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.Processing.orderStatusId -> {
                        orderFutureStatus = OrderStatus.Gathering.orderStatusId
                        inputValue = "Listo para recolectar"
                        displayForm = true
                    }
                    OrderStatus.Gathering.orderStatusId, OrderStatus.DeliveringProcess.orderStatusId -> {
                        if (currentDeliveryStatusWay == 1 && currentDeliveryStatusDifference == 1) {
                            orderFutureStatus = OrderStatus.Processing.orderStatusId
                            inputValue = "Entregado en tienda"
                            displayForm = true
                        }
                    }
                }
            }
        }else{
            if (orderInfoForBundle!!.orderOwnerInformation.userId == sessionUserId) {
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.Processing.orderStatusId -> {
                        orderFutureStatus = OrderStatus.Finished.orderStatusId
                        inputValue = "Finalizar"
                        displayForm = true
                    }
                }
            }
        }
    }

    fun orderStatusLeasePurchaseProcess(){
        var currentDeliveryStatusWay = 1
        var currentDeliveryStatusCount = 0
        var currentDeliveryStatusDifference = 0
        val sessionUserId = SessionModel(this).getUser().person?.userId

        for(update in orderInfoForBundle!!.orderUpdates){
            if(update.newOrderStatusId == "2"){
                currentDeliveryStatusCount++
            }
        }

        when("${orderInfoForBundle!!.orderInformation.ownerDelivery}${orderInfoForBundle!!.orderInformation.clientDelivery}"){
            "00"-> {
                val coutnDeliveryStatus = 1
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
            "10", "01" -> {
                val coutnDeliveryStatus = 2
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
            "11" -> {
                val coutnDeliveryStatus = 3
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
        }

        if(currentDeliveryStatusDifference == 0){
            currentDeliveryStatusWay = 0
        }

        if(orderInfoForBundle!!.orderOwnerInformation.isMirelexStore == "0"){
            if(orderInfoForBundle!!.orderOwnerInformation.userId == sessionUserId){
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.Gathering.orderStatusId, OrderStatus.DeliveringProcess.orderStatusId -> {
                        if (currentDeliveryStatusWay == 0){
                            orderFutureStatus = OrderStatus.Finished.orderStatusId
                            inputValue = "Finalizar"
                            displayForm = true
                        }
                    }
                }
            }else{
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.Processing.orderStatusId -> {
                        if(currentDeliveryStatusWay == 1 && (currentDeliveryStatusDifference == 1 || currentDeliveryStatusDifference == 2)) {
                            orderFutureStatus = OrderStatus.Gathering.orderStatusId
                            inputValue = "Listo para recolectar"
                            displayForm = true
                        }
                    }
                    OrderStatus.Gathering.orderStatusId, OrderStatus.DeliveringProcess.orderStatusId -> {
                        if(currentDeliveryStatusWay == 1 && (currentDeliveryStatusDifference == 1 || currentDeliveryStatusDifference == 2)) {
                            orderFutureStatus = OrderStatus.Processing.orderStatusId
                            inputValue = "Entregado en tienda"
                            displayForm = true
                        }
                    }
                }
            }
        }else{
            if (orderInfoForBundle!!.orderOwnerInformation.userId == sessionUserId) {
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.Processing.orderStatusId -> {
                        if(
                            (currentDeliveryStatusWay == 1 && (currentDeliveryStatusDifference == 2)) ||
                            (currentDeliveryStatusWay == 1 && (currentDeliveryStatusDifference == 1))
                        ){
                            orderFutureStatus = OrderStatus.Gathering.orderStatusId
                            inputValue = "Listo para recolectar"
                            displayForm = true
                        }
                    }
                    OrderStatus.Gathering.orderStatusId , OrderStatus.DeliveringProcess.orderStatusId , OrderStatus.Delivered.orderStatusId  -> {
                        if(currentDeliveryStatusWay == 0){
                            orderFutureStatus = OrderStatus.Finished.orderStatusId
                            inputValue = "Finalizar"
                            displayForm = true
                        }
                    }
                }
            }
        }
    }

    fun getOrderActionStatusClient() {
        var currentDeliveryStatusWay = 1
        var currentDeliveryStatusCount = 0
        var currentDeliveryStatusDifference = 0

        for(update in orderInfoForBundle!!.orderUpdates){
            if(update.newOrderStatusId == "2"){
                currentDeliveryStatusCount++
            }
        }

        when("${orderInfoForBundle!!.orderInformation.ownerDelivery}${orderInfoForBundle!!.orderInformation.clientDelivery}"){
            "11" -> {
                val coutnDeliveryStatus = 3
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
            else -> {
                val coutnDeliveryStatus = 2
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
        }

        if (currentDeliveryStatusDifference == 0) {
            currentDeliveryStatusWay = 0
        }

        when(orderInfoForBundle?.orderInformation?.orderStatusId){
            OrderStatus.Gathering.orderStatusId, OrderStatus.DeliveringProcess.orderStatusId -> {
                if (currentDeliveryStatusWay == 0) {
                    if (orderInfoForBundle?.orderInformation?.orderTypeId == OrderType.Purchase.orderTypeId) {
                        inputValue = OrderStatus.Finished.orderStatusId
                        orderFutureStatus = "Finalizar"
                    } else {
                        inputValue = OrderStatus.Delivered.orderStatusId
                        orderFutureStatus = "Entregado"
                    }
                    displayForm = true
                }
            }
            OrderStatus.Delivered.orderStatusId -> {
                if (orderInfoForBundle?.orderInformation?.orderTypeId == OrderType.Lease.orderTypeId) {
                    if (currentDeliveryStatusWay == 0) {
                        inputValue = "Listo para recolectar"
                        orderFutureStatus = OrderStatus.Gathering.orderStatusId
                        displayForm = true
                    }
                } else {
                    if (currentDeliveryStatusWay == 0) {
                        inputValue = "Finalizar"
                        orderFutureStatus = OrderStatus.Finished.orderStatusId
                        displayForm = true
                    }
                }
            }

        }

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
