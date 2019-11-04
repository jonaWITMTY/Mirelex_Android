package com.example.jonathangalvan.mirelex.Fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.NotificationsAdapter
import com.example.jonathangalvan.mirelex.ChatActivity
import com.example.jonathangalvan.mirelex.Interfaces.ConversationInterface
import com.example.jonathangalvan.mirelex.Interfaces.NotificationDataInterface
import com.example.jonathangalvan.mirelex.Interfaces.NotificationsInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.OrderDetailActivity

import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.SetNotificationRead
import com.example.jonathangalvan.mirelex.ServiceOrderDetailActivity
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class Notifications : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    var notificationAdapater = NotificationsAdapter(ArrayList())

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
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Onclick notification item*/
        notificationsList.addOnItemTouchListener(RecyclerItemClickListener(context!!, notificationsList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val notificationReadObj = UtilsModel.getGson().toJson(SetNotificationRead(notificationAdapater.getItem(position).notificationId))
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, resources.getString(R.string.setNotificationRead), notificationReadObj)).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    override fun onResponse(call: Call, response: Response) {}
                })

                val notificationDataObj = UtilsModel.getGson().fromJson(notificationAdapater.getItem(position).data, NotificationDataInterface::class.java)

                if(notificationDataObj.orderId != null){
                    val goToOrderDetail = Intent(activity!!, OrderDetailActivity::class.java)
                    val bundletToOrderDetail = Bundle()
                    bundletToOrderDetail.putString("orderId", notificationDataObj.orderId)
                    goToOrderDetail.putExtras(bundletToOrderDetail)
                    startActivity(goToOrderDetail)
                }

                if(notificationDataObj.conversationId != null){
                    val goToChat = Intent(activity!!, ChatActivity::class.java)
                    val bundleToChat = Bundle()
                    val conversationObj = UtilsModel.getGson().toJson(ConversationInterface(
                        conversationId = notificationDataObj.conversationId
                    ))
                    bundleToChat.putString("conversationObj", conversationObj)
                    goToChat.putExtras(bundleToChat)
                    startActivity(goToChat)
                }

                if(notificationDataObj.serviceOrderId != null){
                }
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))
    }

    override fun onResume() {
        super.onResume()
        getNotifications()
    }

    fun getNotifications(){
        notificationsList.layoutManager = LinearLayoutManager(activity)
        notificationsList.adapter = notificationAdapater
        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!, resources.getString(R.string.getUserNotifications))).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(activity, responseStr)
                when(responseObj.status){
                    "success" -> {
                        val nontificationObj = UtilsModel.getGson()
                            .fromJson(UtilsModel.getGson().toJson(responseObj), NotificationsInterface::class.java)
                        activity?.runOnUiThread {
                            run {
                                notificationAdapater.loadNewData(nontificationObj.data)
                            }
                        }
                    }
                    "noDataAvailable" -> {
                        activity?.runOnUiThread {
                            run {
                                if((activity!!.findViewById<ViewGroup>(R.id.viewCenteredMessage)) == null) {
                                    val ceneteredLayout = layoutInflater.inflate(
                                        R.layout.view_centered_message,
                                        activity!!.findViewById(R.id.contentTabsFrameLayout),
                                        true
                                    )
                                    ceneteredLayout.centeredMessage.text = responseObj.desc
                                }
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
        menu?.findItem(R.id.storeTabsFilterIcon)?.isVisible = false

        /*Customer tabs icons*/
        menu?.findItem(R.id.customerTabsFilterIcon)?.isVisible = false
        menu?.findItem(R.id.customerTabsAddProductIcon)?.isVisible = false
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
            Notifications().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
