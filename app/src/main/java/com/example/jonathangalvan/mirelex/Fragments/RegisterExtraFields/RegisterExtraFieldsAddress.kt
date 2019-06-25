package com.example.jonathangalvan.mirelex.Fragments.RegisterExtraFields

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Interfaces.NeighborhoodArrayInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.RegisterExtraFieldsActivity
import com.example.jonathangalvan.mirelex.Requests.NeighborhoodRequest
import com.example.jonathangalvan.mirelex.ViewModels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register_extra_fields_address.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Interfaces.NeighborhoodInterface

class RegisterExtraFieldsAddress : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var neighborhoodsArr: NeighborhoodArrayInterface? = null

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
//        fragmentManager?.popBackStack()
        return inflater.inflate(R.layout.fragment_register_extra_fields_address, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(activity!!).get(RegisterViewModel::class.java)
        val user = viewModel.comleteUserCall.value
        val registeredUserInfo = viewModel.userCall.value

        /*Change continue btn deppendinf on userType*/
        when(registeredUserInfo?.person?.userTypeId){
            "4" ->{
                finalAddressButton.text = resources.getString(R.string.end)
            }
            else ->{
                finalAddressButton.text = resources.getString(R.string.continueProcess)
            }
        }

        /*Fill postal code spinner depending on zip field*/
        registerExtraPostalCodeField.editText?.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(registerExtraPostalCodeField.editText?.text.toString().length > 4){
                    val loader = layoutInflater.inflate(R.layout.view_progressbar, activity!!.findViewById(android.R.id.content), true)
                    UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!.applicationContext, activity!!.resources.getString(R.string.getNeighborhood), UtilsModel.getGson().toJson(NeighborhoodRequest(registerExtraPostalCodeField.editText?.text.toString())))).enqueue(object : Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            activity!!.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                        }

                        override fun onResponse(call: Call, response: Response) {
                            activity!!.runOnUiThread {run{activity!!.findViewById<ViewGroup>(android.R.id.content).removeView(activity!!.findViewById(R.id.view_progressbar))}}
                            val responseStr = response.body()?.string()
                            val responseObj = UtilsModel.getPostResponse(activity!!, responseStr)
                            if(responseObj.status == "success") {
                                val neighborhoods= UtilsModel.getGson().fromJson(responseStr, NeighborhoodArrayInterface::class.java)
                                val adapter = ArrayAdapter<NeighborhoodInterface>(activity, R.layout.view_spinner_item, R.id.spinnerItemWhiteSelect, neighborhoods.data)
                                neighborhoodsArr = neighborhoods
                                adapter.setDropDownViewResource( R.layout.view_spinner_item_select)
                                activity!!.runOnUiThread {
                                    run{
                                        zipSpinner.adapter = adapter
                                    }
                                }
                            }
                        }
                    })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        /*Continue btn event*/
        finalAddressButton.setOnClickListener(View.OnClickListener {
            if(inputValidations()){
                user?.street = registerExtraStreetField.editText?.text.toString()
                user?.numExt = registerExtraExtNumField.editText?.text.toString()
                user?.numInt = registerExtraInternalNumberField.editText?.text.toString()
                user?.neighborhoodId = neighborhoodsArr?.data!![zipSpinner.selectedItemPosition].neighborhoodId
                user?.zipCode = registerExtraPostalCodeField.editText?.text.toString()
                viewModel.setCompleteUser(user!!)
                when(registeredUserInfo?.person?.userTypeId){
                    "4" ->{
                        (activity as RegisterExtraFieldsActivity).doUserRegister()
                    }
                    else ->{
                        (activity as RegisterExtraFieldsActivity).openTab(RegisterExtraFieldsMeasure(), "measure")
                    }
                }
            }else{
                val text = resources.getText(R.string.fillRequiredFields)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(activity, text, duration).show()
            }
        })

    }

    fun inputValidations():  Boolean{
        var isCorrect = true
        if(
            zipSpinner.selectedItem == null ||
            registerExtraStreetField.editText?.text.toString().isEmpty() ||
            registerExtraExtNumField.editText?.text.toString().isEmpty() ||
            registerExtraPostalCodeField.editText?.text.toString().isEmpty()
        ){
            isCorrect = false
        }
        return isCorrect
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
            RegisterExtraFieldsAddress().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
