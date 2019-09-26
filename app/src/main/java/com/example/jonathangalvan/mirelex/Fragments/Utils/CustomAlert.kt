package com.example.jonathangalvan.mirelex.Fragments.Utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jonathangalvan.mirelex.*
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.ForgotPasswordRequest
import kotlinx.android.synthetic.main.activity_customer_tabs.view.*
import kotlinx.android.synthetic.main.fragment_custom_alert.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class CustomAlert : DialogFragment() {

    private var onSuccessFinishRes = 0
    private var alertInfo: ResponseInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if(onSuccessFinishRes == 1 && alertInfo?.status == "success"){
            activity?.finish()
        }
    }

    fun newInstance(alertInfo: String?, alertType: Int, onSuccessFinish: Int): CustomAlert {
        val ca: CustomAlert =
            CustomAlert()
        val args = Bundle()
        args.putInt("onSuccessFinish", onSuccessFinish)
        args.putString("alertInfo", alertInfo)
        args.putInt("alertType", alertType)
        ca.arguments = args
        return ca
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var alertView: View? = null
        val alert = AlertDialog.Builder(activity)
        if(arguments?.getString("alertInfo") != "") {
            alertInfo =  UtilsModel.getGson().fromJson(arguments?.getString("alertInfo"), ResponseInterface::class.java)
        }
        onSuccessFinishRes = arguments!!.getInt("onSuccessFinish")

        /*
        1 -> Basic alert
        2 -> Forgot email
        3 -> Leasable alert
        4 -> basic with confirm button
        */

        when(arguments?.getInt("alertType")){
            1 -> {
                alertView = activity?.layoutInflater!!.inflate(R.layout.fragment_custom_alert, null)
                when(alertInfo?.status){
                    "success", "failed", "noDataAvailable" -> {
                        alertView.alertTitle.text = alertInfo?.title
                        alertView.alertDescription.text = alertInfo?.desc
                    }
                    "sessionFailed" -> {
                        alertView.visibility = View.GONE
                        onDismiss(dialog)
                    }
                    "termsAndCondition"-> {
                        alertView.alertTitle.text = activity?.getString(R.string.termsAlert)
                        alertView.alertDescription.text = activity?.getString(R.string.termsAlertDescription)
                    }
                    "deniedAccess"-> {
                        alertView.alertTitle.text = activity?.getString(R.string.accessDenied)
                        alertView.alertDescription.text = activity?.getString(R.string.accessDeniedDescription)
                    }
                    else -> {
                        alertView.alertTitle.text = activity?.getString(R.string.systemError)
                        alertView.alertDescription.text = activity?.getString(R.string.systemErrorDescription)
                    }
                }
            }
            2 ->{
                alertView = activity?.layoutInflater!!.inflate(R.layout.fragment_forgot_email_alert, null)
                val btnCancel: View = alertView.findViewById(R.id.btnCancel)
                btnCancel.setOnClickListener(View.OnClickListener {
                    onDismiss(dialog)
                })

                val btnSubmit: View = alertView.findViewById(R.id.btnSubmit)
                btnSubmit.setOnClickListener(View.OnClickListener {
                    val emailField = alertView?.findViewById<TextView>(R.id.emailFieldWarp)
                    val personObj = ForgotPasswordRequest(emailField?.text.toString())
                    val loader = layoutInflater.inflate(R.layout.view_progressbar, activity!!.findViewById(android.R.id.content), true)
                    dialog.hide()
                    UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!.applicationContext, activity!!.resources.getString(R.string.forgotUser), UtilsModel.getGson().toJson(personObj))).enqueue(object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            activity!!.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                            UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                            onDismiss(dialog)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            activity!!.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                            val responseStr = response.body()?.string()
                            onDismiss(dialog)
                            UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                        }
                    })
                })
            }
            3 -> {
                alertView = activity?.layoutInflater!!.inflate(R.layout.fragment_leasable_alert, null)
                when(activity){
                    is ProductDetailActivity ->{
                        val btnCancel = alertView.findViewById<View>(R.id.btnCancel)
                        btnCancel.setOnClickListener(View.OnClickListener {
                            (activity as ProductDetailActivity).continueForLeaseble()
                            onDismiss(dialog)
                        })

                        val btnSubmit = alertView.findViewById<View>(R.id.btnSubmit)
                        btnSubmit.setOnClickListener(View.OnClickListener {
                            (activity as ProductDetailActivity).continueForFitting()
                            onDismiss(dialog)
                        })
                    }
                    is StoreTabsActivity, is CustomerTabsActivity -> {
                        alertView.findViewById<TextView>(R.id.leasableAlertTitle).text = alertInfo?.title
                        alertView.findViewById<TextView>(R.id.leasableAlertDesc).text = alertInfo?.desc

                        val btnCancel = alertView.findViewById<View>(R.id.btnCancel)
                        btnCancel.setOnClickListener(View.OnClickListener {
                            onDismiss(dialog)
                        })

                        val btnSubmit = alertView.findViewById<View>(R.id.btnSubmit)
                        btnSubmit.setOnClickListener(View.OnClickListener {
                            startActivity(Intent(activity, ProfileActivity::class.java))
                            onDismiss(dialog)
                        })
                    }
                }
            }
            4 -> {
                alertView = activity?.layoutInflater!!.inflate(R.layout.fragment_custom_alert_confirm, null)
                alertView.alertTitle.text = alertInfo?.title
                alertView.alertDescription.text = alertInfo?.desc

                val btnSubmit = alertView.findViewById<View>(R.id.btnSubmit)
                btnSubmit.setOnClickListener(View.OnClickListener {
                    when(activity){
                        is OrderCheckoutActivity -> {
                            (activity as OrderCheckoutActivity).confirmBtnCallback()
                        }
                        is ServiceCreateActivity ->{
                            (activity as ServiceCreateActivity).confirmBtnCallback()
                        }
                        is ContactActivity -> {
                            (activity as ContactActivity).confirmBtnCallback()
                        }
                        else -> {
                            onDismiss(dialog)
                        }
                    }
                })
            }
        }
        alert.setView(alertView)

        return alert.create()
    }
}
