package com.example.jonathangalvan.mirelex

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.jonathangalvan.mirelex.Interfaces.*
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.GetCleanningServiceStoresRequest
import com.example.jonathangalvan.mirelex.ViewModels.ServicesViewModel
import kotlinx.android.synthetic.main.activity_service_create.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ServiceCreateActivity : AppCompatActivity() {

    private var service = 0
    private var serviceTypes: ArrayList<CatalogInterface> = ArrayList()
    private var serviceStores: ArrayList<ServiceStore> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_create)
        supportActionBar?.hide()
        val viewModel = ViewModelProviders.of(this).get(ServicesViewModel::class.java)
        reseatServiceTypes()

        createServiceType.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) { }

            override fun onTabUnselected(p0: TabLayout.Tab?) { }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                service = p0!!.position
                reseatServiceTypes()
            }
        })

        serviceCreateTypes.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reseatServiceStores(position)
            }
        }

        serviceCreateStores.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                println("*****************")
                println(serviceStores[position].name)
                println(serviceStores[position].total)
            }
        }
    }

    fun reseatServiceTypes(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        var endpoint = ""
        when(service){
            0 -> {
                /*Cleanning tab*/
                endpoint = resources.getString(R.string.getCleanningTypes)
            }
            else -> {
                /*Sewing tab*/
                endpoint = resources.getString(R.string.getSewingTypes)

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
                val responseObj = UtilsModel.getGson().fromJson(responseStr, ResponseInterface::class.java)
                if(responseObj.status == "success"){
                    val serviceTypesObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), CatalogArrayInterface::class.java)
                    serviceTypes = serviceTypesObj.data
                    runOnUiThread {
                        run {
                            val adapterServicesTypes = ArrayAdapter<CatalogInterface>(this@ServiceCreateActivity, android.R.layout.simple_spinner_dropdown_item, serviceTypesObj.data)
                            adapterServicesTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            serviceCreateTypes.adapter = adapterServicesTypes
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
                val responseObj = UtilsModel.getGson().fromJson(responseStr, ResponseInterface::class.java)
                if(responseObj.status == "success"){
                    val serviceStoresObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ServiceStoresInterface::class.java)
                    val adapterServicesStores = ArrayAdapter<ServiceStore>(this@ServiceCreateActivity, android.R.layout.simple_spinner_dropdown_item, serviceStoresObj.data)
                    serviceStores = serviceStoresObj.data
                    adapterServicesStores.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    runOnUiThread {
                        run {
                            serviceCreateStores.adapter = adapterServicesStores
                        }
                    }
                }
            }
        })
    }
}
