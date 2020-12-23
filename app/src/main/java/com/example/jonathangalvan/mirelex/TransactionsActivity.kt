package com.example.jonathangalvan.mirelex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jonathangalvan.mirelex.Adapters.TransactionsAdapter
import com.example.jonathangalvan.mirelex.Interfaces.ProductsInterface
import com.example.jonathangalvan.mirelex.Interfaces.TransactionsInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import kotlinx.android.synthetic.main.activity_transactions.*
import kotlinx.android.synthetic.main.view_progressbar.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class TransactionsActivity : AppCompatActivity() {

    var transactionsAdapter = TransactionsAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.transactions)

        transactionsGrid.layoutManager = LinearLayoutManager(this )
        transactionsGrid.adapter = transactionsAdapter

        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getTransactions))).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val response = response.body()!!.string()
                val responseObj = UtilsModel.getPostResponse(this@TransactionsActivity, response)
                runOnUiThread {
                    val responseTransactions = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), TransactionsInterface::class.java)
                    transactionsAdapter.loadNewData(responseTransactions.data)
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}