package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jonathangalvan.mirelex.Adapters.SliderPagerAdapter
import com.example.jonathangalvan.mirelex.Enums.OrderStatus
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Fragments.Order.OrderStatusDetailList
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomBottomAlert
import com.example.jonathangalvan.mirelex.Interfaces.BottomAlertInterface
import com.example.jonathangalvan.mirelex.Interfaces.ConversationInterface
import com.example.jonathangalvan.mirelex.Interfaces.OrderProductInfo
import com.example.jonathangalvan.mirelex.Interfaces.UserInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.AcceptRejectOrderRequest
import com.example.jonathangalvan.mirelex.Requests.GetOrderInfoRequest
import com.example.jonathangalvan.mirelex.Requests.UpdateOrderStatusRequest
import com.rey.material.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.view_detail_info_row.view.*
import kotlinx.android.synthetic.main.view_detail_info_row_with_chevron_title.view.*
import kotlinx.android.synthetic.main.view_detail_info_row_with_title.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.ArrayList

class OrderDetailActivity : AppCompatActivity(), OrderStatusDetailList.OnFragmentInteractionListener{
    var orderInfoForBundle: OrderProductInfo? = null
    var orderFutureStatus = ""
    var inputValue = ""
    var displayForm = false
    var orderId: String? = ""
    var sessionUser: UserInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Getorder info*/
        val bundleFromOrders = intent.extras
        orderId = bundleFromOrders?.getString("orderId")
        getOrderInfo()

        /*Set user info*/
        sessionUser = SessionModel(this).getUser()

        /*Hide action button and form*/
        detailOrderActionButton.visibility = View.GONE
        detailOrderActionAceptReject.visibility = View.GONE
        detailOrderDelivery.visibility = View.GONE

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
                val responseObj = UtilsModel.getPostResponse(this@OrderDetailActivity, responseStr)
                if(responseObj.status == "success"){
                    val orderInfo = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), OrderProductInfo::class.java)
                    orderInfoForBundle = orderInfo
                    runOnUiThread {
                        run {
                            supportActionBar?.title = orderInfo.orderInformation.folio

                            /*Event for action button*/
                            if(
                                ((orderInfoForBundle?.orderInformation?.acceptedDate == null && orderInfoForBundle?.orderInformation?.rejectedDate == null) ||
                                orderInfoForBundle?.orderInformation?.orderStatusId == OrderStatus.AcceptDate.orderStatusId) &&
                                orderInfoForBundle?.orderOwnerInformation?.userId == sessionUser?.person?.userId
                            ){
                                detailOrderActionAceptReject.visibility = View.VISIBLE
//                                        detailOrderDelivery.visibility = View.VISIBLE /*Hide delivery and payments*/

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
                                statusForm()
                            }

                            /*Fill order and product info*/

                            /*Slider Gallery*/
                            var images: ArrayList<String> = ArrayList()
                            if(orderInfo.orderProducts!![0].productFeaturedImage != null){
                                images.add(orderInfo.orderProducts!![0].productFeaturedImage.toString())
                                for(productImage in orderInfo.orderProducts!![0].images!!){
                                    images.add(productImage.imageUrl.toString())
                                }
                            }
                            val sliderAdapter = SliderPagerAdapter(images, supportFragmentManager)
                            detailOrderImageSlider.adapter = sliderAdapter
                            indicator.setViewPager(detailOrderImageSlider)

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
                                    if(sessionUser?.person?.userId == orderInfo.orderClientInformation.userId){
                                         when(orderInfo.orderOwnerInformation.companyName){
                                            null -> {
                                                name = "${orderInfo.orderOwnerInformation.firstName} ${orderInfo.orderOwnerInformation.paternalLastName}"
                                            }
                                            else -> {
                                                name = orderInfo.orderOwnerInformation.companyName.toString()
                                            }
                                        }
                                        pictureUrl = orderInfo.orderOwnerInformation.profilePictureUrl.toString()
                                        json = UtilsModel.getGson().toJson(orderInfo.orderOwnerInformation)
                                        isThereImage = orderInfo.orderOwnerInformation.profilePictureUrl
                                    }else{
                                        when(orderInfo.orderClientInformation.companyName){
                                            null -> {
                                                name = "${orderInfo.orderClientInformation.firstName} ${orderInfo.orderClientInformation.paternalLastName}"
                                            }
                                            else -> {
                                                name = orderInfo.orderClientInformation.companyName.toString()
                                            }
                                        }
                                        pictureUrl = orderInfo.orderClientInformation.profilePictureUrl.toString()
                                        json = UtilsModel.getGson().toJson(orderInfo.orderClientInformation)
                                        isThereImage = orderInfo.orderClientInformation.profilePictureUrl
                                    }
                                }
                            }
                            if(isThereImage != null){
                                Picasso.with(this@OrderDetailActivity).load(pictureUrl).into(detailOrderOwnerImage)
                            }
                            detailOrderOwnerName.text = name

                            /*Fill order info*/
                            addRow(resources.getString(R.string.folio), orderInfo.orderInformation.folio!!, detailOrderInfo)
                            addRow(resources.getString(R.string.type), orderInfo.orderInformation.orderType!!, detailOrderInfo)
                            addRow(resources.getString(R.string.date), orderInfo.orderInformation.startDate!!, detailOrderInfo)
                            if(orderInfo.orderOwnerInformation.isMirelexStore == "1"){
                                val ownerinfo = orderInfo.orderOwnerInformation
                                var storeStr = "\n${ownerinfo.companyName.toString()} " +
                                        "\n${ownerinfo.email.toString()} " +
                                        "\n${ownerinfo.personalPhone.toString()}" +
                                        "\n\n${ownerinfo.address.street}, ${ownerinfo.address.neighborhoodName}, ${ownerinfo.address.postalCode}, ${ownerinfo.address.cityName}, ${ownerinfo.address.stateName}"
                                addRow(resources.getString(R.string.selectedStoreNoPoints), storeStr, detailOrderInfo)
                            }else{
                                val storeInfo = orderInfo!!.orderInformation.store
                                var storeStr = "\n${storeInfo?.firstName.toString()} " +
                                        "\n${storeInfo?.email.toString()} " +
                                        "\n${storeInfo?.personalPhone.toString()}"
                                if(storeInfo?.address != null){
                                    storeStr += "\n\n${storeInfo?.address?.street}, ${storeInfo?.address?.neighborhoodName}, ${storeInfo?.address?.postalCode}, ${storeInfo?.address?.cityName}, ${storeInfo?.address?.stateName}"
                                }
                                addRow(resources.getString(R.string.selectedStoreNoPoints), storeStr, detailOrderInfo)
                            }
