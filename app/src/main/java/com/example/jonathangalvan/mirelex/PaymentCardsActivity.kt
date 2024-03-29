package com.example.jonathangalvan.mirelex

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.PaymentCardsAdapter
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomBottomAlert
import com.example.jonathangalvan.mirelex.Interfaces.BottomAlertInterface
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Interfaces.UserInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import kotlinx.android.synthetic.main.activity_payment_cards.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
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
        paymentCardsList.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        paymentCardsList.adapter = paymentAdapter

        /*Onclick card*/
        paymentCardsList.addOnItemTouchListener(RecyclerItemClickListener(this, paymentCardsList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                runOnUiThread {
                    run {
                        val parameterObj = UtilsModel.getGson().toJson(BottomAlertInterface(
                            alertType = "payment",
                            cardDefault = paymentAdapter.getItem(position).default,
                            cardId = paymentAdapter.getItem(position).cardId
                        ))
                        CustomBottomAlert().bottomSheetDialogInstance(parameterObj).show(supportFragmentManager, "alert")
                    }
                }
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))
    }

    fun dialogCallback(){
        updateUser()
    }

    override fun onResume() {
        super.onResume()
        updateUser()
    }

    fun updateUser(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this,resources.getString(R.string.getUserInfo))).enqueue(object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseUserObj = UtilsModel.getPostResponse(this@PaymentCardsActivity, response.body()!!.string())
                if(responseUserObj.status == "success"){
                    SessionModel.saveSessionValue(this@PaymentCardsActivity, "user", UtilsModel.getGson().toJson(responseUserObj.data!![0]))
                    val user = SessionModel(this@PaymentCardsActivity).getUser()
                    sessionUser = user
                    runOnUiThread {
                        run {
                            findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById<ViewGroup>(R.id.viewCenteredMessage))
                            paymentAdapter.loadPaymentCards(sessionUser!!.paymentCards)
                            if(sessionUser!!.paymentCards.isNullOrEmpty()){
                                val ceneteredLayout = layoutInflater.inflate(R.layout.view_centered_message, findViewById(android.R.id.content), true)
                                ceneteredLayout.centeredMessage.text = resources.getString(R.string.noDataAvailable)
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.paymentCardAdd -> {
                val goToServiceCreate = Intent(this, PaymentCardCreateActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(this,  R.anim.abc_slide_in_bottom,  R.anim.abc_fade_out)
                startActivity(goToServiceCreate, options.toBundle())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.payment_icons, menu)
        menu?.getItem(0)?.icon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, android.R.color.white))
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
