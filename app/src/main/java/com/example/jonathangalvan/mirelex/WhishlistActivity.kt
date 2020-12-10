package com.example.jonathangalvan.mirelex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.ProductsAdapter
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Interfaces.ProductsInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CustomerProductRequest
import kotlinx.android.synthetic.main.activity_whishlist.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class WhishlistActivity : AppCompatActivity() {

    var productAdapter: ProductsAdapter? = ProductsAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whishlist)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.wishlist)


        /*Set productGrid config*/
        favoritesProductGrid.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(this, 2)
        favoritesProductGrid.adapter = productAdapter

    }

    override fun onResume() {
        super.onResume()
        getProducts()
    }

    fun getProducts(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(applicationContext, resources.getString(R.string.getProductFavorites))).enqueue( object:
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@WhishlistActivity, responseStr)
                val responseProducts: ProductsInterface
                when(responseObj.status){
                    "success" -> {
                        responseProducts = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ProductsInterface::class.java)
                        runOnUiThread {
                            if(findViewById<ViewGroup>(R.id.viewCenteredMessage) != null) {
                                findViewById<ViewGroup>(R.id.contentTabsFrameLayout)?.removeView(findViewById(R.id.viewCenteredMessage))
                            }
                            productAdapter!!.loadNewData(responseProducts.data)
                        }
                    }
                    "noDataAvailable" -> {
                        runOnUiThread {
                            run {
                                productAdapter!!.loadNewData(ArrayList())
                                if((findViewById<ViewGroup>(R.id.viewCenteredMessage)) == null) {
                                    val ceneteredLayout = layoutInflater.inflate(
                                        R.layout.view_centered_message,
                                        findViewById(R.id.contentTabsFrameLayout),
                                        true
                                    )
                                    ceneteredLayout.centeredMessage.text = responseObj.desc
                                }
                            }
                        }
                    }
                    else -> {
                        UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                    }
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
