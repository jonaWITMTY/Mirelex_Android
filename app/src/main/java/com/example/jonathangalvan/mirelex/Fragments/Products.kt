package com.example.jonathangalvan.mirelex.Fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.ProductsAdapter
import com.example.jonathangalvan.mirelex.Interfaces.ProductInterface
import com.example.jonathangalvan.mirelex.Interfaces.ProductsInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ProductDetailActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.CustomerProductRequest
import kotlinx.android.synthetic.main.fragment_products.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class Products : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = SessionModel(activity!!).getUser()
        val loader = layoutInflater.inflate(R.layout.view_progressbar, activity?.findViewById(android.R.id.content), true)
        var getProductsObj = CustomerProductRequest(user.characteristics?.sizeId)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(activity!!.applicationContext, activity!!.resources.getString(R.string.getProducts), UtilsModel.getGson().toJson(getProductsObj))).enqueue( object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(activity?.supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {run{activity?.findViewById<ViewGroup>(android.R.id.content)?.removeView(activity?.findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(responseStr)
                val responseProducts: ProductsInterface
                if(responseObj.status == "success"){
                    responseProducts = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ProductsInterface::class.java)
                    activity?.runOnUiThread {
                        productsGrid.adapter = ProductsAdapter(activity!!.applicationContext, responseProducts.data)
                        productsGrid.setOnItemClickListener { parent, view, position, id ->
                            val goToProductDetail = Intent(activity!!, ProductDetailActivity::class.java)
                            val b = Bundle()
                            b.putString("productId", (productsGrid.adapter.getItem(position) as ProductInterface).productId.toString())
                            goToProductDetail.putExtras(b)
                            startActivity(goToProductDetail)
                        }
                    }
                }else{
                    UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(activity?.supportFragmentManager,"alertDialog")
                }
            }
        })
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
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
