package com.example.jonathangalvan.mirelex

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_payment_card_create.*
import java.util.*
import kotlin.collections.ArrayList
import org.json.JSONObject
import io.conekta.conektasdk.Token.CreateToken
import io.conekta.conektasdk.Conekta
import android.app.Activity
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Interfaces.ConektaCardPairsnterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreatePaymentCard
import io.conekta.conektasdk.Card
import io.conekta.conektasdk.Token
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class PaymentCardCreateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_card_create)
        supportActionBar?.hide()

        /*Close activity event*/
        paymentCardCreateClose.setOnClickListener(View.OnClickListener {
            finish()
        })

        /*Fill month spinner*/
        var months = ArrayList<String>()
        for (i in 1.. 12){
            months.add(i.toString())
        }
        fillSpinner(months, findViewById(R.id.paymentCardCreateMonth))

        /*Fill year spinner*/
        val years = ArrayList<String>()
        for (i in Calendar.getInstance().get(Calendar.YEAR).. (Calendar.getInstance().get(Calendar.YEAR) + 10)){
            years.add(i.toString())
        }
        fillSpinner(years, findViewById(R.id.paymentCardCreateYear))

        /*On create card event*/
        paymentCardCreateBtn.setOnClickListener(View.OnClickListener {
            if(inputValidations()){
                val activity = this
                Conekta.setPublicKey("key_BqcAYyRc1rUFAT4Kr3kYEgA")
                Conekta.collectDevice(activity)

                val card = Card(
                    paymentCardCreateName.editText?.text.toString(),
                    paymentCardCreateNumber.editText?.text.toString(),
                    paymentCardCreateCVC.editText?.text.toString(),
                    months[paymentCardCreateMonth.selectedItemPosition],
                    years[paymentCardCreateYear.selectedItemPosition]
                )
                val token = Token(activity)

                token.onCreateTokenListener {
                    try {
                        val cardObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(it), ConektaCardPairsnterface::class.java)
                        if(cardObj.nameValuePairs.`object` != "error"){
                            val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
                            val cardObjRequest = UtilsModel.getGson().toJson(CreatePaymentCard(
                                cardObj.nameValuePairs.id
                            ))
                            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.createCard), cardObjRequest)).enqueue(object: Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                    UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                    val responseStr = response.body()?.string()
                                    val responseObj = UtilsModel.getPostResponse(this@PaymentCardCreateActivity, responseStr)
                                    UtilsModel.getAlertView().newInstance(responseStr, 1, 1).show(supportFragmentManager,"alertDialog")
                                }
                            })
                        }else{
                            val text = cardObj.nameValuePairs.message
                            val duration = Toast.LENGTH_SHORT
                            Toast.makeText(this, text, duration).show()
                        }
                    } catch (err: Exception) {
                        //Do something on error
                    }
                }
                token.create(card)
            }else{
                val text = resources.getText(R.string.fillRequiredFields)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(this, text, duration).show()
            }
        })
    }

    fun inputValidations(): Boolean{
        var isCorrect = true
        if(
            paymentCardCreateName.editText?.text.toString().isEmpty() ||
            paymentCardCreateNumber.editText?.text.toString().isEmpty() ||
            paymentCardCreateCVC.editText?.text.toString().isEmpty()
        ){
            isCorrect = false
        }
        return isCorrect
    }

    fun fillSpinner(data: ArrayList<String>?, adapterView: AdapterView<ArrayAdapter<String>>){
        if(data != null){
            val adapter = ArrayAdapter<String>(this, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, data)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
            adapterView.adapter = adapter
        }
    }
}
