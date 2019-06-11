package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.PaymentCardsAdapter
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomBottomAlert
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Interfaces.UserInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import kotlinx.android.synthetic.main.activity_payment_cards.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class PaymentCardsActivity : AppCompatActivity() {

    var paymentAdapter: PaymentCardsAdapter = PaymentCardsAdapter(this, ArrayList())
    var sessionUser: UserInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_cards)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.paymentMethods)

        /*Get current paymentCards and fill list*/
        sessionUser = SessionModel(this).getUser()
        paymentCardsList.layoutManager = LinearLayoutManager(this)
        paymentCardsList.adapter = paymentAdapter

        /*Onclick card*/
        paymentCardsList.addOnItemTouchListener(RecyclerItemClickListener(this, paymentCardsList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                runOnUiThread {
                    run {
                        CustomBottomAlert().bottomSheetDialogInstance(paymentAdapter.getItem(position).cardId).show(supportFragmentManager, "alert")
                    }
                }
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))
    }

    fun dialogCallback(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this,resources.getString(R.string.getUserInfo))).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseUserObj = UtilsModel.getGson().fromJson(response.body()!!.string(), ResponseInterface::class.java)
                if(responseUserObj.status == "success"){
                    SessionModel.saveSessionValue(this@PaymentCardsActivity, "user", UtilsModel.getGson().toJson(responseUserObj.data!![0]))
                    val user = SessionModel(this@PaymentCardsActivity).getUser()
                    sessionUser = user
                    runOnUiThread {
                        run {
                            paymentAdapter.loadPaymentCards(sessionUser!!.paymentCards)
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        paymentAdapter.loadPaymentCards(sessionUser!!.paymentCards)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
