package com.example.jonathangalvan.mirelex

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Interfaces.CatalogArrayInterface
import com.example.jonathangalvan.mirelex.Interfaces.CatalogInterface
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ViewModels.RegisterViewModel
import com.example.jonathangalvan.mirelex.ViewModels.ServicesViewModel
import kotlinx.android.synthetic.main.activity_service_create.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ServiceCreateActivity : AppCompatActivity() {

    private var serviceType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_create)
        supportActionBar?.hide()
        val viewModel = ViewModelProviders.of(this).get(ServicesViewModel::class.java)
        reseatCtalogs()

        createServiceType.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) { }

            override fun onTabUnselected(p0: TabLayout.Tab?) { }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                serviceType = p0!!.position
                reseatCtalogs()

            }
        })
    }

    fun reseatCtalogs(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        when(serviceType){
            0 -> {
                /*Cleanning tab*/
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@ServiceCreateActivity, resources.getString(R.string.getCleanningTypes))).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                        UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getGson().fromJson(responseStr, ResponseInterface::class.java)
                        if(responseObj.status == "success"){
                            val cleanningTypes = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), CatalogArrayInterface::class.java)
                            runOnUiThread {
                                run {
                                    /*Fill sizes*/
                                    val adapterCleanningTypes = ArrayAdapter<CatalogInterface>(this@ServiceCreateActivity, android.R.layout.simple_spinner_dropdown_item, cleanningTypes.data)
                                    adapterCleanningTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    serviceCreateTypes.adapter = adapterCleanningTypes
                                }
                            }
                        }
                    }
                })
            }
            1 -> {
                /*Sewing tab*/
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@ServiceCreateActivity, resources.getString(R.string.getSewingTypes))).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                        UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getGson().fromJson(responseStr, ResponseInterface::class.java)
                        if(responseObj.status == "success"){
                            val cleanningTypes = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), CatalogArrayInterface::class.java)
                            runOnUiThread {
                                run {
                                    /*Fill sizes*/
                                    val adapterCleanningTypes = ArrayAdapter<CatalogInterface>(this@ServiceCreateActivity, android.R.layout.simple_spinner_dropdown_item, cleanningTypes.data)
                                    adapterCleanningTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    serviceCreateTypes.adapter = adapterCleanningTypes
                                }
                            }
                        }
                    }
                })
            }
        }
    }
}
