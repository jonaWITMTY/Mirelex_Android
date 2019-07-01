package com.example.jonathangalvan.mirelex.Fragments.RegisterExtraFields

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Interfaces.GenderInterface
import com.example.jonathangalvan.mirelex.Interfaces.ProductTypeInterface
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.RegisterExtraFieldsActivity
import com.example.jonathangalvan.mirelex.ViewModels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register_extra_fields_info.*


class RegisterExtraFieldsInfo : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_extra_fields_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(activity!!).get(RegisterViewModel::class.java)

        /*Hide and fill fields depending on usertype*/
        if(viewModel.userCall.value?.person?.userTypeId == "4"){
            registerExtraNameField.visibility = View.GONE
            registerExtraPaternalLastNameField.visibility = View.GONE
            registerExtraMaternalLastNameField.visibility = View.GONE
            (registerExtraGenderSpinner.parent as ViewGroup).visibility = View.GONE
            registerExtraCompanyNameField.editText?.setText(viewModel.userCall.value?.person?.companyName)
        }else{
            registerExtraCompanyNameField.visibility = View.GONE
            registerExtraNameField.editText?.setText(viewModel.userCall.value?.person?.firstName)
            registerExtraPaternalLastNameField.editText?.setText(viewModel.userCall.value?.person?.paternalLastName)
            registerExtraMaternalLastNameField.editText?.setText(viewModel.userCall.value?.person?.maternalLastName)
        }

        registerExtraEmailField.editText?.setText(viewModel.userCall.value?.person?.email)
        registerExtraPersonalPhoneField.editText?.setText(viewModel.userCall.value?.person?.personalPhone)

        /*If producttypes exists in viewmodel, set gender*/
        if(viewModel.productTypes.isNotEmpty()){
            val adapter = ArrayAdapter<ProductTypeInterface>(activity, R.layout.view_spinner_item, R.id.spinnerItemWhiteSelect, viewModel.productTypes)
            adapter.setDropDownViewResource(R.layout.view_spinner_item_select)
            activity?.findViewById<Spinner>(R.id.registerExtraGenderSpinner)?.adapter = adapter
        }

        /*Continue btn event*/
        continueBtn.setOnClickListener(View.OnClickListener {
            if(inputValidations(viewModel.userCall.value?.person?.userTypeId)) {
                var completeUser = viewModel.comleteUserCall.value
                completeUser?.userId = viewModel.userCall.value?.person?.userId
                if (viewModel.userCall.value?.person?.userTypeId == "4") {
                    completeUser?.companyName = registerExtraCompanyNameField.editText?.text.toString()
                } else {
                    completeUser?.firstName = registerExtraNameField.editText?.text.toString()
                    completeUser?.paternalLastName = registerExtraPaternalLastNameField.editText?.text.toString()
                    completeUser?.maternalLastName = registerExtraMaternalLastNameField.editText?.text.toString()

                    /*Gender*/
                    when(viewModel.productTypes[registerExtraGenderSpinner.selectedItemPosition].productTypeId){
                        ProductType.Suit.productTypeId -> {
                            completeUser?.genderId = "1"
                        }
                        ProductType.Dress.productTypeId -> {
                            completeUser?.genderId = "2"
                        }
                    }
                }
                completeUser?.userTypeId = viewModel.userCall.value?.person?.userTypeId
                completeUser?.email = viewModel.userCall.value?.person?.email!!
                completeUser?.personalPhone = registerExtraPersonalPhoneField.editText?.text.toString()
                viewModel.setCompleteUser(completeUser!!)
                (activity as RegisterExtraFieldsActivity).openTab(RegisterExtraFieldsAddress(), "address")
            }else{
                val text = resources.getText(R.string.fillRequiredFields)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(activity, text, duration).show()
            }
        })
    }

    fun inputValidations(mirelexStore: String?):  Boolean{
        var isCorrect = true
        when(mirelexStore){
            "3" -> {
                if(
                    registerExtraNameField.editText?.text.toString().isEmpty() ||
                    registerExtraPaternalLastNameField.editText?.text.toString().isEmpty()
                ){
                    isCorrect = false
                }
            }
            "4" -> {
                if(registerExtraCompanyNameField.editText?.text.toString().isEmpty()){
                    isCorrect = false
                }
            }
        }
        if(registerExtraPersonalPhoneField.editText?.text.toString().isEmpty()){
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
            RegisterExtraFieldsInfo().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
