package com.example.jonathangalvan.mirelex

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
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
        val productInfo = UtilsModel.getGson().fromJson(getProductInfoFromBundle?.getString("productInfo"), ProductInfoInterface::class.java)

        supportActionBar?.title = ""
        if(productInfo.productOwner.person?.profilePictureUrl != null){
            Picasso.with(this).load(productInfo.productOwner.person?.profilePictureUrl).into(storeDetailImageView)
            storeDetailImageView.setOnClickListener( View.OnClickListener {
                ImagePreview().newInstance(productInfo.productOwner.person?.profilePictureUrl).show(supportFragmentManager,"alertDialog")
            })
        }

        /*Hide social buttons if empty*/
        if(productInfo.productOwner.person?.facebookUrl == null){
            storeDetailFacebook.visibility = View.GONE
        }

        if(productInfo.productOwner.person?.instagramUrl == null){
            storeDetailInstagram.visibility = View.GONE
        }

        if(productInfo.productOwner.person?.facebookUrl == null && productInfo.productOwner.person?.instagramUrl == null){
            storeDetailSocialTitle.visibility = View.GONE
        }

        var name: String? = ""
        when(productInfo.productOwner.person?.userTypeId){
            "4" -> {
                name = "${productInfo.productOwner.person?.companyName}"
                /*Set social click events*/
                storeDetailFacebook.setOnClickListener(View.OnClickListener {
                    newFacebookIntent(this.packageManager, productInfo.productOwner.person?.facebookUrl!!)
                })

                storeDetailInstagram.setOnClickListener(View.OnClickListener {
                    newInstagramIntent(productInfo.productOwner.person?.instagramUrl!!)
                })
            }
            else -> {
                name = "${productInfo.productOwner.person?.firstName} ${productInfo.productOwner.person?.paternalLastName}"
                storeDetailFacebook.visibility = View.GONE
                storeDetailInstagram.visibility = View.GONE
            }
        }
        storeDetailName.text = name
        supportActionBar?.title = name

        storeDetailEmail.text = productInfo.productOwner.person?.email
        storeDetailPersonalPhone.text = productInfo.productOwner.person?.personalPhone
        if(productInfo.productOwner.address != null){
            storeDetailAddress1.text = "${productInfo.productOwner.address!![0].street}, ${productInfo.productOwner.address!![0].numExt}"
            storeDetailAddress2.text = "${productInfo.productOwner.address!![0].neighborhoodName},  ${productInfo.productOwner.address!![0].postalCode}"
            storeDetailAddress3.text = "${productInfo.productOwner.address!![0].cityName},  ${productInfo.productOwner.address!![0].stateName}"
        }else{
            storeDetailAddress1.visibility  = View.GONE
            storeDetailAddress2.visibility  = View.GONE
            storeDetailAddress3.visibility  = View.GONE
        }

        /*Email click event*/
        storeDetailEmail.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "plain/text"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(productInfo.productOwner.person?.email))
            startActivity(Intent.createChooser(intent, ""))
        })

        /*Phone click event*/
        storeDetailPersonalPhone.setOnClickListener(View.OnClickListener {
            val requiredPermission = "android.permission.CALL_PHONE"
            val checkVal = checkCallingOrSelfPermission(requiredPermission)
            if(checkVal== PackageManager.PERMISSION_GRANTED){
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:+${productInfo.productOwner.person?.personalPhone}")
                startActivity(callIntent)
            }else{
                val permissions = arrayOf<String>(Manifest.permission.CALL_PHONE)
                ActivityCompat.requestPermissions(this, permissions, 1)
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun newFacebookIntent(pm: PackageManager, url: String) {
        var uri = Uri.parse(url)
        try {
            val applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0)
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=$url")
            }
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    fun newInstagramIntent(url: String?){
        val uri = Uri.parse(url)
        val likeIng = Intent(Intent.ACTION_VIEW, uri)
        likeIng.setPackage("com.instagram.android")
        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        }
    }
}
