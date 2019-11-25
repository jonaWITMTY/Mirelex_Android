package com.example.jonathangalvan.mirelex.UI

import android.widget.ArrayAdapter
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.util.AttributeSet
import android.view.View
import android.widget.Spinner
import com.example.jonathangalvan.mirelex.Adapters.ColorSpinnerAdapter
import com.example.jonathangalvan.mirelex.Interfaces.ProductCatalogs
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import android.widget.AdapterView
import android.widget.AbsListView.CHOICE_MODE_MULTIPLE
import android.widget.ListView


class MultiSpinnerCustom : Spinner, OnMultiChoiceClickListener, DialogInterface.OnCancelListener {

    private var selected: BooleanArray? = null
    private var defaultText: String? = null
    private var multiSpinnerlistener: MultiSpinnerListener? = null
    private var multiSpinnerType: String? = null
    private var multiSpinnerItemsObjStr: String? = null

    constructor(context: Context) : super(context) {}

    constructor(arg0: Context, arg1: AttributeSet) : super(arg0, arg1) {}

    constructor(arg0: Context, arg1: AttributeSet, arg2: Int) : super(arg0, arg1, arg2) {}

    override fun onClick(dialog: DialogInterface, which: Int, isChecked: Boolean) {
        selected!![which] = isChecked
    }

    override fun onCancel(dialog: DialogInterface) {
        refreshText()
    }

    fun selectItem(p0: Int, isChecked: Boolean){
        selected!![p0] = isChecked
    }

    fun refreshText(){
        if(selected != null){
            val spinnerBuffer = StringBuffer()
            var someUnselected = false

            /*Set text to display in spinner depending on type*/
            when(multiSpinnerType){
                "colors" -> {
                    val catalogs = UtilsModel.getGson().fromJson(multiSpinnerItemsObjStr, ProductCatalogs::class.java)
                    for (i in catalogs.colors.indices) {
                        if (selected!![i]) {
                            spinnerBuffer.append(catalogs.colors!![i].name)
                            spinnerBuffer.append(", ")
                        } else {
                            someUnselected = true
                        }
                    }
                }
            }

            var spinnerText: String?
            if (someUnselected) {
                spinnerText = spinnerBuffer.toString()
                if (spinnerText.length > 2)
                    spinnerText = spinnerText.substring(0, spinnerText.length - 2)
            } else {
                spinnerText = defaultText
            }

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, arrayOf<String>(spinnerText!!))
            setAdapter(adapter)
            multiSpinnerlistener!!.onItemsSelected(selected)
        }
    }

    override fun performClick(): Boolean {
        val builder = AlertDialog.Builder(context)

        /*Add custom item adapter to dialog*/
        when(multiSpinnerType){
            "colors" -> {
                val catalogs = UtilsModel.getGson().fromJson(multiSpinnerItemsObjStr, ProductCatalogs::class.java)
                val colorSpinnerAdapter = ColorSpinnerAdapter(context, catalogs.colors, this, selected)
                builder.setAdapter(colorSpinnerAdapter, null)
            }
        }

        builder.setPositiveButton(android.R.string.ok) { dialog, which -> dialog.cancel() }
        builder.setOnCancelListener(this)
        builder.show()
        return true
    }

    fun setItems(allText: String, type: String?, itemsObjArrStr: String?, listener: MultiSpinnerListener, selectedItems: BooleanArray? = null) {
        defaultText = allText
        multiSpinnerlistener = listener
        multiSpinnerType = type
        multiSpinnerItemsObjStr = itemsObjArrStr

        /*Set values depending on type*/
        when(multiSpinnerType){
            "colors" -> {
                val catalogs = UtilsModel.getGson().fromJson(multiSpinnerItemsObjStr, ProductCatalogs::class.java)
                if(selectedItems != null){
                    selected = BooleanArray(catalogs.colors.size)
                    for (i in selected!!.indices){
                        selected!![i] = selectedItems!![i]
                    }

                    /*Set text to spinner*/
                    refreshText()
                }else{
                    selected = BooleanArray(catalogs.colors.size)
                    for (i in selected!!.indices){
                        selected!![i] = false
                    }

                    /*Set text to spinner*/
                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, arrayOf(allText))
                    setAdapter(adapter)
                }
            }
        }
    }

    interface MultiSpinnerListener {
        fun onItemsSelected(selected: BooleanArray?)
    }
}