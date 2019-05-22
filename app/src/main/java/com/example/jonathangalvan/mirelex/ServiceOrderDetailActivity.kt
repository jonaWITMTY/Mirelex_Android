package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.jonathangalvan.mirelex.Interfaces.ServiceInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_service_order_detail.*
import kotlinx.android.synthetic.main.view_detail_info_row_with_title.view.*

class ServiceOrderDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_order_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundleFromServices = intent.extras
        val serviceObj = UtilsModel.getGson().fromJson(bundleFromServices.getString("serviceObj"), ServiceInterface::class.java)

        supportActionBar?.title = serviceObj.folio

        addRow(resources.getString(R.string.type), serviceObj.orderType!!)
        addRow(resources.getString(R.string.date), serviceObj.startDate!!)
        addRow(resources.getString(R.string.status), serviceObj.orderStatus!!)
        addRow(resources.getString(R.string.total), serviceObj.totalFormatted!!)

        Picasso.with(this).load(serviceObj.owner.profilePictureUrl).into(serviceDetailStoreImg)
        if(serviceObj.owner.companyName == null){
            serviceDetailStoreName.text = "${serviceObj.owner.firstName} ${serviceObj.owner.paternalLastName}"
        }else{
            serviceDetailStoreName.text = serviceObj.owner.companyName
        }

        /*On store click*/
        serviceDetailStore.setOnClickListener(View.OnClickListener {
            val goToStoreDetailShort = Intent(this, StoreDetailShortActivity::class.java)
            val bundleToStoreDetailShort = Bundle()
            bundleToStoreDetailShort.putString("ownerObj",UtilsModel.getGson().toJson(serviceObj.owner))
            goToStoreDetailShort.putExtras(bundleToStoreDetailShort)
            startActivity(goToStoreDetailShort)
        })
    }

    fun addRow(title: String, value: String){
        val insertRow = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, serviceDetailInfo, false)
        insertRow.detailInfoNameView.text = title
        insertRow.detailInfoValueView.text = value
        serviceDetailInfo.addView(insertRow)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
