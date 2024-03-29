package com.example.jonathangalvan.mirelex.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jonathangalvan.mirelex.Interfaces.*
import com.example.jonathangalvan.mirelex.Requests.UpdateUserRequest

class RegisterViewModel: ViewModel() {

    private var gender = MutableLiveData<ArrayList<GenderInterface>>()
    val genderCall: LiveData<ArrayList<GenderInterface>> get() = gender

    private var user = MutableLiveData<UserInterface>()
    val userCall: LiveData<UserInterface> get() = user

    private var comleteUser = MutableLiveData<UpdateUserRequest>()
    val comleteUserCall: LiveData<UpdateUserRequest> get() = comleteUser

    var sizes = MutableLiveData<ArrayList<CatalogInterface>>()

    var womenCatalogs = MutableLiveData<WomenCatalogsInterface>()

    var productTypes = ArrayList<ProductTypeInterface>()

    init {
        setGenders()
        setCompleteUser(UpdateUserRequest())
    }

    private fun setGenders() {
        val contacts = ArrayList<GenderInterface>()
        contacts.add(GenderInterface("1", "Masculino"))
        contacts.add(GenderInterface("2", "Femenino"))
        gender.value = contacts
    }

    fun setUser(userObj: UserInterface){
        user.value = userObj
    }

    fun setCompleteUser(registerReq: UpdateUserRequest){
        comleteUser.value = registerReq
    }
}