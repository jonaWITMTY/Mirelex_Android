package com.example.jonathangalvan.mirelex

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_store_detail.*

class StoreDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val getProductInfoFromBundle = intent.extras
        val productInfo = UtilsModel.getGson().fromJson(getProductInfoFromBundle.getString("productInfo"), ProductInfoInterface::class.java)

        supportActionBar?.title = ""
        if(productInfo.productOwner.person?.profilePictureUrl != null){
            Picasso.with(this).load(productInfo.productOwner.person?.profilePictureUrl).into(storeDetailImageView)
            storeDetailImageView.setOnClickListener( View.OnClickListener {
                ImagePreview().newInstance(productInfo.productOwner.person?.profilePictureUrl).show(supportFragmentManager,"alertDialog")
            })
        }
        when(productInfo.productOwner.person?.userTypeId){
            "4" -> {
                storeDetailName.text = "${productInfo.productOwner.person?.companyName}"
            }
            else -> {
                storeDetailName.text = "${productInfo.productOwner.person?.firstName} ${productInfo.productOwner.person?.paternalLastName}"
            }
        }
        storeDetailEmail.text = productInfo.productOwner.person?.email
        storeDetailPersonalPhone.text = productInfo.productOwner.person?.personalPhone
        storeDetailAddress1.text = "${productInfo.productOwner.address!![0].street}, ${productInfo.productOwner.address!![0].numExt}"
        storeDetailAddress2.text = "${productInfo.productOwner.address!![0].neighborhoodName},  ${productInfo.productOwner.address!![0].postalCode}"
        storeDetailAddress3.text = "${productInfo.productOwner.address!![0].cityName},  ${productInfo.productOwner.address!![0].stateName}"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
