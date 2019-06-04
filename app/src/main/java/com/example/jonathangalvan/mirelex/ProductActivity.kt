package com.example.jonathangalvan.mirelex

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Fragments.Product.ProductImagePicker
import com.example.jonathangalvan.mirelex.Fragments.Product.ProductUpdate
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ViewModels.ProductViewModel
import okhttp3.*
import java.io.IOException
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.os.Build
import okhttp3.internal.Util


class ProductActivity : AppCompatActivity(),
    ProductUpdate.OnFragmentInteractionListener,
    ProductImagePicker.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val vmp = ViewModelProviders.of(this).get(ProductViewModel::class.java)
        val bunldeFromProducts = intent.extras

        if(intent.extras != null){
            val productId = bunldeFromProducts.getString("productId")
            vmp.productId = productId
            openTab(ProductUpdate(), "productUpdate")
        }else{

        }
    }

    fun openTab(fragment: Fragment, tag: String){
        val transaction = supportFragmentManager.beginTransaction()
        if(supportFragmentManager.findFragmentById(R.id.productActivityContainer) != null){
            transaction.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
        }
        transaction.replace(R.id.productActivityContainer, fragment)
        transaction.addToBackStack(tag)
        transaction.commit()
    }

    fun setActionBarTitle(title: String){
        supportActionBar?.title = title
    }

    fun doFinalProductProcess(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val vmp = ViewModelProviders.of(this).get(ProductViewModel::class.java)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.updateProduct), UtilsModel.getGson().toJson(vmp.productObjRequest))).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    if(UtilsModel.getImagePermissions(this@ProductActivity)){
                        UtilsModel.getOkClient().newCall(UtilsModel.postImageRequest( this@ProductActivity, vmp.featuredImage!!, resources.getString(R.string.uploadFeatureImage), vmp.productObj?.productInformation?.productId.toString())).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                val responseStr = response.body()?.string()
                                val responseObj = UtilsModel.getPostResponse(responseStr)
                                UtilsModel.getAlertView().newInstance(responseStr, 1, 1).show(supportFragmentManager,"alertDialog")
                            }
                        })
                    }else{
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                    }
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        val fragmentObj = supportFragmentManager.findFragmentById(R.id.productActivityContainer)
        if(fragmentObj is ProductUpdate){
            finish()
        }else{
            super.onBackPressed()
        }
    }

    override fun onFragmentInteraction(uri: Uri) {}
}
