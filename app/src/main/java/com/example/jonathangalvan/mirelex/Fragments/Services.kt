package com.example.jonathangalvan.mirelex.Fragments

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.ServicesAdapter
import com.example.jonathangalvan.mirelex.Interfaces.ServicesInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.ServiceCreateActivity
import kotlinx.android.synthetic.main.fragment_services.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class Services : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private val serviceAdapter = ServicesAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        servicesOrderService.setOnClickListener(View.OnClickListener {
            val goToServiceCreate = Intent(activity!!, ServiceCreateActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(activity!!,  R.anim.abc_slide_in_bottom,  R.anim.abc_fade_out)
            startActivity(goToServiceCreate, options.toBundle())
        })

        servicesList.layoutManager = LinearLayoutManager(activity!!)
        servicesList.adapter = serviceAdapter
        servicesList.addOnItemTouchListener(RecyclerItemClickListener(context!!, servicesList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
//                val goToOrderDetail = Intent(activity!!, OrderDetailActivity::class.java)
//                val bundletToOrderDetail = Bundle()
//                bundletToOrderDetail.putString("orderId", ordersAdapter.getOrder(position).orderId)
//                goToOrderDetail.putExtras(bundletToOrderDetail)
//                startActivity(goToOrderDetail)
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))

        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!.applicationContext, activity!!.resources.getString(R.string.getOrderServices))).enqueue( object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                if(responseObj.status == "success"){
                    val servicesObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ServicesInterface::class.java)
                    activity?.runOnUiThread {
                        run {
                            serviceAdapter.loadNewData(servicesObj.data)
                        }
                    }
                }else{
                    UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                }
            }
        })
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            Services().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
