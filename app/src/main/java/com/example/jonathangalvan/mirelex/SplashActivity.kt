package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.onesignal.OneSignal
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        /*App orientation*/
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        /*Onesignal Init*/
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()
    }

    override fun onResume() {
        super.onResume()
        /*Check for user info*/
        val pref = getSharedPreferences("SessionData", 0)
        if(pref.contains("token")){
            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this,resources.getString(R.string.getUserInfo))).enqueue(object:
                Callback {
                override fun onFailure(call: Call, e: IOException) {
                    SessionModel(this@SplashActivity).signOutSession()
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseStr = response.body()!!.string()
                    if(responseStr.isNotEmpty()){
                        val responseUserObj = UtilsModel.getGson().fromJson(responseStr, ResponseInterface::class.java)
                        if(responseUserObj.status == "success"){
                            SessionModel.saveSessionValue(this@SplashActivity, "user", UtilsModel.getGson().toJson(responseUserObj.data!![0]))
                            val user = SessionModel(this@SplashActivity).getUser()
                            when(user.person?.userTypeId){
                                "3" ->{
                                    startActivity(Intent(this@SplashActivity, CustomerTabsActivity::class.java))
                                }
                                "4" ->{
                                    startActivity(Intent(this@SplashActivity, StoreTabsActivity::class.java))
                                }
                            }
                        }else{
                            SessionModel(this@SplashActivity).signOutSession()
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
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
