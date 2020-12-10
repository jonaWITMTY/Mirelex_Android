package com.example.jonathangalvan.mirelex.Fragments.Utils

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView

import com.example.jonathangalvan.mirelex.R
import kotlinx.android.synthetic.main.fragment_date_picker_available.*

class DatePickerAvailable : androidx.fragment.app.Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    private var selectedDate: String? = null

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
        return inflater.inflate(R.layout.fragment_date_picker_available, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        datePickerSelection.setOnDateChangeListener(CalendarView.OnDateChangeListener { view, year, month, dayOfMonth ->
            var newMonth: String = ""
            var realMonth = month +1
            if(realMonth < 10){
                newMonth = "0$realMonth"
            }else{
                newMonth = realMonth.toString()
            }
            selectedDate = "$year-$newMonth-$dayOfMonth"
            (activity?.findViewById<TextView>(R.id.serviceCreateDate))?.text = selectedDate
            activity!!.supportFragmentManager.popBackStack()
        })
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
            DatePickerAvailable().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
