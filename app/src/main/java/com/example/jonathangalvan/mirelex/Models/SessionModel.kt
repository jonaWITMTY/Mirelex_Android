package com.example.jonathangalvan.mirelex.Models

import android.content.Context
import com.example.jonathangalvan.mirelex.Interfaces.UserInterface
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SessionModel(val declaredContext: Context){
    companion object{

        fun saveSessionValue(context: Context, tag: String, value: String){
            val pref = context.getSharedPreferences("SessionData", 0)
            val editor = pref.edit()
            editor.putString(tag, value)
            editor.apply()
        }

        fun getSessionValue(context: Context, tag: String): String?{
            val pref = context.getApplicationContext().getSharedPreferences("SessionData", 0)
            return pref.getString(tag, null)
        }

        fun existSessionValue(context: Context, tag: String): Boolean {
            val sharedPrefs = context.getSharedPreferences("SessionData", MODE_PRIVATE)
            return sharedPrefs.contains(tag)
        }

        fun deleteSessionValue(context:Context, tag: String){
            val preferences = context.getSharedPreferences("SessionData", 0)
            preferences.edit().remove(tag).commit()
        }
    }

    /*Extra session methods*/
    fun getUser(): UserInterface{
        val userJson = getSessionValue(declaredContext,"user")
        return UtilsModel.getGson().fromJson(userJson, UserInterface::class.java)
    }

    fun getSessionUserType(): String?{
        val user = getUser()
        return user.person?.userTypeId
    }

    fun signOutSession(){
        saveSessionValue(declaredContext, "token", "")
        saveSessionValue(declaredContext, "user", "")
    }
}