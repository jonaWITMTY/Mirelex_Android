package com.example.jonathangalvan.mirelex

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.OrderPersonInformation
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_store_detail_short.*

class StoreDetailShortActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail_short)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundleFromServiceDetail = intent.extras
        val ownerObj = UtilsModel.getGson().fromJson(bundleFromServiceDetail.getString("personObj"), OrderPersonInformation::class.java)

        if(ownerObj.profilePictureUrl != null){
            Picasso.with(this).load(ownerObj.profilePictureUrl).into(storeDetailShortImageView)
            storeDetailShortImageView.setOnClickListener( View.OnClickListener {
                ImagePreview().newInstance(ownerObj.profilePictureUrl).show(supportFragmentManager,"alertDialog")
            })
        }

        var storeName : String = ""
        if(ownerObj.companyName == null){
            storeName = "${ownerObj.firstName} ${ownerObj.paternalLastName}"
        }else{
            storeName = ownerObj.companyName!!
        }
        supportActionBar?.title = storeName
        storeDetailShortName.text = storeName
        storeDetailShortPhone.text = ownerObj.personalPhone
        storeDetailShortEmail.text = ownerObj.email
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
