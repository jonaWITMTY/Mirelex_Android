package com.example.jonathangalvan.mirelex

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Requests.LoginRequest
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import android.view.ViewGroup
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Interfaces.TokenInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.onesignal.OneSignal


class MainActivity : AppCompatActivity() {

    init {
        instance = this
    }

    companion object{
        private var instance: MainActivity? = null

        fun getMainContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        /*OneSignal*/
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .setNotificationOpenedHandler {
                if(SessionModel.getSessionValue(this, "user") != ""){
                    when(SessionModel(this).getUser().person?.userTypeId){
                        "3" ->{
                            startActivity(Intent(this@MainActivity, CustomerTabsActivity::class.java))
                        }
                        "4" ->{
                            startActivity(Intent(this@MainActivity, StoreTabsActivity::class.java))
                        }
                    }
                }
            }
            .init()

        /*Go to RegisterActivity*/
        val goToRegisterlistener = View.OnClickListener { v: View ->
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        registerBtn.setOnClickListener(goToRegisterlistener)

        /*Go to Forgot password*/
        val goToForgotPassword = View.OnClickListener {
            val newAlert = UtilsModel.getAlertView().newInstance("", 2, 0)
            newAlert.show(supportFragmentManager, "alertView")
        }
        forgotPassword.setOnClickListener(goToForgotPassword)

        /*Submit Login Form*/
        val loginActionlistener = View.OnClickListener{v: View ->
            if(inputValidations()){
                val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
                val loginObj = LoginRequest(emailField.text.toString(), passwordField.text.toString())

                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this,resources.getString(R.string.userSignIn), UtilsModel.getGson().toJson(loginObj))).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                        UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                    }

                    override fun onResponse(call: Call, response: Response){
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getPostResponse(responseStr)
                        if(responseObj.status == "logged"){
                            val tokenObj: TokenInterface = UtilsModel.getGson().fromJson(responseObj.data!![0].toString(), TokenInterface::class.java)
                            SessionModel.saveSessionValue(this@MainActivity, "token", tokenObj.token)
                            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@MainActivity,resources.getString(R.string.getUserInfo))).enqueue(object: Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                    UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                    val responseUserStr = response.body()!!.string()
                                    val responseUserObj = UtilsModel.getGson().fromJson(responseUserStr, ResponseInterface::class.java)
                                    if(responseUserObj.status == "success"){
                                        SessionModel.saveSessionValue(this@MainActivity, "user", UtilsModel.getGson().toJson(responseUserObj.data!![0]))
                                        val user = SessionModel(this@MainActivity).getUser()
                                        when(user.person?.userTypeId){
                                            "3" ->{
                                                if (user.characteristics?.characteristicsId == null || user.address!!.isEmpty()){
                                                    startActivity(Intent(this@MainActivity, RegisterExtraFieldsActivity::class.java))
                                                }else{
                                                    startActivity(Intent(this@MainActivity, CustomerTabsActivity::class.java))
                                                }
                                            }
                                            "4" ->{
                                                if (user.address!!.isEmpty()){
                                                    startActivity(Intent(this@MainActivity, RegisterExtraFieldsActivity::class.java))
                                                }else{
                                                    startActivity(Intent(this@MainActivity, StoreTabsActivity::class.java))
                                                }
                                            }
                                            else ->{
                                                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorUserAccessDenied(), 1, 0).show(supportFragmentManager,"alertDialog")
                                            }
                                        }
                                    }else{
                                        UtilsModel.getAlertView().newInstance(responseUserStr, 1, 0).show(supportFragmentManager,"alertDialog")
                                    }
                                }
                            })
                        }else{
                            runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                            UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                        }
                    }
                })
            }else{
                val text = resources.getText(R.string.fillRequiredFields)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(this, text, duration).show()
            }
        }
        btnLogin.setOnClickListener(loginActionlistener)
    }

    fun inputValidations(): Boolean{
        var isCorrect = true
        if(emailField.text.toString().isEmpty()|| passwordField.text.toString().isEmpty()){
            isCorrect = false
        }
        return isCorrect
    }

    override fun onBackPressed() { }
}
