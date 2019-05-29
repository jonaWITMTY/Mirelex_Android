package com.example.jonathangalvan.mirelex.Fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.*
import com.example.jonathangalvan.mirelex.Models.SessionModel

import kotlinx.android.synthetic.main.fragment_profile.*

class Profile : Fragment() {
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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Fill user cell*/
        val sessionModel = SessionModel(activity!!)
        val currentUser = sessionModel.getUser()
        var name = ""
        when(currentUser.person?.userTypeId){
            "4" -> {
                name = "${currentUser.person?.companyName}"
            }
            else -> {
                name = "${currentUser.person?.firstName} ${currentUser.person?.paternalLastName}"
            }
        }
        sessionUserName.text = name
        sessionUserEmail.text = currentUser.person?.email

        /*Click event to go to terms and conditions*/
        sessionUserTerms.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity!!, WebviewActivity::class.java))
        })

        /*Click event to fo to Contact*/
        sessionUserContact.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity!!, ContactActivity::class.java))
        })

        /*Click to logut*/
        sessionUserLogout.setOnClickListener(View.OnClickListener {
            SessionModel.saveSessionValue(activity!!, "token", "")
            SessionModel.saveSessionValue(activity!!, "user", "")
            startActivity(Intent(activity!!, MainActivity::class.java))
        })

        /*Click to profile information*/
        sessionUserProfile.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity!!, ProfileActivity::class.java))
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
            Profile().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
