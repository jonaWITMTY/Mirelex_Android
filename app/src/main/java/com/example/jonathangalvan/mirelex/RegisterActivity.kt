package com.example.jonathangalvan.mirelex

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.example.jonathangalvan.mirelex.Adapters.RegisterTabsAdapter
import com.example.jonathangalvan.mirelex.Fragments.RegisterCustomerTab
import kotlinx.android.synthetic.main.activity_register.*
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Fragments.RegisterStoreTab
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Interfaces.TokenInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.LoginRequest
import com.example.jonathangalvan.mirelex.Requests.RegisterRequest
import com.example.jonathangalvan.mirelex.ViewModels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register_customer_tab.view.*
import kotlinx.android.synthetic.main.fragment_register_store_tab.view.*
import okhttp3.*
import java.io.IOException


class RegisterActivity : AppCompatActivity(), RegisterCustomerTab.OnFragmentInteractionListener,
    RegisterStoreTab.OnFragmentInteractionListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.colorGrey)))
        supportActionBar?.elevation = 0F
        val viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)

        /*Tabs*/
        val fragmentAdapter = RegisterTabsAdapter(supportFragmentManager)
        viewPagerRegisterTabs.adapter = fragmentAdapter
        registerTabs.setupWithViewPager(viewPagerRegisterTabs)
        viewPagerRegisterTabs.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    0 -> {
                        val params = viewPagerRegisterTabs.getLayoutParams()
                        params.height = UtilsModel.dptopx(this@RegisterActivity,220)
                        viewPagerRegisterTabs.requestLayout()
                    }
                    1 -> {
                        val params = viewPagerRegisterTabs.getLayoutParams()
                        params.height = UtilsModel.dptopx(this@RegisterActivity,100)
                        viewPagerRegisterTabs.requestLayout()
                    }
                }
            }
        })

        /*RegisterCLickListener*/
        val listener = View.OnClickListener{
            val loader = layoutInflater.inflate(R.layout.view_progressbar,findViewById(android.R.id.content), true)
            val registerObj = RegisterRequest(
                registerEmailField?.text.toString(),
                registerPasswordField?.text.toString(),
                viewPagerRegisterTabs.registerNameField?.text.toString(),
                viewPagerRegisterTabs.registerPaternalLastNameField?.text.toString(),
                viewPagerRegisterTabs.registerCompanyNameField?.text.toString(),
                viewModel.genderCall.value!![viewPagerRegisterTabs.genderField.selectedItemPosition].genderId,
                viewPagerRegisterTabs.currentItem
            )

            if(termsCheckbox.isChecked){
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.createUser), UtilsModel.getGson().toJson(registerObj))).enqueue(object : Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                        UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                        val responseRegisterStr = response.body()?.string()
                        val responseRegisterObj = UtilsModel.getPostResponse(responseRegisterStr)

                        if(responseRegisterObj.status == "success"){
                            val loginObj = LoginRequest(registerEmailField.text.toString(), registerPasswordField.text.toString())
                            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@RegisterActivity,resources.getString(R.string.userSignIn), UtilsModel.getGson().toJson(loginObj))).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                    UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                                }

                                override fun onResponse(call: Call, response: Response){
                                    val responseLoginStr = response.body()?.string()
                                    val responseLoginObj = UtilsModel.getPostResponse(responseLoginStr)
                                    if(responseLoginObj.status == "logged"){
                                        val tokenObj: TokenInterface = UtilsModel.getGson().fromJson(responseLoginObj.data!![0].toString(), TokenInterface::class.java)
                                        SessionModel.saveSessionValue(this@RegisterActivity, "token", tokenObj.token)
                                        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@RegisterActivity,resources.getString(R.string.getUserInfo))).enqueue(object: Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                                            }

                                            override fun onResponse(call: Call, response: Response) {
                                                val responseUserObj = UtilsModel.getGson().fromJson(response.body()!!.string(), ResponseInterface::class.java)
                                                SessionModel.saveSessionValue(this@RegisterActivity, "user", UtilsModel.getGson().toJson(responseUserObj.data!![0]))
                                                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                                startActivity(Intent(this@RegisterActivity, RegisterExtraFieldsActivity::class.java))
                                            }
                                        })
                                    }else{
                                        UtilsModel.getAlertView().newInstance(responseLoginStr, 1, 0).show(supportFragmentManager,"alertDialog")
                                    }
                                }
                            })
                        }else{
                            val newAlert = UtilsModel.getAlertView().newInstance(responseRegisterStr, 1, 1)
                            newAlert.show(supportFragmentManager, "alertView")
                        }
                    }
                })
            }else{
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorMissingTerms(), 1, 0).show(supportFragmentManager, "alertView")
            }
        }
        registerSubmitBtn.setOnClickListener(listener)

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onFragmentInteraction(uri: Uri) { }
}
