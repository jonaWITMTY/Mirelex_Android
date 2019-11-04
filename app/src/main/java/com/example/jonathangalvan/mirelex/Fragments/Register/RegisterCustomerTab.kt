package com.example.jonathangalvan.mirelex.Fragments.Register

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.R
import android.widget.ArrayAdapter
import com.example.jonathangalvan.mirelex.Interfaces.GenderInterface
import com.example.jonathangalvan.mirelex.ViewModels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register_customer_tab.*


class RegisterCustomerTab : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_customer_tab, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Update fields on Viewmodel*/
        val viewModel = ViewModelProviders.of(activity!!).get(RegisterViewModel::class.java)

        /*Gender List*/
//        val adapter = ArrayAdapter<GenderInterface>(activity, R.layout.view_spinner_item, R.id.spinnerItemWhiteSelect, viewModel.genderCall.value!!)
//        adapter.setDropDownViewResource(R.layout.view_spinner_item_select)
//        genderField.adapter = adapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener") as Throwable
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
            RegisterCustomerTab().apply {
                arguments = Bundle().apply {
                }
            }
    }
}



