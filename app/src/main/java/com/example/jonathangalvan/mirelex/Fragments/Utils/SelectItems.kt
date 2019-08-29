package com.example.jonathangalvan.mirelex.Fragments.Utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.jonathangalvan.mirelex.Adapters.SelecListAddressAdapter
import com.example.jonathangalvan.mirelex.Interfaces.AddressListArrayInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Listeners.SelectedItemsListener
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.R
import kotlinx.android.synthetic.main.fragment_register_extra_fields_address.view.*
import kotlinx.android.synthetic.main.fragment_select_items.*


class SelectItems() : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var ml: SelectedItemsListener.SelectedItemsListenerInterface? = null
    private var addressAdapter = SelecListAddressAdapter(ArrayList())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_items, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is SelectedItemsListener.SelectedItemsListenerInterface){
            ml = context
        }else {
            throw RuntimeException(context!!.toString() + " must implement FragmentEvent")
        }

        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Get arguments*/
        val inputName = arguments?.getString("inputName")
        val listType = arguments?.getString("listType")
        val list = arguments?.getString("list")
        val tag = arguments?.getString("tag")

        /*Close fragment*/
        selectItemsClose.setOnClickListener(View.OnClickListener {
            ml?.callbackClose(tag!!)
        })

        /*Fill list*/
        selectListItems.layoutManager = LinearLayoutManager(activity)
        when(listType){
            "storesList" -> {
                selectListItems.adapter = addressAdapter
                val obj = UtilsModel.getGson().fromJson(list, AddressListArrayInterface::class.java)
                addressAdapter.loadNewData(obj.data!!)

                selectListItems.addOnItemTouchListener(RecyclerItemClickListener(context!!, selectListItems, object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        ml?.callback(tag!!, inputName!!, obj.data!![position].addressId.toString(), obj.data!![position].businessName.toString())
                    }

                    override fun onItemLongClick(view: View?, position: Int) {}
                }))
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        ml?.callbackClose(tag!!)
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SelectItems().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
