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
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomAlert
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.CatalogInterface
import com.example.jonathangalvan.mirelex.Interfaces.WomenCatalogInterface
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.RegisterExtraFieldsActivity
import com.example.jonathangalvan.mirelex.ViewModels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register_extra_fields_measure.*

class RegisterExtraFieldsMeasure : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

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
        return inflater.inflate(R.layout.fragment_register_extra_fields_measure, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(activity!!).get(RegisterViewModel::class.java)
        val user = viewModel.comleteUserCall.value
        val registeredUserInfo = viewModel.userCall.value

        /*Fill sizes*/
        val adapterSizes = ArrayAdapter<CatalogInterface>(activity, R.layout.view_spinner_item, R.id.spinnerItemWhiteSelect, viewModel.sizes.value)
        adapterSizes.setDropDownViewResource(R.layout.view_spinner_item_select)
        registerExtraSizesField.adapter = adapterSizes

        /*Fill bodyType*/
        val adapterBodyType = ArrayAdapter<WomenCatalogInterface>(activity, R.layout.view_spinner_item, R.id.spinnerItemWhiteSelect, viewModel.womenCatalogs.value?.bodyTypes)
        adapterBodyType.setDropDownViewResource(R.layout.view_spinner_item_select)
        registerExtraBodyTypesField.adapter = adapterBodyType

        /*Fill skinColorType*/
        val adapterSkinColorType = ArrayAdapter<WomenCatalogInterface>(activity, R.layout.view_spinner_item, R.id.spinnerItemWhiteSelect, viewModel.womenCatalogs.value?.skinColors)
        adapterSkinColorType.setDropDownViewResource(R.layout.view_spinner_item_select)
        registerExtraSkinColorField.adapter = adapterSkinColorType

        /*Fill hairColor*/
        val adapterHairColor = ArrayAdapter<WomenCatalogInterface>(activity, R.layout.view_spinner_item, R.id.spinnerItemWhiteSelect, viewModel.womenCatalogs.value?.hairColors)
        adapterHairColor.setDropDownViewResource(R.layout.view_spinner_item_select)
        registerExtraHairColorField.adapter = adapterHairColor

        /*Clicks measures "?"*/
        imagePreviewHeight.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.heightImage)).show(fragmentManager, "alertDialog")
        })

        imagePreviewBust.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.bustImage)).show(fragmentManager, "alertDialog")
        })

        imagePreviewWaist.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.waistImage)).show(fragmentManager, "alertDialog")
        })

        imagePreviewHip.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.hipImage)).show(fragmentManager, "alertDialog")
        })

        /*Hide fields*/
        when(user?.genderId){
            "1" -> {
                registerExtraBustLayout.visibility = View.GONE
                registerExtraWaistLayout.visibility = View.GONE
                registerExtraHipLayout.visibility = View.GONE
                registerExtraBodyTypesWrap.visibility = View.GONE
                registerExtraSkinColorWrap.visibility = View.GONE
                registerExtraHairColorWrap.visibility = View.GONE
            }
            "2" -> {
                registerExtraSizesWrap.visibility = View.GONE
            }
        }

        /*Submit form*/
        registerExtraFinalMeasureBtn.setOnClickListener(View.OnClickListener {
            if(inputValidations(user?.genderId)){
                user?.height = registerExtraHeightField.editText?.text.toString()
                when(user?.genderId){
                    "1" -> {
                        user?.sizeId = viewModel.sizes.value?.get(registerExtraSizesField.selectedItemPosition)?.productCatalogId?.toString()
                    }
                    "2" -> {
                        user?.bodyTypeId = viewModel.womenCatalogs.value!!.bodyTypes[registerExtraBodyTypesField.selectedItemPosition].catalogId.toString()
                        user?.skinColorId = viewModel.womenCatalogs.value!!.skinColors[registerExtraSkinColorField.selectedItemPosition].catalogId.toString()
                        user?.hairColorId = viewModel.womenCatalogs.value!!.hairColors[registerExtraHairColorField.selectedItemPosition].catalogId.toString()
                        user?.bust = registerExtraBustField.editText?.text.toString()
                        user?.waist = registerExtraWaistField.editText?.text.toString()
                        user?.hip = registerExtraHipField.editText?.text.toString()
                    }
                }
                viewModel.setCompleteUser(user!!)
                (activity as RegisterExtraFieldsActivity).doUserRegister()
            }else{
                val text = resources.getText(R.string.fillRequiredFields)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(activity, text, duration).show()
            }
        })
    }

    fun inputValidations(userGenderId: String?):  Boolean{
        var isCorrect = true
        when(userGenderId){
            "1" -> {
                if(registerExtraSizesField.selectedItem == null){
                    isCorrect = false
                }
            }
            "2" -> {
                if(registerExtraBodyTypesField.selectedItem == null){
                    isCorrect = false
                }
                if(registerExtraSkinColorField.selectedItem == null){
                    isCorrect = false
                }
                if(registerExtraHairColorField.selectedItem == null){
                    isCorrect = false
                }
                if(registerExtraBustField.editText?.text.toString().isEmpty()){
                    isCorrect = false
                }
                if(registerExtraWaistField.editText?.text.toString().isEmpty()){
                    isCorrect = false
                }
                if(registerExtraHipField.editText?.text.toString().isEmpty()){
                    isCorrect = false
                }
            }
        }
        if(registerExtraHeightField.editText?.text.toString().isEmpty()){
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
            RegisterExtraFieldsMeasure().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
