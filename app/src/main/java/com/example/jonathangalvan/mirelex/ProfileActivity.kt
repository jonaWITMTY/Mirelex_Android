package com.example.jonathangalvan.mirelex

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.example.jonathangalvan.mirelex.Enums.Gender
import com.example.jonathangalvan.mirelex.Enums.ProductType
import com.example.jonathangalvan.mirelex.Enums.UserType
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomBottomAlert
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.Interfaces.*
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.NeighborhoodRequest
import com.example.jonathangalvan.mirelex.Requests.SizesRequest
import com.example.jonathangalvan.mirelex.Requests.UpdateUserRequest
import kotlinx.android.synthetic.main.activity_profile.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity() {

    var user: UserInterface? = null
    var sizes: ArrayList<CatalogInterface>? = null
    var womanCatalog: WomenCatalogsInterface? = null
    var neighborhoodsArr: NeighborhoodArrayInterface? = null
    var comesFromPhoneVerify: String? = ""
    var myCalendar : Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Get session user info*/
        user = SessionModel(this).getUser()

        /*Get flag from phone verified alert*/
        val bundeFromCustom = intent?.extras
        comesFromPhoneVerify = bundeFromCustom?.getString("comeFromVerifyPhoneAlert")

        /*Set modifiable phone field depending on phone verify flag*/
        if(user?.person?.phoneVerified == "1"){
            updateSessionUserPersonalPhone.editText?.isFocusable = false
        }

        /*Get name depending on userTYpeId*/
        var title : String = ""
        when(user?.person?.userTypeId){
            UserType.Store.userTypeId -> {
                title = user?.person?.companyName.toString()
            }
            else ->{
                title = "${user?.person?.firstName} ${user?.person?.paternalLastName}"
            }
        }
        supportActionBar?.title = title

        /*Fill common fields*/
        updateSessionUserEmail.editText?.setText(user?.person?.email)
        updateSessionUserPersonalPhone.editText?.setText(user?.person?.personalPhone)
        updateSessionUserHomePhone.editText?.setText(user?.person?.homePhone)
        if(user?.address!!.size > 0){
            updateSessionUserStreet.editText?.setText(user?.address!![0].street)
            updateSessionUserNumber.editText?.setText(user?.address!![0].numExt)
            updateSessionUserInternalNumber.editText?.setText(user?.address!![0].numInt)
            updateSessionUserZip.editText?.setText(user?.address!![0].postalCode)
            getNeighborhoods()
        }
        updateSessionUserFacebook.editText?.setText(user?.person?.facebookUrl)
        updateSessionUserInstagram.editText?.setText(user?.person?.instagramUrl)

        /*Postal code event*/
        updateSessionUserZip.editText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                getNeighborhoods()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        /*Clicks measures "?"*/
        imagePreviewHeight.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.heightImage)).show(supportFragmentManager, "alertDialog")
        })

        imagePreviewBust.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.bustImage)).show(supportFragmentManager, "alertDialog")
        })

        imagePreviewWaist.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.waistImage)).show(supportFragmentManager, "alertDialog")
        })

        imagePreviewHip.setOnClickListener(View.OnClickListener {
            ImagePreview().newInstance(resources.getString(R.string.hipImage)).show(supportFragmentManager, "alertDialog")
        })

        /*Hide/show fields depending in usertype*/
        when(user?.person?.userTypeId){
            UserType.Store.userTypeId -> {
                updateSessionUserName.visibility = View.GONE
                updateSessionUserPaternal.visibility = View.GONE
                updateSessionUserMaternal.visibility = View.GONE
                updateSessionUserGender.visibility = View.GONE
                updateSessionUserHeightLayout.visibility = View.GONE
                updateSessionUserSizeWoman.visibility = View.GONE
                (updateSessionUserSize.parent as View).visibility = View.GONE
                (updateSessionUserBodyType.parent as View).visibility = View.GONE
                (updateSessionUserSkinColor.parent as View).visibility = View.GONE
                (updateSessionUserHairColor.parent as View).visibility = View.GONE
                updateSessionUserBustLayout.visibility = View.GONE
                updateSessionUserWaistLayout.visibility = View.GONE
                updateSessionUserHipLayout.visibility = View.GONE
                updateSessionUserBirthDate.visibility = View.GONE

                /*Fill store fields*/
                updateSessionUserCompanyName.editText?.setText(user?.person?.companyName)
                if(user?.person?.isDesigner == 1){
                    updateSessionUserAreYouDesigner.isChecked = true
                }

            }
            else -> {
                /*Hide store fields*/
                updateSessionUserCompanyName.visibility = View.GONE
                updateSessionUserAreYouDesignerLayout.visibility = View.GONE

                /*Fill common person fields*/
                updateSessionUserName.editText?.setText(user?.person?.firstName )
                updateSessionUserPaternal.editText?.setText(user?.person?.paternalLastName)
                updateSessionUserMaternal.editText?.setText(user?.person?.maternalLastName)
                updateSessionUserGender.editText?.setText(user?.person?.userGender)
                updateSessionUserHeight.editText?.setText(user?.characteristics?.height)
                updateSessionUserBirthDate.editText?.setText(user?.person?.birthDate)

                /*Filter fields depending on user gender*/
                when(user?.person?.userGenderId){
                    Gender.Male.genderId -> {
                        /*Hide fields*/
                        (updateSessionUserBodyType.parent as View).visibility = View.GONE
                        (updateSessionUserSkinColor.parent as View).visibility = View.GONE
                        (updateSessionUserHairColor.parent as View).visibility = View.GONE
                        updateSessionUserBustLayout.visibility = View.GONE
                        updateSessionUserWaistLayout.visibility = View.GONE
                        updateSessionUserHipLayout.visibility = View.GONE
                        updateSessionUserSizeWoman.visibility = View.GONE

                        /*Get male catalogs*/
                        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
                        val sizesObj = SizesRequest(ProductType.Suit.productTypeId.toInt())
                        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( this, resources.getString(R.string.userSizes), UtilsModel.getGson().toJson(sizesObj))).enqueue(object:
                            Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                val responseStr = response.body()?.string()
                                val responseObj = UtilsModel.getPostResponse(this@ProfileActivity, responseStr)
                                if(responseObj.status == "success"){
                                    val sizesObj = UtilsModel.getGson().fromJson(responseStr, CatalogArrayInterface::class.java)
                                    sizes = sizesObj.data
                                    runOnUiThread {
                                        run {
                                            /*Fill sizes*/
                                            fillMaleSpinner(sizes, findViewById(R.id.updateSessionUserSize))
                                            updateSessionUserSize.setSelection(getMaleAdapterItemPosition(user?.characteristics?.sizeId?.toLong(), sizes))
                                        }
                                    }
                                }
                            }
                        })
                    }
                    else-> {
                        /*Fill woman fields*/
                        updateSessionUserBust.editText?.setText(user?.characteristics?.bust)
                        updateSessionUserWaist.editText?.setText(user?.characteristics?.waist)
                        updateSessionUserHip.editText?.setText(user?.characteristics?.hip)
                        updateSessionUserSizeWoman?.editText?.setText(user?.characteristics?.size)

                        /*Hide fields*/
                        (updateSessionUserSize.parent as View).visibility = View.GONE

                        /*Get female catalogs*/
                        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
                        UtilsModel.getOkClient().newCall(UtilsModel.postRequest( this, resources.getString(R.string.womanCatalogs))).enqueue(object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                                val responseStr = response.body()?.string()
                                val responseObj = UtilsModel.getPostResponse(this@ProfileActivity, responseStr)
                                if(responseObj.status == "success"){
                                    val womenCatalogObj = UtilsModel.getGson().fromJson(UtilsModel.getGson().toJson(responseObj.data!![0]), WomenCatalogsInterface::class.java)
                                    womanCatalog = womenCatalogObj
                                    runOnUiThread {
                                        run{
                                            /*Fill bodyType*/
                                            fillWomanSpinner(womanCatalog?.bodyTypes, findViewById(R.id.updateSessionUserBodyType))
                                            updateSessionUserBodyType.setSelection(getWomanAdapterItemPosition(user?.characteristics?.bodyTypeId?.toLong(), womanCatalog?.bodyTypes))

                                            /*Fill skinColorType*/
                                            fillWomanSpinner(womanCatalog?.skinColors, findViewById(R.id.updateSessionUserSkinColor))
                                            updateSessionUserSkinColor.setSelection(getWomanAdapterItemPosition(user?.characteristics?.skinColorId?.toLong(), womanCatalog?.skinColors))

                                            /*Fill hairColor*/
                                            fillWomanSpinner(womanCatalog?.hairColors, findViewById(R.id.updateSessionUserHairColor))
                                            updateSessionUserHairColor.setSelection(getWomanAdapterItemPosition(user?.characteristics?.hairColorId?.toLong(), womanCatalog?.hairColors))
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }

        updateSessionUserUpdateInfo.setOnClickListener(View.OnClickListener {
            if(inputValidations()){
                val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
                val updateUserObj = UpdateUserRequest()
                updateUserObj.userId = user?.person?.userId
                updateUserObj.userTypeId = user?.person?.userTypeId
                if(user?.address!!.size > 0){
                    updateUserObj.addressId = user?.address!![0].addressId
                }
                updateUserObj.email = updateSessionUserEmail.editText?.text.toString()
                updateUserObj.personalPhone = updateSessionUserPersonalPhone.editText?.text.toString()
                updateUserObj.homePhone = updateSessionUserHomePhone.editText?.text.toString()
                updateUserObj.street = updateSessionUserStreet.editText?.text.toString()
                updateUserObj.numExt = updateSessionUserNumber.editText?.text.toString()
                updateUserObj.numInt = updateSessionUserInternalNumber.editText?.text.toString()
                updateUserObj.zipCode = updateSessionUserZip.editText?.text.toString()
                if(updateSessionUserNeighborhoods.selectedItemPosition != null){
                    updateUserObj.neighborhoodId = neighborhoodsArr!!.data[updateSessionUserNeighborhoods.selectedItemPosition].neighborhoodId
                }
                updateUserObj.instagramProfile = updateSessionUserInstagram.editText?.text.toString()
                updateUserObj.facebookProfile = updateSessionUserFacebook.editText?.text.toString()

                when(user?.person?.userTypeId){
                    UserType.Store.userTypeId -> {
                        updateUserObj.companyName = updateSessionUserCompanyName.editText?.text.toString()
                        updateUserObj.isMirelexStore = user?.person?.isMirelexStore.toString()
//                        updateUserObj.instagramProfile = updateSessionUserInstagram.editText?.text.toString()
//                        updateUserObj.facebookProfile = updateSessionUserFacebook.editText?.text.toString()
                        updateUserObj.isDesigner = if(updateSessionUserAreYouDesigner.isChecked){1}else{0}
                    }
                    else -> {
                        updateUserObj.height = updateSessionUserHeight.editText?.text.toString()
                        updateUserObj.genderId = user?.person?.userGenderId
                        updateUserObj.firstName = updateSessionUserName.editText?.text.toString()
                        updateUserObj.paternalLastName = updateSessionUserPaternal.editText?.text.toString()
                        updateUserObj.maternalLastName = updateSessionUserMaternal.editText?.text.toString()
                        updateUserObj.characteristicsId = user?.characteristics?.characteristicsId
                        updateUserObj.birthDate = updateSessionUserBirthDate.editText?.text.toString()

                        when(user?.person?.userGenderId){
                            Gender.Male.genderId -> {
                                updateUserObj.sizeId = sizes?.get(updateSessionUserSize.selectedItemPosition)?.productCatalogId.toString()
                            }
                            else -> {
                                updateUserObj.bust = updateSessionUserBust.editText?.text.toString()
                                updateUserObj.waist = updateSessionUserWaist.editText?.text.toString()
                                updateUserObj.hip = updateSessionUserHip.editText?.text.toString()
                                updateUserObj.bodyTypeId = womanCatalog?.bodyTypes!![updateSessionUserBodyType.selectedItemPosition].catalogId
                                updateUserObj.skinColorId = womanCatalog?.skinColors!![updateSessionUserSkinColor.selectedItemPosition].catalogId
                                updateUserObj.hairColorId = womanCatalog?.hairColors!![updateSessionUserHairColor.selectedItemPosition].catalogId
                            }
                        }
                    }
                }

                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.updateUser), UtilsModel.getGson().toJson(updateUserObj))).enqueue(object: Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                        val responseStr = response.body()?.string()
                        val responseObj = UtilsModel.getPostResponse(this@ProfileActivity, responseStr)
                        if(responseObj.status == "success"){
                            SessionModel.saveSessionValue(this@ProfileActivity, "user", UtilsModel.getGson().toJson(responseObj.data!![0]))
                            if((user?.person?.phoneVerified == "0" || user?.person?.phoneVerified == null) && comesFromPhoneVerify != "1"){
                                val ba = UtilsModel.getGson().toJson(BottomAlertInterface(
                                    alertType = "confirmAccountPhone",
                                    formProfilePage = "1"
                                ))
                                val alert = CustomBottomAlert().bottomSheetDialogInstance(ba)
                                alert.isCancelable = false
                                alert.show(supportFragmentManager, "alert")
                            }else{
                                UtilsModel.getAlertView().newInstance(responseStr, 1, 1).show(supportFragmentManager,"alertDialog")
                            }
                        }else{
                            UtilsModel.getAlertView().newInstance(responseStr, 1, 1).show(supportFragmentManager,"alertDialog")
                        }
                    }
                })
            }else{
                val text = resources.getText(R.string.fillRequiredFields)
                val duration = Toast.LENGTH_SHORT
                Toast.makeText(this, text, duration).show()
            }
        })


        myCalendar = Calendar.getInstance()
        val date = object : DatePickerDialog.OnDateSetListener{

            override fun onDateSet(
                view: DatePicker, year: Int, monthOfYear: Int,
                dayOfMonth: Int
            ) {
                myCalendar?.set(Calendar.YEAR, year)
                myCalendar?.set(Calendar.MONTH, monthOfYear)
                myCalendar?.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateField()
            }

        }

        updateSessionUserBirthDate.editText?.setOnClickListener(View.OnClickListener{
            DatePickerDialog(
                this, date, myCalendar!!
                    .get(Calendar.YEAR), myCalendar!!.get(Calendar.MONTH),
                myCalendar!!.get(Calendar.DAY_OF_MONTH)
            ).show()
        })
    }

    fun updateDateField() {
        val myFormat = "yyyy-MM-dd" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        updateSessionUserBirthDate.editText?.setText(sdf.format(myCalendar!!.time))
    }

    fun getNeighborhoods(){
        if(updateSessionUserZip.editText?.text.toString().length > 4){
            val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
            UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this@ProfileActivity, resources.getString(R.string.getNeighborhood), UtilsModel.getGson().toJson(
                NeighborhoodRequest(updateSessionUserZip.editText?.text.toString())
            ))).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                    val responseStr = response.body()?.string()
                    val responseObj = UtilsModel.getPostResponse(this@ProfileActivity, responseStr)
                    if(responseObj.status == "success") {
                        val neighborhoods= UtilsModel.getGson().fromJson(responseStr, NeighborhoodArrayInterface::class.java)
                        val adapter = ArrayAdapter<NeighborhoodInterface>(this@ProfileActivity, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, neighborhoods.data)
                        neighborhoodsArr = neighborhoods
                        adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
                        runOnUiThread {
                            run{
                                updateSessionUserNeighborhoods.adapter = adapter
                                if(user?.address!!.size > 0){
                                    updateSessionUserNeighborhoods.setSelection(getWomanAdapterItemPosition(user?.address!![0].neighborhoodId?.toLong()))
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    fun inputValidations(): Boolean {
        var isCorrect = true
        if(
            updateSessionUserEmail.editText?.text.toString().isEmpty() ||
            updateSessionUserPersonalPhone.editText?.text.toString().isEmpty()
        ){
            isCorrect = false
        }else{
            when(user?.person?.userTypeId){
                UserType.Store.userTypeId -> {
                    if(updateSessionUserCompanyName.editText?.text.toString().isEmpty()){
                        isCorrect = false
                    }
                }
                else -> {
                    if(
                        updateSessionUserName.editText?.text.toString().isEmpty() ||
                        updateSessionUserPaternal.editText?.text.toString().isEmpty()
                    ){
                        isCorrect = false
                    }
                }
            }
        }
        return isCorrect
    }

    fun fillWomanSpinner(data: ArrayList<WomenCatalogInterface>?, adapterView: AdapterView<ArrayAdapter<WomenCatalogInterface>>){
        val adapter = ArrayAdapter<WomenCatalogInterface>(this@ProfileActivity, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, data!!)
        adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
        adapterView.adapter = adapter
    }

    fun fillMaleSpinner(data: ArrayList<CatalogInterface>?, adapterView: AdapterView<ArrayAdapter<CatalogInterface>>){
        val adapter = ArrayAdapter<CatalogInterface>(this@ProfileActivity, R.layout.view_spinner_item_black, R.id.spinnerItemBlackSelect, data!!)
        adapter.setDropDownViewResource(R.layout.view_spinner_item_black_select)
        adapterView.adapter = adapter
    }

    private fun getWomanAdapterItemPosition(id: Long?, mListAdapter: ArrayList<WomenCatalogInterface>?): Int {
        for (position in 0 until mListAdapter!!.size)
            if (mListAdapter.get(position).catalogId?.toLong() == id)
                return position
        return 0
    }

    private fun getMaleAdapterItemPosition(id: Long?, mListAdapter: ArrayList<CatalogInterface>?): Int {
        for (position in 0 until mListAdapter!!.size)
            if (mListAdapter.get(position).productCatalogId?.toLong() == id)
                return position
        return 0
    }

    private fun getWomanAdapterItemPosition(id: Long?): Int {
        for (position in 0 until neighborhoodsArr!!.data.size)
            if (neighborhoodsArr?.data?.get(position)?.neighborhoodId?.toLong() == id)
                return position
        return 0
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
