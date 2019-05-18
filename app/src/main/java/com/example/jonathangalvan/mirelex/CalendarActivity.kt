package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreateOrderRequest
import kotlinx.android.synthetic.main.activity_calendar.*
import java.text.SimpleDateFormat
import java.util.*


class CalendarActivity : AppCompatActivity() {

    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Get bundle information*/
        val bundleFromProductDetail = intent.extras
        when(bundleFromProductDetail.getString("orderType")){
            OrderType.Lease.orderTypeId -> {
                supportActionBar?.title = resources.getString(R.string.leaseable)
            }
            OrderType.Fitting.orderTypeId -> {
                supportActionBar?.title = resources.getString(R.string.fitting)
            }
        }

        /*Calendar event on date change*/
        val c = Calendar.getInstance().getTime()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val formattedDate = df.format(c)
        selectedDate = formattedDate
        calendarDateChooser.setOnDateChangeListener(CalendarView.OnDateChangeListener { view, year, month, dayOfMonth ->
            var newMonth: String = ""
            var realMonth = month +1
            if(realMonth < 10){
                newMonth = "0$realMonth"
            }else{
                newMonth = realMonth.toString()
            }
            selectedDate = "$year-$newMonth-$dayOfMonth"
        })

        /*On click event*/
        calendarContinueProcess.setOnClickListener(View.OnClickListener {
            val orderRequestObj = CreateOrderRequest(orderType = bundleFromProductDetail.getString("orderType"))
            val goToOrderCheckout = Intent(this, OrderCheckoutActivity::class.java)
            val orderCheckoutBundle = Bundle()
            val productObj = UtilsModel.getGson().fromJson(bundleFromProductDetail.getString("productInfo"), ProductInfoInterface::class.java)
            orderRequestObj.productId = productObj.productInformation.productId.toString()
            orderRequestObj.startDate = selectedDate
            orderRequestObj.endDate = selectedDate
            orderCheckoutBundle.putString("orderRequestObj", UtilsModel.getGson().toJson(orderRequestObj))
            orderCheckoutBundle.putString("productInfo", bundleFromProductDetail.getString("productInfo"))
            goToOrderCheckout.putExtras(orderCheckoutBundle)
            startActivity(goToOrderCheckout)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
