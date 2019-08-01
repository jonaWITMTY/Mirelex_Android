package com.example.jonathangalvan.mirelex

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_contact.*
import android.content.Intent
import android.widget.Toast
import android.content.pm.PackageManager
import android.telephony.PhoneNumberUtils
import android.content.ComponentName
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.SendContactMessageRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission



class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.contact)

        /*Get session user*/
        val currentUser = SessionModel(this).getUser()

        /*Fill email*/
        contactEmail.editText?.setText(currentUser.person?.email.toString())

        /*Whatsapp event*/
        contactWhatsapp.setOnClickListener(View.OnClickListener {
            try {
                val smsNumber = resources.getString(R.string.whatsapp)
                val sendIntent = Intent("android.intent.action.MAIN")
                sendIntent.component = ComponentName("com.whatsapp", "com.whatsapp.Conversation")
                sendIntent.putExtra(
                    "jid",
                    PhoneNumberUtils.stripSeparators(smsNumber) + "@s.whatsapp.net"
                )
                startActivity(sendIntent)
            } catch (e: PackageManager.NameNotFoundException) {
                Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        /*Phonecall number*/
        contactCallus.setOnClickListener(View.OnClickListener {
            val requiredPermission = "android.permission.CALL_PHONE"
            val checkVal = checkCallingOrSelfPermission(requiredPermission)
            if(checkVal==PackageManager.PERMISSION_GRANTED){
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:${resources.getString(R.string.phoneCall)}")
                startActivity(callIntent)
            }else{
                val permissions = arrayOf<String>(Manifest.permission.CALL_PHONE)
                ActivityCompat.requestPermissions(this, permissions, 1)
            }
        })

        /*Submit btn*/
        contactSendBtn.setOnClickListener(View.OnClickListener {
            if(inputValidations()){
                val sendContactMessageObj = SendContactMessageRequest(
                    contactName.editText?.text.toString(),
                    contactEmail.editText?.text.toString(),
                    contactSubject.editText?.text.toString(),
                    contactMessage.editText?.text.toString()
                )
                val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.sendContactMessage), UtilsModel.getGson().toJson(sendContactMessageObj))).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                        UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getPostResponse(this@ContactActivity, responseStr)
                        when(responseObj.status){
                            "success" -> {
                                UtilsModel.getAlertView().newInstance(responseStr, 4, 0).show(supportFragmentManager,"alertDialog")
                            }
                            else -> {
                                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                            }
                        }
                    }
                })
            }else{
                val text = resources.getText(R.string.fillRequiredFields)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(this, text, duration).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun confirmBtnCallback(){
        finish()
    }

    fun inputValidations(): Boolean{
        var isCorrect = true
        if(
            contactName.editText?.text.toString().isEmpty() ||
            contactEmail.editText?.text.toString().isEmpty() ||
            contactSubject.editText?.text.toString().isEmpty() ||
            contactMessage.editText?.text.toString().isEmpty()
        ){
            isCorrect = false
        }
        return isCorrect
    }
}
