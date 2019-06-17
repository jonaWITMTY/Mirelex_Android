
package com.example.jonathangalvan.mirelex.Fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.jonathangalvan.mirelex.Adapters.OrdersAdapter
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Interfaces.FilterInterface
import com.example.jonathangalvan.mirelex.Interfaces.OrderInterface
import com.example.jonathangalvan.mirelex.Interfaces.OrdersInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.R
import kotlinx.android.synthetic.main.fragment_orders.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.ArrayList
import com.example.jonathangalvan.mirelex.OrderDetailActivity
import kotlinx.android.synthetic.main.view_centered_message.view.*


class Orders : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private val ordersAdapter = OrdersAdapter(ArrayList())
    private var ordersObj: OrdersInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ordersList.layoutManager = LinearLayoutManager(activity)
        ordersList.adapter = ordersAdapter
        ordersList.addOnItemTouchListener(RecyclerItemClickListener(context!!, ordersList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val goToOrderDetail = Intent(activity!!, OrderDetailActivity::class.java)
                val bundletToOrderDetail = Bundle()
                bundletToOrderDetail.putString("orderId", ordersAdapter.getOrder(position).orderId)
                goToOrderDetail.putExtras(bundletToOrderDetail)
                startActivity(goToOrderDetail)
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))

        /*Fill filter options*/
        val filterArr = ArrayList<FilterInterface>()
        filterArr.add(FilterInterface("0", activity!!.resources.getString(R.string.all)))
        filterArr.add(FilterInterface(OrderType.Fitting.orderTypeId, activity!!.resources.getString(R.string.fitting)))
        filterArr.add(FilterInterface(OrderType.Lease.orderTypeId, activity!!.resources.getString(R.string.lease)))
        filterArr.add(FilterInterface(OrderType.Purchase.orderTypeId, activity!!.resources.getString(R.string.purchase)))
        val filterAdapter = ArrayAdapter<FilterInterface>(activity!!, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, filterArr)
        filterAdapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
        filterOrdersSpinner.adapter = filterAdapter

        /*Do filter action*/
        filterOrdersSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(ordersObj != null){
                    activity?.runOnUiThread {
                        run {
                            var newObj = ArrayList<OrderInterface>()
                            if(filterArr[position].id != "0"){
                                for(order in ordersObj!!.data){
                                    if(filterArr[position].id == order.orderTypeId){
                                        newObj.add(order)
                                    }
                                }
                            }else{
                                newObj = ordersObj!!.data
                            }
                            ordersAdapter.loadNewData(newObj)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getOrders()
    }

    fun getOrders(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!.applicationContext, activity!!.resources.getString(R.string.getOrders))).enqueue( object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                when(responseObj.status){
                    "success" -> {
                        ordersObj = UtilsModel.getGson()
                            .fromJson(UtilsModel.getGson().toJson(responseObj), OrdersInterface::class.java)
                        activity?.runOnUiThread {
                            run {
                                ordersAdapter.loadNewData(ordersObj!!.data)
                            }
                        }
                    }
                    "noDataAvailable" -> {
                        activity?.runOnUiThread {
                            run {
                                val ceneteredLayout = layoutInflater.inflate(
                                    R.layout.view_centered_message,
                                    activity!!.findViewById(R.id.customerTabsFrameLayout),
                                    true
                                )
                                ceneteredLayout.centeredMessage.text = responseObj.desc
                            }
                        }
                    }
                    else -> {
                        UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                    }
                }
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        /*Store tabs icons*/
        menu?.findItem(R.id.storeTabsAddIcon)?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.storeTabsFilterIcon -> {
                filterOrdersSpinner.performClick()
            }
        }
        return super.onOptionsItemSelected(item)
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
            Orders().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
