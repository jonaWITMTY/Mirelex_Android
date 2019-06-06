package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.ConversationInterface
import com.example.jonathangalvan.mirelex.Interfaces.OrderPersonInformation
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_store_detail_short.*

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

        /*Fill store fields*/
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

        /*Open chat*/
        storeDetailShortOpenChat.setOnClickListener(View.OnClickListener{
            val bundleToChat = Bundle()
            val goToChat = Intent(this, ChatActivity::class.java)
            val sessionUser = SessionModel(this).getUser()
            val conversationObj = ConversationInterface(
                ownerObj.userId,
                if(ownerObj.companyName != null) ownerObj.companyName  else "${ownerObj.firstName } ${ownerObj.paternalLastName }",
                sessionUser.person?.userId
            )
            bundleToChat.putString("conversationObj", UtilsModel.getGson().toJson(conversationObj))
            goToChat.putExtras(bundleToChat)
            startActivity(goToChat)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
