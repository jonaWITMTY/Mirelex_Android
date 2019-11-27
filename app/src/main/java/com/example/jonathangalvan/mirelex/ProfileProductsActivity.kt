package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.GridLayoutManager
import android.view.*
import com.example.jonathangalvan.mirelex.Adapters.ProductsAdapter
import com.example.jonathangalvan.mirelex.Interfaces.ProductsInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import kotlinx.android.synthetic.main.activity_profile_products.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ProfileProductsActivity : AppCompatActivity() {

    var productAdapter: ProductsAdapter? = ProductsAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_products)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Set title to activity*/
        supportActionBar?.title = resources.getString(R.string.myProducts)

        /*Set productGrid config*/
        profileProductsGrid.layoutManager = GridLayoutManager(this, 2)
        profileProductsGrid.adapter = productAdapter

        profileProductsGrid?.addOnItemTouchListener(RecyclerItemClickListener(this, profileProductsGrid, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val goToProductDetail: Intent = Intent(this@ProfileProductsActivity, ProductActivity::class.java)
                val b = Bundle()
                b.putString("productId", productAdapter!!.getProduct(position).productId.toString())
                goToProductDetail.putExtras(b)
                startActivity(goToProductDetail)
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))
    }

    override fun onResume() {
        super.onResume()

        /*Load products*/
        loadProducts()
    }

    fun loadProducts(){
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getProducts))).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@ProfileProductsActivity, responseStr)
                val responseProducts: ProductsInterface
                when(responseObj.status){
                    "success" -> {
                        responseProducts = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj), ProductsInterface::class.java)
                        runOnUiThread {
                            if(findViewById<ViewGroup>(R.id.viewCenteredMessage) != null) {
                                findViewById<ViewGroup>(android.R.id.content)?.removeView(findViewById(R.id.viewCenteredMessage))
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
                                        findViewById(android.R.id.content),
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.customerTabsFilterIcon)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.customer_tabs_icons, menu)
        menu?.getItem(0)?.icon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, android.R.color.white))
        }
        menu?.getItem(1)?.icon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, android.R.color.white))
        }
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        return super.onOptionsItemSelected(item)
        when(item?.itemId){
            R.id.customerTabsAddProductIcon ->{
                startActivity(Intent(this, ProductActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
