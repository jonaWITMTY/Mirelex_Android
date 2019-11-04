package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Interfaces.DateTimeArrInterface
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreateOrderRequest
import com.example.jonathangalvan.mirelex.Requests.GetProductDatesRequest
import kotlinx.android.synthetic.main.activity_calendar.*
import java.text.SimpleDateFormat
import java.util.*
import com.squareup.timessquare.CalendarPickerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.collections.ArrayList


class CalendarActivity : AppCompatActivity() {

    private var selectedDate: String = ""
    private var unavailableDates: ArrayList<Date> = ArrayList()

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

        /*Get product info*/
        val productObj = UtilsModel.getGson().fromJson(bundleFromProductDetail.getString("productInfo"), ProductInfoInterface::class.java)

        /*Get current date*/
        val c = Calendar.getInstance().getTime()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val formattedDate = df.format(c)
        selectedDate = formattedDate

        /*Fill date array*/
        creatCalendar(ArrayList())
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val productDates = GetProductDatesRequest(productObj.productInformation.productId.toString())
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getProducDates), UtilsModel.getGson().toJson(productDates))).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@CalendarActivity, responseStr)
                if(responseObj.status == "success"){
                    val datesObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), DateTimeArrInterface::class.java)
                    val dateArr = ArrayList<Date>()
                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    for (data in datesObj.data){
                        val d = sdf.parse(data.startDate)
                        dateArr.add(d)
                    }
                    unavailableDates = dateArr
                    runOnUiThread {
                        run{
                            calendar_view.highlightDates(dateArr)
                        }
                    }
                }
            }
        })

        /*On click event*/
        calendarContinueProcess.setOnClickListener(View.OnClickListener {
            val orderRequestObj = CreateOrderRequest(orderType = bundleFromProductDetail.getString("orderType"))
            val goToOrderCheckout = Intent(this, OrderCheckoutActivity::class.java)
            val orderCheckoutBundle = Bundle()
            orderRequestObj.productId = productObj.productInformation.productId.toString()
            orderRequestObj.startDate = selectedDate
            orderRequestObj.endDate = selectedDate
            orderCheckoutBundle.putString("orderRequestObj", UtilsModel.getGson().toJson(orderRequestObj))
            orderCheckoutBundle.putString("productInfo", bundleFromProductDetail.getString("productInfo"))
            goToOrderCheckout.putExtras(orderCheckoutBundle)
            startActivity(goToOrderCheckout)
        })
    }

    fun toSimpleString(date: Date?) = with(date ?: Date()) {
        SimpleDateFormat("yyyy-MM-dd").format(this)
    }

    fun creatCalendar(dateArr: ArrayList<Date>){
        val nextYear = Calendar.getInstance()
        nextYear.add(Calendar.YEAR, 1)
        val calendar = findViewById<View>(R.id.calendar_view) as CalendarPickerView
        val today = Date()
        calendar.init(today, nextYear.time).withSelectedDate(today).withHighlightedDates(dateArr)
        calendar_view.setDateSelectableFilter(object: CalendarPickerView.DateSelectableFilter {
            override fun isDateSelectable(date: Date?): Boolean {
                return if(unavailableDates.contains(date)){
                    false
                }else{
                    selectedDate = toSimpleString(date)
                    true
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
