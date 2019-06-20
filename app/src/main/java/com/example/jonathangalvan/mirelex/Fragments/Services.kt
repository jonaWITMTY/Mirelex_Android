package com.example.jonathangalvan.mirelex.Fragments

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.jonathangalvan.mirelex.Adapters.ServicesAdapter
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Interfaces.FilterInterface
import com.example.jonathangalvan.mirelex.Interfaces.ServiceInterface
import com.example.jonathangalvan.mirelex.Interfaces.ServicesInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.GetServicesRequest
import com.example.jonathangalvan.mirelex.ServiceCreateActivity
import com.example.jonathangalvan.mirelex.ServiceOrderDetailActivity
import kotlinx.android.synthetic.main.fragment_services.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class Services : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private val serviceAdapter = ServicesAdapter(ArrayList())
    private var servicesObj: ServicesInterface? = null

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Fill filter options*/
        val filterArr = ArrayList<FilterInterface>()
        filterArr.add(FilterInterface("0", activity!!.resources.getString(R.string.all)))
        filterArr.add(FilterInterface(OrderType.Cleaning.orderTypeId, activity!!.resources.getString(R.string.cleanning)))
        filterArr.add(FilterInterface(OrderType.Sewing.orderTypeId, activity!!.resources.getString(R.string.sewing)))
        val filterAdapter = ArrayAdapter<FilterInterface>(activity!!, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, filterArr)
        filterAdapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
        filterServicesSpinner.adapter = filterAdapter

        /*Do filter action*/
        filterServicesSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(servicesObj != null){
                    activity?.runOnUiThread {
                        run {
                            var newObj = ArrayList<ServiceInterface>()
                            if(filterArr[position].id != "0"){
                                for(service in servicesObj!!.data){
                                    if(filterArr[position].id == service.orderTypeId){
                                        newObj.add(service)
                                    }
                                }
                            }else{
                                newObj = servicesObj!!.data
                            }
                            serviceAdapter.loadNewData(newObj)
                        }
                    }
                }
            }
        }

        /*Hide order service depending on sessionusertype*/
        when(SessionModel(activity!!).getSessionUserType()){
            UserType.Store.userTypeId -> {
                servicesOrderService.visibility = View.GONE
            }
        }

        servicesOrderService.setOnClickListener(View.OnClickListener {
            val goToServiceCreate = Intent(activity!!, ServiceCreateActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(activity!!,  R.anim.abc_slide_in_bottom,  R.anim.abc_fade_out)
            startActivity(goToServiceCreate, options.toBundle())
        })

        servicesList.layoutManager = LinearLayoutManager(activity!!)
        servicesList.adapter = serviceAdapter
        servicesList.addOnItemTouchListener(RecyclerItemClickListener(context!!, servicesList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val goToServiceDetail = Intent(activity!!, ServiceOrderDetailActivity::class.java)
                val bundletToServiceDetail = Bundle()
                bundletToServiceDetail.putString("serviceObj", UtilsModel.getGson().toJson(serviceAdapter.getService(position)))
                goToServiceDetail.putExtras(bundletToServiceDetail)
                startActivity(goToServiceDetail)
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))
    }

    override fun onResume() {
        super.onResume()
        loadServiceOrders()
    }

    fun loadServiceOrders(){
        val user = SessionModel(activity!!).getUser()
        var isClient = "0"
        if(user.person?.userTypeId == UserType.Customer.userTypeId){
            isClient = "1"
        }
        val servicesObjRequest = UtilsModel.getGson().toJson(GetServicesRequest(
                isClient
        ))

        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!.applicationContext, activity!!.resources.getString(R.string.getOrderServices), servicesObjRequest)).enqueue( object:
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
                        servicesObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ServicesInterface::class.java)
                        activity?.runOnUiThread {
                            run {
                                serviceAdapter.loadNewData(servicesObj!!.data)
                            }
                        }
                    }
                    "noDataAvailable" -> {
                        activity?.runOnUiThread {
                            run {
                                if((activity!!.findViewById<ViewGroup>(R.id.viewCenteredMessage)) == null) {
                                    val ceneteredLayout = layoutInflater.inflate(
                                        R.layout.view_centered_message,
                                        activity!!.findViewById(R.id.customerTabsFrameLayout),
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.storeTabsFilterIcon, R.id.customerTabsFilterIcon  -> {
                filterServicesSpinner.performClick()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        /*Store tabs icons*/
        menu?.findItem(R.id.storeTabsAddIcon)?.isVisible = false
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
