package com.example.jonathangalvan.mirelex

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import com.example.jonathangalvan.mirelex.Requests.FacebookLoginRequest
import com.facebook.CallbackManager
import com.onesignal.OneSignal
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.facebook.login.LoginManager
import java.util.*
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.jonathangalvan.mirelex.Enums.UserType
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.Exception

private val REQUEST_CODE_ASK_PERMISSIONS = 1
private val REQUIRED_SDK_PERMISSIONS = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE)

class MainActivity : AppCompatActivity() {
    val callbackManager = CallbackManager.Factory.create()

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        /*App permissions*/
        checkPermissions()

        /*Facebook login*/
        var loginButtonFacebook = findViewById<View>(R.id.btnLoginFacebook)
        loginButtonFacebook.setOnClickListener(View.OnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        })

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
                val loginObj = FacebookLoginRequest(loginResult.accessToken.token)
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@MainActivity,resources.getString(R.string.userFacebookSignIn), UtilsModel.getGson().toJson(loginObj))).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                        UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                    }

                    override fun onResponse(call: Call, response: Response){
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getPostResponse(this@MainActivity, responseStr)
                        if(responseObj.status == "logged"){
                            getUserInfo(responseObj.data!![0].toString())
                            saveJsonFileForIntegration()
                        }else{
                            runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                            UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                        }
                    }
                })
            }

            override fun onCancel() {}

            override fun onError(exception: FacebookException) {
                val text = resources.getText(R.string.facebookError)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(this@MainActivity, text, duration).show()
            }
        })

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
                        val responseObj = UtilsModel.getPostResponse(this@MainActivity, responseStr)
                        if(responseObj.status == "logged"){
                            getUserInfo(responseObj.data!![0].toString())
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

    fun getUserInfo(response: String?){
        val tokenObj: TokenInterface = UtilsModel.getGson().fromJson(response, TokenInterface::class.java)
        SessionModel.saveSessionValue(this@MainActivity, "token", tokenObj.token)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@MainActivity,resources.getString(R.string.getUserInfo))).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseUserStr = response.body()!!.string()
                val responseUserObj = UtilsModel.getPostResponse(this@MainActivity, responseUserStr)
                if(responseUserObj.status == "success"){
                    SessionModel.saveSessionValue(this@MainActivity, "user", UtilsModel.getGson().toJson(responseUserObj.data!![0]))
                    val user = SessionModel(this@MainActivity).getUser()
                    saveJsonFileForIntegration()
                    when(user.person?.userTypeId){
                        "3" ->{
                            startActivity(Intent(this@MainActivity, CustomerTabsActivity::class.java))
                        }
                        "4" ->{
                            startActivity(Intent(this@MainActivity, StoreTabsActivity::class.java))
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
    }

    fun saveJsonFileForIntegration(){
        try {
            // Set up the file directory
            val filePath = Environment.getExternalStorageDirectory().toString() + "/Android/data/VestidosAr"
            val fileDirectory = File(filePath)
            fileDirectory.mkdirs();
            println("Directory created")

            // Set up the file itself
            val textFile = File(fileDirectory, "example.json")
            textFile.createNewFile()

            var isClient = ""
            when(SessionModel(this).getUser().person?.userTypeId){
                UserType.Store.userTypeId -> isClient = "0"
                UserType.Customer.userTypeId -> isClient = "1"
            }

            // Write to the file
            val fileOutputStream = FileOutputStream(textFile)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.append(isClient)
            outputStreamWriter.close()
            fileOutputStream.close()

            println("Si se creo todo bien")
        } catch (e: Exception) {
            println("Error aqui")
            println(e.message)
        }
    }

    fun checkPermissions() {
        val missingPermissions = ArrayList<String>()
        // check all required dynamic permissions
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            val permissions = missingPermissions
                .toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS)
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(
                REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                grantResults
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                for (index in permissions.indices.reversed()) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(
                            this, "Required permission '" + permissions[index]
                                    + "' not granted, exiting", Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                }
            }
        }
    }

    override fun onBackPressed() { }
}