//                            addRow(resources.getString(R.string.status), orderInfo.orderInformation.orderStatus!!, detailOrderInfo)
                            detailOrderEstatusName.text = orderInfo.orderInformation.orderStatus
                            addRow(resources.getString(R.string.total), orderInfo.orderInformation.totalFormatted!!, detailOrderInfo)

                            if(orderInfo?.orderInformation?.deliveryAddress != null){
                                addRowTitle(
                                    "${orderInfo.orderInformation.deliveryAddress!!.street} ${orderInfo.orderInformation.deliveryAddress!!.numExt}, " +
                                            "${orderInfo.orderInformation.deliveryAddress!!.cityName}, " +
                                            "${orderInfo.orderInformation.deliveryAddress!!.stateName}" ,
                                    detailOrderInfo
                                )
                            }

                            addStatusRow(detailOrderInfo)

                            /*Fill order fitting info*/
                            if(orderInfo.orderOwnerInformation.userId == sessionUser?.person?.userId){
                                if(orderInfo.orderProducts!![0].fittings!!.isNotEmpty()){
                                    val tv = TextView(this@OrderDetailActivity)
                                    val params = android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                    params.setMargins(10, 10, 10, 10)
                                    tv.layoutParams = params
                                    tv.text = resources.getString(R.string.adjustedMesaurements)
                                    tv.gravity = Gravity.CENTER
                                    tv.setTextColor(ContextCompat.getColor(this@OrderDetailActivity, android.R.color.black))
                                    tv.setTypeface(null, Typeface.BOLD)
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.toFloat())
                                    detailOrderFittingInfo.addView(tv)

                                    addRow(resources.getString(R.string.bust), orderInfo.orderProducts!![0].fittings!![0].bust.toString(), detailOrderFittingInfo)
                                    addRow(resources.getString(R.string.waist), orderInfo.orderProducts!![0].fittings!![0].waist.toString(), detailOrderFittingInfo)
                                    addRow(resources.getString(R.string.hip), orderInfo.orderProducts!![0].fittings!![0].hip.toString(), detailOrderFittingInfo)
                                    addRow(resources.getString(R.string.heightInCms), orderInfo.orderProducts!![0].fittings!![0].height.toString(), detailOrderFittingInfo)
                                }
                            }

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
                                if(sessionUser?.person?.userTypeId == UserType.Customer.userTypeId && orderInfo.orderOwnerInformation.userTypeId == UserType.Store.userTypeId) {
                                    val goToStoreDetailShort = Intent(
                                        this@OrderDetailActivity,
                                        StoreDetailShortActivity::class.java
                                    )
                                    val bundleToStoreDetailShort = Bundle()
                                    bundleToStoreDetailShort.putString("personObj", json)
                                    goToStoreDetailShort.putExtras(bundleToStoreDetailShort)
                                    startActivity(goToStoreDetailShort)
                                }else{
                                    if(sessionUser?.person?.userTypeId == UserType.Customer.userTypeId && orderInfo.orderOwnerInformation.userTypeId == UserType.Customer.userTypeId) {
                                        val bundleToChat = Bundle()
                                        val goToChat = Intent(this@OrderDetailActivity, ChatActivity::class.java)
                                        val sessionUser = SessionModel(this@OrderDetailActivity).getUser()
                                        var conversationObj : ConversationInterface

                                        if(sessionUser?.person?.userId == orderInfo.orderClientInformation.userId) {
                                             conversationObj = ConversationInterface(
                                                userIdTo = orderInfo.orderOwnerInformation.userId,
                                                userTo = if (orderInfo.orderOwnerInformation.companyName != null) orderInfo.orderOwnerInformation.companyName else "${orderInfo.orderOwnerInformation.firstName} ${orderInfo.orderOwnerInformation.paternalLastName}",
                                                userIdFrom = sessionUser.person?.userId
                                            )
                                        }else{
                                            conversationObj = ConversationInterface(
                                                userIdTo = orderInfo.orderClientInformation.userId,
                                                userTo = if (orderInfo.orderClientInformation.companyName != null) orderInfo.orderClientInformation.companyName else "${orderInfo.orderClientInformation.firstName} ${orderInfo.orderClientInformation.paternalLastName}",
                                                userIdFrom = sessionUser.person?.userId
                                            )
                                        }
                                        bundleToChat.putString("conversationObj", UtilsModel.getGson().toJson(conversationObj))
                                        goToChat.putExtras(bundleToChat)
                                        startActivity(goToChat)
                                    }else{
                                        if(sessionUser?.person?.userTypeId == UserType.Store.userTypeId && orderInfo.orderClientInformation.userTypeId == UserType.Customer.userTypeId) {
                                            val bundleToChat = Bundle()
                                            val goToChat = Intent(this@OrderDetailActivity, ChatActivity::class.java)
                                            val sessionUser = SessionModel(this@OrderDetailActivity).getUser()
                                            val conversationObj = ConversationInterface(
                                                userIdTo = orderInfo.orderClientInformation.userId,
                                                userTo = if(orderInfo.orderClientInformation.companyName != null) orderInfo.orderClientInformation.companyName  else "${orderInfo.orderClientInformation.firstName } ${orderInfo.orderClientInformation.paternalLastName }",
                                                userIdFrom = sessionUser.person?.userId
                                            )
                                            bundleToChat.putString("conversationObj", UtilsModel.getGson().toJson(conversationObj))
                                            goToChat.putExtras(bundleToChat)
                                            startActivity(goToChat)
                                        }
                                    }
                                }
                            })

                            if(!orderInfo.orderStatusHistory.isEmpty() && orderInfo.orderStatusHistory.size > 0){
                                var highlitedVlineFound = false
                                orderInfo.orderStatusHistory.forEachIndexed {index, value ->

                                    //Status img
                                    val imageView = ImageView(this@OrderDetailActivity)
                                    var lp = LinearLayout.LayoutParams(70, 70)
                                    lp.setMargins(10,0,10,0)
                                    imageView.layoutParams = lp
                                    Glide.with(this@OrderDetailActivity).load(value?.icon).apply( RequestOptions().override(70, 70)).into(imageView)
                                    orderStatusIcons.addView(imageView)

                                    //Line
                                    if(orderInfo.orderStatusHistory.size > (index + 1)){
                                        val vLine = View(this@OrderDetailActivity)

                                        if(highlitedVlineFound){
                                            vLine.setBackgroundColor(ContextCompat.getColor(this@OrderDetailActivity, R.color.colorGrey))
                                        }else{
                                            vLine.setBackgroundColor(ContextCompat.getColor(this@OrderDetailActivity, R.color.colorBlue))
                                        }

                                        lp = LinearLayout.LayoutParams(30, 3)
                                        vLine.layoutParams = lp
                                        orderStatusIcons.addView(vLine)

                                        if(value.active){
                                            highlitedVlineFound = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    fun statusForm(){
        if(displayForm){
            detailOrderActionButton.visibility = View.VISIBLE
            detailOrderActionButton.text = inputValue
            detailOrderActionButton.setOnClickListener(View.OnClickListener {
                if(orderInfoForBundle!!.orderInformation.orderTypeId == OrderType.Fitting.orderTypeId ){
                    if(
                        (orderFutureStatus == OrderStatus.SeeYouSoon.orderStatusId && sessionUser?.person?.isMirelexStore == "1") ||
                        (orderFutureStatus == OrderStatus.FitiingDone.orderStatusId && sessionUser?.person?.isMirelexStore == "1")
                    ){
                        val ba = UtilsModel.getGson().toJson(BottomAlertInterface(
                            alertType = "fittingOrderProcess",
                            orderId = orderInfoForBundle!!.orderInformation.orderId,
                            userId = orderInfoForBundle!!.orderClientInformation.userId,
                            productId = orderInfoForBundle?.orderProducts!![0].productId
                        ))
                        CustomBottomAlert().bottomSheetDialogInstance(ba).show(supportFragmentManager, "alert")
                    }else{
                        changeOrderStatus()
                    }
                }else{
                    changeOrderStatus()
                }
            })
        }
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
//            detailOrderDelivery.isChecked  Hide delivery and payments
        ))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@OrderDetailActivity, endpoint, aceptOrderObj)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@OrderDetailActivity, responseStr)
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
                val responseObj = UtilsModel.getPostResponse(this@OrderDetailActivity, responseStr)
                if(responseObj.status == "success"){
                    finish()
                }else{
                    UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                }
            }
        })
    }

    fun orderStatusFittingProcess(){
        val sessionUserId = sessionUser?.person?.userId

        if(orderInfoForBundle!!.orderOwnerInformation.isMirelexStore == "0"){
            /*Process when order is customer - customer*/

            /*Session user is owner*/
            if(orderInfoForBundle!!.orderOwnerInformation.userId == sessionUserId){
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                        OrderStatus.YesItCould.orderStatusId -> {
                            orderFutureStatus = OrderStatus.ReadyInStore.orderStatusId
                            inputValue = "Entregado en tienda"
                            displayForm = true
                        }
                        OrderStatus.FitiingDone.orderStatusId -> {
                            orderFutureStatus = OrderStatus.SeeYouSoon.orderStatusId
                            inputValue = "Finalizar"
                            displayForm = true
                        }
                }
            }else{

                when(sessionUser?.person?.userTypeId){

                    /*Session user is client*/
                    UserType.Customer.userTypeId -> { }

                    /*Session user is store*/
                    UserType.Store.userTypeId ->{
                        when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                            OrderStatus.ReadyInStore.orderStatusId -> {
                                orderFutureStatus = OrderStatus.RentInCourse.orderStatusId
                                inputValue = "LLego el cliente"
                                displayForm = true
                            }
                            OrderStatus.RentInCourse.orderStatusId -> {
                                orderFutureStatus = OrderStatus.FitiingDone.orderStatusId
                                inputValue = "Prueba finalizada"
                                displayForm = true
                            }
                        }
                    }
                }
            }
        }else{
            /*Process when order is store - customer*/

            /*Session user is store*/
            if (orderInfoForBundle!!.orderOwnerInformation.userId == sessionUserId) {
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.YesItCould.orderStatusId -> {
                        orderFutureStatus = OrderStatus.SeeYouSoon.orderStatusId
                        inputValue = "Finalizar"
                        displayForm = true
                    }
                }
            }
        }
    }

    fun orderStatusLeasePurchaseProcess(){
        val sessionUserId = sessionUser?.person?.userId

        if(orderInfoForBundle!!.orderOwnerInformation.isMirelexStore == "0"){

            /*Process when order is customer - customer*/

            /*Session user is owner*/
            if(orderInfoForBundle!!.orderOwnerInformation.userId == sessionUserId){
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.YesItCould.orderStatusId -> {
                        orderFutureStatus = OrderStatus.ReadyInStore.orderStatusId
                        inputValue = "Entregado en tienda"
                        displayForm = true
                    }
                    OrderStatus.IsInStore.orderStatusId -> {
                        orderFutureStatus = OrderStatus.SeeYouSoon.orderStatusId
                        inputValue = "Finalizar"
                        displayForm = true
                    }
                }
            }else{

                when(sessionUser?.person?.userTypeId){

                    /*Session user is client*/
                    UserType.Customer.userTypeId -> {

                    }

                    /*Session user is store*/
                    UserType.Store.userTypeId ->{
                        when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                            OrderStatus.ReadyInStore.orderStatusId -> {
                                when(orderInfoForBundle!!.orderInformation.orderTypeId){
                                    OrderType.Lease.orderTypeId -> {
                                        orderFutureStatus = OrderStatus.RentInCourse.orderStatusId
                                        inputValue = "Entregado a cliente"
                                        displayForm = true
                                    }
                                    OrderType.Purchase.orderTypeId -> {
                                        orderFutureStatus = OrderStatus.SeeYouSoon.orderStatusId
                                        inputValue = "Finalizar"
                                        displayForm = true
                                    }
                                }
                            }
                            OrderStatus.RentInCourse.orderStatusId -> {
                                orderFutureStatus = OrderStatus.IsInStore.orderStatusId
                                inputValue = "Recibido del cliente"
                                displayForm = true
                            }
                        }
                    }
                }

            }
        }else{

            /*Process when order is store - customer*/

            /*Session user is store*/
            if (orderInfoForBundle!!.orderOwnerInformation.userId == sessionUserId) {
                when (orderInfoForBundle!!.orderInformation.orderStatusId) {
                    OrderStatus.YesItCould.orderStatusId -> {
                        when(orderInfoForBundle!!.orderInformation.orderTypeId){
                            OrderType.Lease.orderTypeId -> {
                                orderFutureStatus = OrderStatus.RentInCourse.orderStatusId
                                inputValue = "Entregado a cliente"
                                displayForm = true
                            }
                            OrderType.Purchase.orderTypeId -> {
                                orderFutureStatus = OrderStatus.SeeYouSoon.orderStatusId
                                inputValue = "Finalizar"
                                displayForm = true
                            }
                        }
                    }
                    OrderStatus.RentInCourse.orderStatusId -> {
                        orderFutureStatus = OrderStatus.SeeYouSoon.orderStatusId
                        inputValue = "Finalizar"
                        displayForm = true
                    }
                }
            }
        }
    }

    fun addRow(title: String, value: String, viewGroup: ViewGroup){
        val insertRow = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailOrderInfo, false)
        insertRow.detailInfoNameView.text = title
        insertRow.detailInfoValueView.text = value
        viewGroup.addView(insertRow)
    }

    fun addRowTitle(value: String, viewGroup: ViewGroup){
        val insertRow = layoutInflater.inflate(R.layout.view_detail_info_row, detailOrderInfo, false)
        insertRow.detailInfoJustValueView.text = value
        viewGroup.addView(insertRow)
    }

    fun addStatusRow(viewGroup: ViewGroup){
        val insertRow = layoutInflater.inflate(R.layout.view_detail_info_row_with_chevron_title, detailOrderInfo, false)
        insertRow.detailInfoChevronTitle.text = resources.getString(R.string.statusHistory)
        insertRow.setOnClickListener(View.OnClickListener {
            openTab(OrderStatusDetailList())
        })
        viewGroup.addView(insertRow)
    }

    fun openTab(fragment: androidx.fragment.app.Fragment){
        val b = Bundle()
        b.putString("orderObj", UtilsModel.getGson().toJson(orderInfoForBundle))
        fragment.arguments = b
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom, R.anim.abc_slide_in_bottom,  R.anim.abc_slide_out_bottom)
        transaction.replace(android.R.id.content, fragment, "SelectedItemsFragment")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onFragmentInteraction(uri: Uri) {}

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
