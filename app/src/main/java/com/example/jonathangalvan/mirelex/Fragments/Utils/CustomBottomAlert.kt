package com.example.jonathangalvan.mirelex.Fragments.Utils

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Interfaces.BottomAlertInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.OrderCheckoutActivity
import com.example.jonathangalvan.mirelex.OrderDetailActivity
import com.example.jonathangalvan.mirelex.PaymentCardsActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.CreateFittingMeasurementsRequest
import com.example.jonathangalvan.mirelex.Requests.DeleteCardRequest
import com.example.jonathangalvan.mirelex.Requests.SetDefaultPaymentCardRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class CustomBottomAlert: DialogFragment() {

    fun bottomSheetDialogInstance(alertObj: String?): CustomBottomAlert {
        val bsd = CustomBottomAlert()
        val args = Bundle()
        args.putString("alertObj", alertObj)
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
                                        when(activity){
                                            is PaymentCardsActivity -> {
                                                (activity as PaymentCardsActivity).dialogCallback()
                                            }
                                        }
                                    }
                                }
                            }
                            UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                            onDismiss(dialog)
                        }
                    })
                })
            }
            "fittingOrderProcess" -> {
                view = activity!!.layoutInflater.inflate(R.layout.fragment_add_sizes_alert, null)

                /*Clicks measures "?"*/
                view.findViewById<View>(R.id.imagePreviewHeight).setOnClickListener(View.OnClickListener {
                    ImagePreview().newInstance(resources.getString(R.string.heightImage)).show(fragmentManager, "alertDialog")
                })

                view.findViewById<View>(R.id.imagePreviewBust).setOnClickListener(View.OnClickListener {
                    ImagePreview().newInstance(resources.getString(R.string.bustImage)).show(fragmentManager, "alertDialog")
                })

                view.findViewById<View>(R.id.imagePreviewWaist).setOnClickListener(View.OnClickListener {
                    ImagePreview().newInstance(resources.getString(R.string.waistImage)).show(fragmentManager, "alertDialog")
                })

                view.findViewById<View>(R.id.imagePreviewHip).setOnClickListener(View.OnClickListener {
                    ImagePreview().newInstance(resources.getString(R.string.hipImage)).show(fragmentManager, "alertDialog")
                })

                /*Upload client mesaurements info*/
                val continueBtn = view.findViewById<View>(R.id.fittingCustomSizesBtn)
                continueBtn.setOnClickListener(View.OnClickListener {

                    var fittingHeight = view!!.findViewById<TextInputLayout>(R.id.fittingCustomSizesHeight)
                    var fittingBust = view!!.findViewById<TextInputLayout>(R.id.fittingCustomSizesBust)
                    var fittingWaist = view!!.findViewById<TextInputLayout>(R.id.fittingCustomSizesWaist)
                    var fittingHip = view!!.findViewById<TextInputLayout>(R.id.fittingCustomSizesHip)

                    if(
                        fittingHeight.editText?.text.toString().isNotEmpty() &&
                        fittingBust.editText?.text.toString().isNotEmpty() &&
                        fittingWaist.editText?.text.toString().isNotEmpty() &&
                        fittingHip.editText?.text.toString().isNotEmpty()
                    ){
                        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity!!.findViewById(android.R.id.content), true)
                        val createMeasurementObj = UtilsModel.getGson().toJson(CreateFittingMeasurementsRequest(
                            alertObj.productId,
                            alertObj.userId,
                            alertObj.orderId,
                            fittingBust.editText?.text.toString(),
                            fittingWaist.editText?.text.toString(),
                            fittingHip.editText?.text.toString(),
                            fittingHeight.editText?.text.toString()
                        ))
                        dialog.hide()
                        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, resources.getString(R.string.orderFittingUpdate), createMeasurementObj)).enqueue(object: Callback {
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
                                        run {
                                            (activity as OrderDetailActivity).changeOrderStatus()
                                        }
                                    }
                                }else{
                                    UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                                }
                                onDismiss(dialog)
                            }
                        })
                    }else{
                        val text = resources.getText(R.string.fillRequiredFields)
                        val duration = Toast.LENGTH_SHORT
                        Toast.makeText(activity!!, text, duration).show()
                    }
                })
            }
            "newUpdateVersion" -> {
                view = activity!!.layoutInflater.inflate(R.layout.fragment_custom_alert_confirm, null)

                /*Set title*/
                (view.findViewById<TextView>(R.id.alertTitle)).text = resources.getString(R.string.availableUpdate)
                (view.findViewById<TextView>(R.id.alertDescription)).text = resources.getString(R.string.availableUpdateDescription)

                /*Update action*/
                val btn = view.findViewById<View>(R.id.btnSubmit)
                btn.setOnClickListener(View.OnClickListener {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity!!.packageName}")))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=${activity!!.packageName}")
                            )
                        )
                    }
                })
            }
        }

        alert.setContentView(view!!)
        return alert
    }
}