package com.example.jonathangalvan.mirelex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Enums.OrderStatus
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Interfaces.ServiceInterface
import com.example.jonathangalvan.mirelex.Interfaces.UserInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.AcceptRejectServiceOrderRequest
import com.example.jonathangalvan.mirelex.Requests.UpdateServiceOrderStatusRequest
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_service_order_detail.*
import kotlinx.android.synthetic.main.view_detail_info_row_with_title.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ServiceOrderDetailActivity : AppCompatActivity() {
    var serviceObj: ServiceInterface? = null
    var sessionUser: UserInterface? = null
    var orderFutureStatus = ""
    var inputValue = ""
    var displayForm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_order_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundleFromServices = intent.extras
        serviceObj = UtilsModel.getGson().fromJson(bundleFromServices?.getString("serviceObj"), ServiceInterface::class.java)
        sessionUser = SessionModel(this).getUser()

        supportActionBar?.title = serviceObj?.folio

        /*Hide status action bar*/
        serviceDetailActionAceptReject.visibility = View.GONE
        serviceDetailActionButton.visibility = View.GONE

        /*Set data depending on session user*/
        setServiceData()

        /*Set status action*/
        setStatusAction()
    }

    fun setServiceData(){
        var pictureUrl = ""
        var name = ""
        var json = ""
        var isThereImage: String? = null
        when(SessionModel(this).getSessionUserType()){
            UserType.Store.userTypeId -> {
                pictureUrl = serviceObj?.client?.profilePictureUrl.toString()
                name = "${serviceObj?.client?.firstName} ${serviceObj?.client?.paternalLastName}"
                json = UtilsModel.getGson().toJson(serviceObj?.client)
                isThereImage = serviceObj?.client?.profilePictureUrl
            }
            else -> {
                pictureUrl = serviceObj?.owner?.profilePictureUrl.toString()
                name = serviceObj?.owner?.companyName.toString()
                json = UtilsModel.getGson().toJson(serviceObj?.owner)
                isThereImage = serviceObj?.owner?.profilePictureUrl
            }
        }

        addRow(resources.getString(R.string.type), serviceObj?.orderType!!)
        addRow(resources.getString(R.string.date), serviceObj?.startDate!!)
        if(serviceObj?.orderStatus != null){
            addRow(resources.getString(R.string.status), serviceObj?.orderStatus!!)
        }
        addRow(resources.getString(R.string.total), serviceObj?.totalFormatted!!)

        if( isThereImage != null){
            Picasso.with(this).load(pictureUrl).into(serviceDetailStoreImg)
        }

        serviceDetailStoreName.text = name

        /*On store click*/
        serviceDetailStore.setOnClickListener(View.OnClickListener {
            val goToStoreDetailShort = Intent(this, StoreDetailShortActivity::class.java)
            val bundleToStoreDetailShort = Bundle()
            bundleToStoreDetailShort.putString("personObj", json)
            goToStoreDetailShort.putExtras(bundleToStoreDetailShort)
            startActivity(goToStoreDetailShort)
        })
    }

    fun setStatusAction(){
        if(serviceObj?.owner?.userId == sessionUser?.person?.userId){
            if(
                (serviceObj?.acceptedDate == null && serviceObj?.rejectedDate == null) ||
                serviceObj?.orderStatusId == OrderStatus.Open.orderStatusId
            ){
                serviceDetailActionAceptReject.visibility = View.VISIBLE

                serviceDetailActionAceptOrder.setOnClickListener(View.OnClickListener {
                    aceptOrder()
                })

                serviceDetailActionRejectOrder.setOnClickListener(View.OnClickListener {
                    aceptOrder(false)
                })
            }else{
                when(serviceObj?.orderTypeId){
                    OrderType.Sewing.orderTypeId -> {
                        serviceOrderSewingStatus()
                    }
                    OrderType.Cleaning.orderTypeId -> {
                        serviceOrderCleaningStatus()
                    }
                }
                /*Show or hide status form*/
                if(displayForm){
                    serviceDetailActionButton.visibility = View.VISIBLE
                    serviceDetailActionButton.text = inputValue
                    serviceDetailActionButton.setOnClickListener(View.OnClickListener {
                        changeOrderStatus()
                    })
                }
            }
        }
    }

    fun aceptOrder(isAcepted: Boolean = true){
        var endpoint = ""
        if(isAcepted){
            endpoint = resources.getString(R.string.acceptServiceOrder)
        }else{
            endpoint = resources.getString(R.string.rejectServiceOrder)
        }

        /*Accept denied order service*/
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val aceptOrderObj= UtilsModel.getGson().toJson(
            AcceptRejectServiceOrderRequest(
                serviceObj?.serviceOrderId
            )
        )
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, endpoint, aceptOrderObj)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@ServiceOrderDetailActivity, responseStr)
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
        val updateOrderStatus= UtilsModel.getGson().toJson(
            UpdateServiceOrderStatusRequest(
                orderFutureStatus,
                serviceObj?.serviceOrderId
            )
        )
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.updateServiceOrderStatus), updateOrderStatus)).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@ServiceOrderDetailActivity, responseStr)
                if(responseObj.status == "success"){
                    finish()
                }else{
                    UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                }
            }
        })
    }

    fun serviceOrderCleaningStatus(){
        var currentDeliveryStatusWay = 1
        var currentDeliveryStatusCount = 0
        var currentDeliveryStatusDifference = 0

        for(update in serviceObj!!.updates){
            if(update.newOrderStatusId == "2"){
                currentDeliveryStatusCount++
            }
        }

        when ("${serviceObj?.ownerDelivery}${serviceObj?.clientDelivery}") {
            "00" -> {
                val coutnDeliveryStatus = 1 // numero de estados en recollecion
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
            "10", "01" -> {
                val coutnDeliveryStatus = 2 // numero de estados en recollecion
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
            "11" -> {
                val coutnDeliveryStatus = 3
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
        }

        if (currentDeliveryStatusDifference === 0) {
            currentDeliveryStatusWay = 0
        }

        if (serviceObj?.owner?.isMirelexStore == "1") {
            if (serviceObj?.owner?.userId == sessionUser?.person?.userId) {
                when (serviceObj?.orderStatusId) {
                    OrderStatus.Gathering.orderStatusId, OrderStatus.Processing.orderStatusId, OrderStatus.DeliveringProcess.orderStatusId -> {
                        if (currentDeliveryStatusWay == 1) {
                            orderFutureStatus = OrderStatus.Delivered.orderStatusId
                            inputValue = "Entregado"
                            displayForm = true
                        } else {
                            orderFutureStatus = OrderStatus.Finished.orderStatusId
                            inputValue = "Finalizar"
                            displayForm = true
                        }
                    }
                    "5" -> {
                        if (currentDeliveryStatusWay == 1) {
                            orderFutureStatus = OrderStatus.Gathering.orderStatusId
                            inputValue = "Listo para recolectar"
                            displayForm = true
                        }
                    }
                }
            }
        }
    }

    fun serviceOrderSewingStatus(){
        var currentDeliveryStatusWay = 1
        var currentDeliveryStatusCount = 0
        var currentDeliveryStatusDifference = 0

        for(update in serviceObj!!.updates){
            if(update.newOrderStatusId == "2"){
                currentDeliveryStatusCount++
            }
        }

        when ("${serviceObj?.ownerDelivery}${serviceObj?.clientDelivery}") {
            "00", "10", "01" -> {
                val coutnDeliveryStatus = 2 // numero de estados en recollecion
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
            "11" -> {
                val coutnDeliveryStatus = 3
                currentDeliveryStatusDifference = coutnDeliveryStatus - currentDeliveryStatusCount
            }
        }

        if (currentDeliveryStatusDifference === 0) {
            currentDeliveryStatusWay = 0
        }

        if (serviceObj?.owner?.isMirelexStore == "1") {
            if (serviceObj?.owner?.userId == sessionUser?.person?.userId) {
                when (serviceObj?.orderStatusId) {
                    "3" -> {
                        orderFutureStatus = OrderStatus.Gathering.orderStatusId
                        inputValue = "Listo para recolectar"
                        displayForm = true
                    }
                    "2" -> {
                        orderFutureStatus = OrderStatus.Finished.orderStatusId
                        inputValue = "Finalizar"
                        displayForm = true
                    }
                }
            }
        }
    }

    fun addRow(title: String, value: String){
        val insertRow = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, serviceDetailInfo, false)
        insertRow.detailInfoNameView.text = title
        insertRow.detailInfoValueView.text = value
        serviceDetailInfo.addView(insertRow)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
