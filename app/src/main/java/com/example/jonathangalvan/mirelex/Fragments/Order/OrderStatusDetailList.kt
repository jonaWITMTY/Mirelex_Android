package com.example.jonathangalvan.mirelex.Fragments.Order

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.OrderStatusAdapater
import com.example.jonathangalvan.mirelex.Interfaces.OrderInterface
import com.example.jonathangalvan.mirelex.Interfaces.OrderProductInfo
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.OrderDetailActivity

import com.example.jonathangalvan.mirelex.R
import kotlinx.android.synthetic.main.fragment_order_status_detail_list.*
import kotlinx.android.synthetic.main.fragment_register_extra_fields_address.view.*

class OrderStatusDetailList : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var orderObj: OrderProductInfo? = null
    private var orderStatusAdapter = OrderStatusAdapater( ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderObj = UtilsModel.getGson().fromJson(arguments?.getString("orderObj"), OrderProductInfo::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_status_detail_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        orderStatusDetailList.layoutManager = LinearLayoutManager(activity)
        orderStatusDetailList.adapter = orderStatusAdapter
        orderStatusAdapter.loadNewData(orderObj!!.orderUpdates)
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OrderStatusDetailList().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
