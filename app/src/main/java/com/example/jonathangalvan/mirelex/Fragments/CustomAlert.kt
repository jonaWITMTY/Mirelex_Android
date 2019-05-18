package com.example.jonathangalvan.mirelex.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.OrderCheckoutActivity
import com.example.jonathangalvan.mirelex.ProductDetailActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.ForgotPasswordRequest
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

    fun newInstance(alertInfo: String?, alertType: Int, onSuccessFinish: Int): CustomAlert{
        val ca: CustomAlert = CustomAlert()
        val args = Bundle()
        args.putInt("onSuccessFinish", onSuccessFinish)
        args.putString("alertInfo", alertInfo)
        args.putInt("alertType", alertType)
        ca.setArguments(args)
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
                alertView.alertTitle.text = alertInfo?.title
                alertView.alertDescription.text = alertInfo?.desc
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
                    UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!.applicationContext, activity!!.resources.getString(R.string.forgotUser), UtilsModel.getGson().toJson(personObj))).enqueue(object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                            onDismiss(dialog)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseStr = response.body()?.string()
                            onDismiss(dialog)
                            UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                        }
                    })
                })
            }
            3 -> {
                alertView = activity?.layoutInflater!!.inflate(R.layout.fragment_leasable_alert, null)
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
