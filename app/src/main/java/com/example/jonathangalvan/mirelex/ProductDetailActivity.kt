package com.example.jonathangalvan.mirelex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.SliderPagerAdapter
import com.example.jonathangalvan.mirelex.Enums.OrderType
import com.example.jonathangalvan.mirelex.Interfaces.ProductInfoInterface
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomAlert
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.UserInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreateOrderRequest
import com.example.jonathangalvan.mirelex.Requests.GetProductInfoRequest
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.view_detail_info_row.view.*
import kotlinx.android.synthetic.main.view_detail_info_row_with_chevron_title.view.*
import kotlinx.android.synthetic.main.view_detail_info_row_with_title.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast



class ProductDetailActivity : AppCompatActivity() {

    private var productId: String? = ""
    private var userId: String? = ""
    private var productInfo: String? = ""
    var sessionUser: UserInterface? = null
    var productObj: ProductInfoInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val getProductIdFromBundle = intent.extras
        productId = getProductIdFromBundle.getString("productId")

        /*Session user*/
        sessionUser = SessionModel(this@ProductDetailActivity).getUser()

        /*Get product info*/
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        var getProductInfoObj = UtilsModel.getGson().toJson(GetProductInfoRequest(productId))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getProductInfo), getProductInfoObj)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@ProductDetailActivity, responseStr)
                if(responseObj.status == "success"){
                    productObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), ProductInfoInterface::class.java)
                    productInfo = UtilsModel.getGson().toJson(productObj)

                    runOnUiThread {
                        run{
                            supportActionBar?.title = "${productObj?.productInformation?.brand}"

                            /*Go to store info*/
                            detailProductGoToStoreProfile.setOnClickListener(View.OnClickListener {
                                val detailStoreBundle: Bundle = Bundle()
                                val goToStoreDetail = Intent(this@ProductDetailActivity, StoreDetailActivity::class.java)
                                detailStoreBundle.putString("productInfo", UtilsModel.getGson().toJson(productObj))
                                goToStoreDetail.putExtras(detailStoreBundle)
                                startActivity(goToStoreDetail)
                            })

                            /*Slider Gallery*/
                            var images: ArrayList<String> = ArrayList()
                            if(productObj?.productInformation?.productFeaturedImage != null){
                                images.add(productObj?.productInformation?.productFeaturedImage.toString())
                                for(productImage in productObj!!.productImages){
                                    images.add(productImage.imageUrl.toString())
                                }
                            }
                            val sliderAdapter = SliderPagerAdapter(images, supportFragmentManager)
                            detailProductImageSlider.adapter = sliderAdapter
                            indicator.setViewPager(detailProductImageSlider)

                            if(productObj?.productOwner?.person?.profilePictureUrl != null){
                                Picasso.with(this@ProductDetailActivity).load(productObj?.productOwner?.person?.profilePictureUrl).into(detailProductOwnerProfileImage)
                            }

                            when(productObj?.productOwner?.person?.userTypeId){
                                "4" -> {
                                    detailProductOwnerName.text = "${productObj?.productOwner?.person?.companyName}"
                                }
                                else -> {
                                    detailProductOwnerName.text = "${productObj?.productOwner?.person?.firstName} ${productObj?.productOwner?.person?.paternalLastName}"
                                }
                            }

                            val description = layoutInflater.inflate(R.layout.view_detail_info_row, detailProductInfo, false)
                            description.detailInfoJustValueView.text = productObj?.productInformation?.description
                            detailProductInfo.addView(description)

                            val productType = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailProductInfo, false)
                            productType.detailInfoNameView.text = resources.getString(R.string.category)
                            productType.detailInfoValueView.text = productObj?.productInformation?.productType
                            detailProductInfo.addView(productType)

                            val size = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailProductInfo, false)
                            size.detailInfoNameView.text = resources.getString(R.string.size)
                            size.detailInfoValueView.text = productObj?.productInformation?.size
                            detailProductInfo.addView(size)


                            when(productObj?.productInformation?.productTypeId){
                                ProductType.Dress.productTypeId -> {

                                    val productStyle = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailProductInfo, false)
                                    productStyle.detailInfoNameView.text = resources.getString(R.string.styleClient)
                                    productStyle.detailInfoValueView.text = productObj?.productInformation?.productStyle
                                    detailProductInfo.addView(productStyle)

                                    val productMaterial = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailProductInfo, false)
                                    productMaterial.detailInfoNameView.text = resources.getString(R.string.material)
                                    productMaterial.detailInfoValueView.text = productObj?.productInformation?.productMaterial
                                    detailProductInfo.addView(productMaterial)

                                    val productDecoration = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailProductInfo, false)
                                    productDecoration.detailInfoNameView.text = resources.getString(R.string.decoration)
                                    var decorations: String = ""
                                    productObj?.productDecorations?.forEach {
                                        decorations += " ${it.decoration},"
                                    }
                                    productDecoration.detailInfoValueView.text = decorations.dropLast(1)
                                    detailProductInfo.addView(productDecoration)

                                    val productLength = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailProductInfo, false)
                                    productLength.detailInfoNameView.text = resources.getString(R.string.length)
                                    productLength.detailInfoValueView.text = productObj?.productInformation?.productLength
                                    detailProductInfo.addView(productLength)

                                    val productSleeveStyle = layoutInflater.inflate(R.layout.view_detail_info_row_with_title, detailProductInfo, false)
                                    productSleeveStyle.detailInfoNameView.text = resources.getString(R.string.sleeveStyle)
                                    productSleeveStyle.detailInfoValueView.text = productObj?.productInformation?.productSleeveStyle
                                    detailProductInfo.addView(productSleeveStyle)

                                }
                            }

                            val productColor = layoutInflater.inflate(R.layout.view_detail_info_row, detailProductInfo, false)
                            var colors: String = ""
                            productObj?.productColors?.forEach {
                                colors += " ${it.color},"
                            }
                            productColor.detailInfoJustValueView.text = colors.dropLast(1)
                            detailProductInfo.addView(productColor)

