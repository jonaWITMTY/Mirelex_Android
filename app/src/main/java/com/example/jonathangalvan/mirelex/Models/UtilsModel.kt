package com.example.jonathangalvan.mirelex.Models

import android.content.Context
import com.example.jonathangalvan.mirelex.Fragments.CustomAlert
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.Requests.GlobalRequest
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class UtilsModel {
    companion object {
        private val gson = Gson()
        private val client = OkHttpClient()
        private var alertView : CustomAlert = CustomAlert()
        private val errorRequestCall: String = "{'title': 'Error en sistema', 'desc': 'Lo sentimos, por el momento estamos trabajando en ello'}"
        private val errorMissingTerms: String = "{'title': 'Términos y condiciones', 'desc': 'Por favor acepta nuestros términos y condiciones'}"
        private val errorUserAccessDenied: String = "{'title': 'Acceso denegado', 'desc': 'El tipo de usuario no es váido'}"

        fun postRequest(context: Context, endPoint: String, data: String = getGson().toJson(GlobalRequest())): Request{
            var newData = data
            if(newData == "") {
                newData = getGson().toJson(GlobalRequest())
            }
            val body: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), newData)
            val request = Request.Builder()
            request.url("${context.resources.getString(R.string.apiUrl)}/v1/$endPoint")
            request.addHeader("Public-Key", context.getResources().getString(R.string.publicKey))
            request.post(body)
            if(SessionModel.getSessionValue(context, "token") != null){
                request.addHeader("token", SessionModel.getSessionValue(context, "token")!!)
            }
            return request.build()
        }

        fun getPostResponse(response: String?): ResponseInterface{
            return getGson().fromJson(response, ResponseInterface::class.java)
        }

        fun dptopx(context: Context, dp: Int): Int {
            val scale = context.resources.displayMetrics.density.toInt()
            val pixels = (dp * scale)
            return pixels
        }

        fun getAlertView(): CustomAlert {
            return alertView
        }

        fun getGson(): Gson{
            return gson
        }

        fun getOkClient(): OkHttpClient{
            return client
        }

        fun getErrorRequestCall(): String {
            return errorRequestCall
        }

        fun getErrorMissingTerms(): String {
            return errorMissingTerms
        }

        fun getErrorUserAccessDenied(): String{
            return errorUserAccessDenied
        }
    }
}