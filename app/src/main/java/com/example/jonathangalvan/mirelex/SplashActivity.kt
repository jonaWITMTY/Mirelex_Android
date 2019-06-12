package com.example.jonathangalvan.mirelex

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class SplashActivity : AppCompatActivity() {

    init {
        instance = this
    }

    companion object{
        private var instance: SplashActivity? = null

        fun getMainContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        /*Check for user info*/
        if(SessionModel.getSessionValue(this, "token") != ""){
            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this,resources.getString(R.string.getUserInfo))).enqueue(object:
                Callback {
                override fun onFailure(call: Call, e: IOException) {
                    SessionModel(this@SplashActivity).signOutSession()
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseUserObj = UtilsModel.getGson().fromJson(response.body()!!.string(), ResponseInterface::class.java)
                    if(responseUserObj.status == "success"){
                        SessionModel.saveSessionValue(this@SplashActivity, "user", UtilsModel.getGson().toJson(responseUserObj.data!![0]))
                        val user = SessionModel(this@SplashActivity).getUser()
                        when(user.person?.userTypeId){
                            "3" ->{
                                if (user.characteristics?.characteristicsId == null || user.address!!.isEmpty()){
                                    startActivity(Intent(this@SplashActivity, RegisterExtraFieldsActivity::class.java))
                                }else{
                                    startActivity(Intent(this@SplashActivity, CustomerTabsActivity::class.java))
                                }
                            }
                            "4" ->{
                                if (user.address!!.isEmpty()){
                                    startActivity(Intent(this@SplashActivity, RegisterExtraFieldsActivity::class.java))
                                }else{
                                    startActivity(Intent(this@SplashActivity, StoreTabsActivity::class.java))
                                }
                            }
                            else ->{
                                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorUserAccessDenied(), 1, 0).show(supportFragmentManager,"alertDialog")
                            }
                        }
                    }else{
                        SessionModel(this@SplashActivity).signOutSession()
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    }
                }
            })
        }else{
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        }
    }

    override fun onBackPressed() { }
}