//                            val modelAr = layoutInflater.inflate(R.layout.view_detail_info_row_with_chevron_title, detailProductInfo,false)
//                            modelAr.detailInfoChevronTitle.text = "Abrir modelo Ar"
//                            modelAr.setOnClickListener(View.OnClickListener {
//                                val launchIntent =
//                                    packageManager.getLaunchIntentForPackage("com.VirtualMindStudio.Vestidos_AR")
//                                if (launchIntent != null) {
//                                    startActivity(launchIntent)
//                                } else {
//                                    Toast.makeText(
//                                        this@ProductDetailActivity,
//                                        "There is no package available in android",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }
//                            })
//                            detailProductInfo.addView(modelAr)

                            if(productObj?.productInformation?.fittable == "1") {
                                val productFitting = layoutInflater.inflate(
                                    R.layout.view_detail_info_row_with_chevron_title,
                                    detailProductInfo,
                                    false
                                )
                                productFitting.detailInfoChevronTitle.text = resources.getString(R.string.doFittingOrder)
                                productFitting.setOnClickListener(View.OnClickListener {
                                    if(sessionUser?.address!!.size > 0){
                                        continueForFitting()
                                    }else{
                                        CustomAlert().newInstance("{'status': '${OrderType.Fitting.orderTypeId}','title':'${resources.getString(R.string.alert)}', 'desc': '${resources.getString(R.string.completeYourProfile)}'}", 3, 0)
                                            .show(supportFragmentManager, "alert")
                                    }
                                })
                                detailProductInfo.addView(productFitting)
                            }

                            if(productObj?.productInformation?.leaseable == "1"){
                                val productLeaseable = layoutInflater.inflate(R.layout.view_detail_info_row_with_chevron_title, detailProductInfo, false)
                                productLeaseable.detailInfoChevronTitle.text = resources.getString(R.string.leaseable)
                                productLeaseable.detailInfoChevronValue.setTextColor(ContextCompat.getColor(this@ProductDetailActivity, R.color.Tale))
                                productLeaseable.detailInfoChevronValue.text = productObj?.productInformation?.priceFormatted
                                productLeaseable.setOnClickListener(View.OnClickListener {
                                    if(sessionUser?.address!!.size > 0){
                                        confirmFittingAlert()
                                    }else{
                                        CustomAlert().newInstance("{'status': '${OrderType.Lease.orderTypeId}','title':'${resources.getString(R.string.alert)}', 'desc': '${resources.getString(R.string.completeYourProfile)}'}", 3, 0)
                                            .show(supportFragmentManager, "alert")
                                    }
                                })
                                detailProductInfo.addView(productLeaseable)
                            }

                            if(productObj?.productInformation?.sellable == "1"){
                                val productSellable = layoutInflater.inflate(R.layout.view_detail_info_row_with_chevron_title, detailProductInfo, false)
                                productSellable.detailInfoChevronTitle.text = resources.getString(R.string.toBuy)
                                productSellable.detailInfoChevronValue.setTextColor(ContextCompat.getColor(this@ProductDetailActivity, R.color.colorGreen))
                                productSellable.detailInfoChevronValue.text = productObj?.productInformation?.sellPriceFormatted
                                productSellable.setOnClickListener(View.OnClickListener {
                                    if(sessionUser?.address!!.size > 0){
                                        continueForPurchase()
                                    }else{
                                        CustomAlert().newInstance("{'status': '${OrderType.Purchase.orderTypeId}','title':'${resources.getString(R.string.alert)}', 'desc': '${resources.getString(R.string.completeYourProfile)}'}", 3, 0)
                                            .show(supportFragmentManager, "alert")
                                    }
                                })
                                detailProductInfo.addView(productSellable)
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        sessionUser = SessionModel(this@ProductDetailActivity).getUser()
    }

    fun confirmFittingAlert(){
        UtilsModel.getAlertView().newInstance("", 3, 0).show(supportFragmentManager,"alertDialog")
    }

    fun continueForLeaseble(){
        val calendarViewBundle = Bundle()
        val goToCalendarView = Intent(this@ProductDetailActivity, CalendarActivity::class.java)
        calendarViewBundle.putString("productInfo", productInfo)
        calendarViewBundle.putString("orderType", "2")
        goToCalendarView.putExtras(calendarViewBundle)
        startActivity(goToCalendarView)
    }

    fun continueForFitting(){
        val calendarViewBundle = Bundle()
        val goToCalendarView = Intent(this@ProductDetailActivity, CalendarActivity::class.java)
        calendarViewBundle.putString("productInfo", productInfo)
        calendarViewBundle.putString("orderType", "3")
        goToCalendarView.putExtras(calendarViewBundle)
        startActivity(goToCalendarView)
    }

    fun continueForPurchase(){
        val formattedDate = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())
        val createOrderObj = CreateOrderRequest(productId = productObj?.productInformation?.productId.toString(), orderType = "1", endDate = formattedDate, startDate = formattedDate)
        val orderCheckoutBundle = Bundle()
        val goToOrderCheckout = Intent(this@ProductDetailActivity, OrderCheckoutActivity::class.java)
        orderCheckoutBundle.putString("productInfo", productInfo)
        orderCheckoutBundle.putString("orderRequestObj", UtilsModel.getGson().toJson(createOrderObj))
        goToOrderCheckout.putExtras(orderCheckoutBundle)
        startActivity(goToOrderCheckout)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
