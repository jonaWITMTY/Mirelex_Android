package com.example.jonathangalvan.mirelex.Models

import android.content.Context
import com.example.jonathangalvan.mirelex.Interfaces.UserInterface

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
    }

    /*Extra session methods*/
    fun getUser(): UserInterface{
        val userJson = getSessionValue(declaredContext,"user")
        return UtilsModel.getGson().fromJson(userJson, UserInterface::class.java)
    }
}