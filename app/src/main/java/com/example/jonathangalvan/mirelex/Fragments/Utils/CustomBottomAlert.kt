package com.example.jonathangalvan.mirelex.Fragments.Utils

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.DialogFragment
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Interfaces.BottomAlertInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.PaymentCardsActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.DeleteCardRequest
import com.example.jonathangalvan.mirelex.Requests.SetDefaultPaymentCardRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class CustomBottomAlert: DialogFragment() {

    fun bottomSheetDialogInstance(cardId: String?): CustomBottomAlert {
        val bsd = CustomBottomAlert()
        val args = Bundle()
        args.putString("alertObj", cardId)
        bsd.arguments = args
        return bsd
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alert = BottomSheetDialog(activity!!)
        val alertObj = UtilsModel.getGson().fromJson(arguments?.getString("alertObj"), BottomAlertInterface::class.java)
        var view: View? = null

        when(alertObj.alertType){
            "payment" -> {
                /*Bottom sheet dialog paymentCard*/
                view = activity!!.layoutInflater.inflate(R.layout.fragment_custom_bottom_paymentcard_alert, null)

                /*Hide default button id card is default*/
                val setDefault = view.findViewById<View>(R.id.paymentCardAlertSetDefault)
                if(alertObj.cardDefault == "1"){
                    setDefault.visibility = View.GONE
                }

                /*Default button*/
                setDefault.setOnClickListener(View.OnClickListener {
                    val loader = layoutInflater.inflate(R.layout.view_progressbar, activity!!.findViewById(android.R.id.content), true)
                    val deleteCardObj = UtilsModel.getGson().toJson(SetDefaultPaymentCardRequest(
                        alertObj.cardId
                    ))
                    dialog.hide()
                    UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, activity!!.resources.getString(R.string.setDefaultCard), deleteCardObj)).enqueue(object:
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            activity!!.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                            UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                            onDismiss(dialog)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            activity!!.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                            val responseStr = response.body()?.string()
                            val responseObj = UtilsModel.getPostResponse(responseStr)
                            if(responseObj.status == "success"){
                                activity?.runOnUiThread {
                                    run{
                                        (activity as PaymentCardsActivity).dialogCallback()
                                    }
                                }
                            }
                            UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                            onDismiss(dialog)
                        }
                    })
                })

                /*Cancel operation*/
                val cancelButton = view.findViewById<View>(R.id.paymentCardAlertCancel)
                cancelButton.setOnClickListener(View.OnClickListener {
                    onDismiss(dialog)
                })

                /*Delete card*/
                val deleteButton = view.findViewById<View>(R.id.paymentCardAlertDelete)
                deleteButton.setOnClickListener(View.OnClickListener {

                    val loader = layoutInflater.inflate(R.layout.view_progressbar, activity!!.findViewById(android.R.id.content), true)
                    val deleteCardObj = UtilsModel.getGson().toJson(DeleteCardRequest(
                        alertObj.cardId
                    ))
                    dialog.hide()
                    UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, activity!!.resources.getString(R.string.deleteCard), deleteCardObj)).enqueue(object:
                        Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            activity!!.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                            UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                            onDismiss(dialog)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            activity!!.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                            val responseStr = response.body()?.string()
                            val responseObj = UtilsModel.getPostResponse(responseStr)
                            if(responseObj.status == "success"){
                                activity?.runOnUiThread {
                                    run{
                                        (activity as PaymentCardsActivity).dialogCallback()
                                    }
                                }
                            }
                            UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                            onDismiss(dialog)
                        }
                    })
                })
            }
        }

        alert.setContentView(view!!)
        return alert
    }
}