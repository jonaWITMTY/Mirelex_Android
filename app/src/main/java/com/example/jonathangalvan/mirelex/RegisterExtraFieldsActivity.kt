package com.example.jonathangalvan.mirelex

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Fragments.RegisterExtraFields.RegisterExtraFieldsAddress
import com.example.jonathangalvan.mirelex.Fragments.RegisterExtraFields.RegisterExtraFieldsInfo
import com.example.jonathangalvan.mirelex.Fragments.RegisterExtraFields.RegisterExtraFieldsMeasure
import com.example.jonathangalvan.mirelex.Interfaces.CatalogArrayInterface
import com.example.jonathangalvan.mirelex.Interfaces.WomenCatalogsInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.SizesRequest
import com.example.jonathangalvan.mirelex.ViewModels.RegisterViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class RegisterExtraFieldsActivity : AppCompatActivity(),
    RegisterExtraFieldsInfo.OnFragmentInteractionListener,
    RegisterExtraFieldsAddress.OnFragmentInteractionListener,
    RegisterExtraFieldsMeasure.OnFragmentInteractionListener{

    private var viewModel: RegisterViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_extra_fields)
        supportActionBar?.hide()
        val user = SessionModel(this).getUser()
        viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)
        viewModel?.setUser(user)
        openTab(RegisterExtraFieldsInfo(), "info")
        var productTypeId = 1
        when(user.person?.userTypeId) {
            "3" -> {
                if(user.person?.userGenderId == "1"){
                    productTypeId = 2
                }
            }
        }
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val sizesObj = SizesRequest(productTypeId)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( this, resources.getString(R.string.userSizes), UtilsModel.getGson().toJson(sizesObj))).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val sizesObj = UtilsModel.getGson().fromJson(responseStr, CatalogArrayInterface::class.java)
                    viewModel?.sizes?.postValue(sizesObj.data)
                    runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                }
            }
        })

        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( this, resources.getString(R.string.womanCatalogs))).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val womenCatalogObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), WomenCatalogsInterface::class.java)
                    viewModel?.womenCatalogs?.postValue(womenCatalogObj)
                }
            }
        })
    }

    fun openTab(fragment: Fragment, tag: String){
        val transaction = supportFragmentManager.beginTransaction()
        if(supportFragmentManager.findFragmentById(R.id.registerExtraFieldsFrame) != null){
            transaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
        }
        transaction.replace(R.id.registerExtraFieldsFrame, fragment)
        transaction.addToBackStack(tag)
        transaction.commit()
    }

    fun doUserRegister(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar,findViewById(android.R.id.content), true)
        val companyRegisterObj = UtilsModel.getGson().toJson(viewModel?.comleteUserCall?.value)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( this, resources.getString(R.string.updateUser), companyRegisterObj)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                val responseRegisterStr = response.body()?.string()
                val responseRegisterObj = UtilsModel.getPostResponse(responseRegisterStr)
                if(responseRegisterObj.status == "success"){
                    when(viewModel?.userCall?.value?.person?.userTypeId){
                        "3" -> {
                            SessionModel.saveSessionValue(this@RegisterExtraFieldsActivity, "user", UtilsModel.getGson().toJson(responseRegisterObj.data!![0]))
                            startActivity(Intent(this@RegisterExtraFieldsActivity, CustomerTabsActivity::class.java))
                        }
                        "4" -> {
                            //Esta entra en fase 2
                        }
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        val fragmentObj = supportFragmentManager.findFragmentById(R.id.registerExtraFieldsFrame)
        if(fragmentObj is RegisterExtraFieldsAddress || fragmentObj is RegisterExtraFieldsMeasure){
            super.onBackPressed()
        }
    }

    override fun onFragmentInteraction(uri: Uri) { }
}
