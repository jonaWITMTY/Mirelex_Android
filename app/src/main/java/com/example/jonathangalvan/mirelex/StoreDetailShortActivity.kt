package com.example.jonathangalvan.mirelex

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.ConversationInterface
import com.example.jonathangalvan.mirelex.Interfaces.OrderPersonInformation
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_store_detail_short.*
import android.content.pm.ApplicationInfo
import android.content.ActivityNotFoundException





class StoreDetailShortActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_detail_short)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Get bundle info*/
        val bundleFromServiceDetail = intent.extras
        val ownerObj = UtilsModel.getGson().fromJson(bundleFromServiceDetail.getString("personObj"), OrderPersonInformation::class.java)

        /*Fill image*/
        if(ownerObj.profilePictureUrl != null){
            Picasso.with(this).load(ownerObj.profilePictureUrl).into(storeDetailShortImageView)
            storeDetailShortImageView.setOnClickListener( View.OnClickListener {
                ImagePreview().newInstance(ownerObj.profilePictureUrl).show(supportFragmentManager,"alertDialog")
            })
        }

        /*Hide social buttons if empty*/
        if(ownerObj?.facebookUrl == null){
            storeDetailShortFacebook.visibility = View.GONE
        }

        if(ownerObj?.instagramUrl == null){
            storeDetailShortInstagram.visibility = View.GONE
        }

        if(ownerObj?.facebookUrl == null && ownerObj?.instagramUrl  == null){
            storeDetailShortSocialTitle.visibility = View.GONE
        }

        /*Fill store fields*/
        var storeName : String = ""
        if(ownerObj.companyName == null){
            storeName = "${ownerObj.firstName} ${ownerObj.paternalLastName}"
            storeDetailShortFacebook.visibility = View.GONE
            storeDetailShortInstagram.visibility = View.GONE
        }else{
            storeName = ownerObj.companyName!!

            /*Set social click events*/
            storeDetailShortFacebook.setOnClickListener(View.OnClickListener {
                newFacebookIntent(this.packageManager, ownerObj.facebookUrl!!)
            })

            storeDetailShortInstagram.setOnClickListener(View.OnClickListener {
                newInstagramIntent(ownerObj.instagramUrl)
            })
        }
        supportActionBar?.title = storeName
        storeDetailShortName.text = storeName
        storeDetailShortPhone.text = ownerObj.personalPhone
        storeDetailShortEmail.text = ownerObj.email

        /*Open chat*/
        storeDetailShortOpenChat.setOnClickListener(View.OnClickListener{
            val bundleToChat = Bundle()
            val goToChat = Intent(this, ChatActivity::class.java)
            val sessionUser = SessionModel(this).getUser()
            val conversationObj = ConversationInterface(
                userIdTo = ownerObj.userId,
                userTo = if(ownerObj.companyName != null) ownerObj.companyName  else "${ownerObj.firstName } ${ownerObj.paternalLastName }",
                userIdFrom = sessionUser.person?.userId
            )
            bundleToChat.putString("conversationObj", UtilsModel.getGson().toJson(conversationObj))
            goToChat.putExtras(bundleToChat)
            startActivity(goToChat)
        })

        /*Email click event*/
        storeDetailShortEmail.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "plain/text"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(ownerObj.email))
            startActivity(Intent.createChooser(intent, ""))
        })

        /*Phone click event*/
        storeDetailShortPhone.setOnClickListener(View.OnClickListener {
            val requiredPermission = "android.permission.CALL_PHONE"
            val checkVal = checkCallingOrSelfPermission(requiredPermission)
            if(checkVal== PackageManager.PERMISSION_GRANTED){
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:+${ownerObj.personalPhone}")
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
