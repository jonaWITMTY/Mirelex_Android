package com.example.jonathangalvan.mirelex

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Fragments.Utils.DatePickerAvailable
import com.example.jonathangalvan.mirelex.Interfaces.*
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreateServiceRequest
import com.example.jonathangalvan.mirelex.Requests.GetCleanningServiceStoresRequest
import com.example.jonathangalvan.mirelex.Requests.GetServiceTotalRequest
import kotlinx.android.synthetic.main.activity_service_create.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ServiceCreateActivity : AppCompatActivity(),
    DatePickerAvailable.OnFragmentInteractionListener{

    private var service = 0
    private var serviceTypes: ArrayList<CatalogInterface> = ArrayList()
    private var sewingTypes: ArrayList<SewingInterface> = ArrayList()
    private var serviceStores: ArrayList<ServiceStore> = ArrayList()
    private var total: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_create)
        supportActionBar?.hide()
        reseatServiceTypes()

        /*Change tabs event*/
        createServiceType.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) { }

            override fun onTabUnselected(p0: TabLayout.Tab?) { }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                service = p0!!.position
                reseatServiceTypes()
            }
        })

        /*On type change*/
        serviceCreateTypes.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reseatServiceStores(position)
            }
        }

        /*Date picker*/
        serviceCreateDate.setOnClickListener(View.OnClickListener {
            openTab(DatePickerAvailable())
        })

        /*View close*/
        serviceCreateClose.setOnClickListener(View.OnClickListener {
            finish()
        })

        /*Create service button*/
        serviceCreateOrderService.setOnClickListener(View.OnClickListener {
            if(serviceCreateTerms.isChecked){
                if(inputValidations()){
                    val loader = layoutInflater.inflate(R.layout.view_progressbar,findViewById(android.R.id.content), true)
                    var orderTypeId = ""
                    when(service){
                        0 -> {
                            /*Cleanning tab*/
                            orderTypeId = OrderType.Cleaning.orderTypeId
                        }
                        else -> {
                            /*Sewing tab*/
                            orderTypeId = OrderType.Sewing.orderTypeId
                        }
                    }
                    val serviceObj = CreateServiceRequest(
                        startDate = serviceCreateDate.text.toString(),
                        endDate = serviceCreateDate.text.toString(),
                        orderType = orderTypeId,
                        total = total,
                        storeId = serviceStores[serviceCreateStores.selectedItemPosition].userId
                    )
                    when(service){
                        0 -> {
                            /*Cleanning tab*/
                            serviceObj.productStyleId = serviceTypes[serviceCreateTypes.selectedItemPosition].productCatalogId.toString()
                            serviceObj.clientDelivery = createServiceSend.isChecked
                        }
                        else -> {
                            /*Sewing tab*/
                            serviceObj.sewingTypeId = sewingTypes[serviceCreateTypes.selectedItemPosition].sewingTypeId.toString()

                        }
                    }

                    UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.createServiceOrder), UtilsModel.getGson().toJson(serviceObj))).enqueue(object: Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                            finish()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                            val responseStr = response.body()?.string()
                            val responseObj = UtilsModel.getPostResponse(responseStr)
                            if(responseObj.status == "success"){
                                UtilsModel.getAlertView().newInstance(responseStr, 4, 0).show(supportFragmentManager,"alertDialog")
                            }else{
                                UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                            }
                        }
                    })
                }else{
                    val text = resources.getText(R.string.fillRequiredFields)
                    val duration = Toast.LENGTH_SHORT
                    Toast.makeText(this, text, duration).show()
                }
            }else{
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorMissingTerms(), 1, 0).show(supportFragmentManager, "alertView")
            }
        })

        /*On date change*/
        serviceCreateDate.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                getServiceTotal()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun getServiceTotal(){
        if(inputValidations()){
            var orderTypeId = ""
            when(service){
                0 -> {
                    /*Cleanning tab*/
                    orderTypeId = OrderType.Cleaning.orderTypeId
                }
                else -> {
                    /*Sewing tab*/
                    orderTypeId = OrderType.Sewing.orderTypeId
                }
            }
            val serviceTotalObj = GetServiceTotalRequest(
                serviceCreateDate.text.toString(),
                serviceCreateDate.text.toString(),
                orderTypeId,
                "0",
                serviceStores[serviceCreateStores.selectedItemPosition].userId,
                null,
                null
            )
            when(service){
                0 -> {
                    /*Cleanning tab*/
                    serviceTotalObj.productStyleId = serviceTypes[serviceCreateTypes.selectedItemPosition].productCatalogId.toString()
                }
                else -> {
                    /*Sewing tab*/
                    serviceTotalObj.sewingTypeId = sewingTypes[serviceCreateTypes.selectedItemPosition].sewingTypeId.toString()

                }
            }
            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getOrderServiceTotal), UtilsModel.getGson().toJson(serviceTotalObj))).enqueue(object: Callback{
                override fun onFailure(call: Call, e: IOException) {}

                override fun onResponse(call: Call, response: Response) {
                    val responseStr = response.body()?.string()
                    val responseObj = UtilsModel.getPostResponse(responseStr)
                    if(responseObj.status == "success"){
                        val serviceTotal = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]),OrderTotal::class.java)
                        total = serviceTotal.total
                        serviceCreateServiceTotal.text = serviceTotal.totalFormatted
                    }
                }
            })
        }
    }

    fun reseatServiceTypes(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        var endpoint = ""
        when(service){
            0 -> {
                /*Cleanning tab*/
                endpoint = resources.getString(R.string.getCleanningTypes)
                createServiceSend.visibility = View.VISIBLE
            }
            else -> {
                /*Sewing tab*/
                endpoint = resources.getString(R.string.getSewingTypes)
                createServiceSend.visibility = View.GONE
            }
        }

        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@ServiceCreateActivity, endpoint)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    when(service){
                        0 -> {
                            /*Cleanning tab*/
                            val serviceTypesObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), CatalogArrayInterface::class.java)
                            serviceTypes = serviceTypesObj.data
                            runOnUiThread {
                                run {
                                    val adapterServicesTypes = ArrayAdapter<CatalogInterface>(this@ServiceCreateActivity, R.layout.view_spinner_item_black, serviceTypesObj.data)
                                    adapterServicesTypes.setDropDownViewResource(R.layout.view_spinner_item_black)
                                    serviceCreateTypes.adapter = adapterServicesTypes
                                }
                            }
                        }
                        else -> {
                            /*Sewing tab*/
                            val serviceTypesObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), SewingArrayInterface::class.java)
                            sewingTypes = serviceTypesObj.data
                            runOnUiThread {
                                run {
                                    val adapterServicesTypes = ArrayAdapter<SewingInterface>(this@ServiceCreateActivity, R.layout.view_spinner_item_black, serviceTypesObj.data)
                                    adapterServicesTypes.setDropDownViewResource(R.layout.view_spinner_item_black)
                                    serviceCreateTypes.adapter = adapterServicesTypes
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    fun reseatServiceStores(position: Int){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        var endpoint = ""
        var storeObj = ""

        when(service){
            0 -> {
                /*Cleanning tab*/
                endpoint = resources.getString(R.string.getCleanningStores)
                storeObj = UtilsModel.getGson().toJson(GetCleanningServiceStoresRequest(serviceTypes[position].productCatalogId.toString()))
            }
            else -> {
                /*Sewing tab*/
                endpoint = resources.getString(R.string.getSewingStores)
            }
        }

        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@ServiceCreateActivity, endpoint, storeObj)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val serviceStoresObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ServiceStoresInterface::class.java)
                    val adapterServicesStores = ArrayAdapter<ServiceStore>(this@ServiceCreateActivity, R.layout.view_spinner_item_black, serviceStoresObj.data)
                    serviceStores = serviceStoresObj.data
                    adapterServicesStores.setDropDownViewResource(R.layout.view_spinner_item_black)
                    runOnUiThread {
                        run {
                            serviceCreateStores.adapter = adapterServicesStores
                        }
                    }
                    getServiceTotal()
                }
            }
        })
    }

    fun openTab(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(android.R.id.content, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun inputValidations(): Boolean{
        var isCorrect = true
        if(
            serviceStores.isEmpty() ||
            serviceCreateDate.text.toString().isEmpty() ||
            total.toString().isEmpty()
        ){
            isCorrect = false
        }
        return isCorrect
    }

    fun confirmBtnCallback(){
        finish()
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
