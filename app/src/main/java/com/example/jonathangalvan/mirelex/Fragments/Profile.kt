package com.example.jonathangalvan.mirelex.Fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.example.jonathangalvan.mirelex.*
import com.example.jonathangalvan.mirelex.ConversationsActivity
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Interfaces.UserInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel

import kotlinx.android.synthetic.main.fragment_profile.*

class Profile : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var currentUser: UserInterface? = null

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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Hide options for store*/
        if(SessionModel(activity!!).getUser().person?.userTypeId == UserType.Store.userTypeId){
            sessionUserPayments.visibility = View.GONE
        }

        /*Fill user cell*/
        updateUser()

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
            SessionModel(activity!!).signOutSession()
            startActivity(Intent(activity!!, MainActivity::class.java))
        })

        /*Click to profile information*/
        sessionUserProfile.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity!!, ProfileActivity::class.java))
        })

        /*Click to messages*/
        sessionUserMessages.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity!!, ConversationsActivity::class.java))
        })

        /*Click to payment methods*/
        sessionUserPayments.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity!!, PaymentCardsActivity::class.java))
        })
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    fun updateUser(){
        val sessionModel = SessionModel(activity!!)
        currentUser = sessionModel.getUser()
        var name = ""
        when(currentUser?.person?.userTypeId){
            "4" -> {
                name = "${currentUser?.person?.companyName}"
            }
            else -> {
                name = "${currentUser?.person?.firstName} ${currentUser?.person?.paternalLastName}"
            }
        }
        sessionUserName.text = name
        sessionUserEmail.text = currentUser?.person?.email
    }

    override fun onResume() {
        super.onResume()
        updateUser()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        /*Store tabs icons*/
        menu?.findItem(R.id.storeTabsAddIcon)?.isVisible = false
        menu?.findItem(R.id.storeTabsFilterIcon)?.isVisible = false
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
