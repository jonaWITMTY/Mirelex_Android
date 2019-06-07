package com.example.jonathangalvan.mirelex.Models

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomAlert
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.MainActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.RegisterActivity
import com.example.jonathangalvan.mirelex.Requests.GlobalRequest
import com.google.gson.Gson
import okhttp3.*
import java.io.File

class UtilsModel {
    companion object {
        private val gson = Gson()
        private val client = OkHttpClient()
        private var alertView : CustomAlert =
            CustomAlert()
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

        fun postImageRequest(context: Context, file: File, endPoint: String, id: String) : Request {
            val formBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file_0", file.name,
                    RequestBody.create(MediaType.parse("text/plain"), file)
                )
                .addFormDataPart("productId", id)
                .addFormDataPart("PrivateKey", context.resources.getString(R.string.privateKey))
                .build()
            val request = Request.Builder()
            request.url("${context.resources.getString(R.string.apiUrl)}/v1/$endPoint")
            request.addHeader("Public-Key", context.resources.getString(R.string.publicKey))
            request.post(formBody)
            if(SessionModel.getSessionValue(context, "token") != null){
                request.addHeader("token", SessionModel.getSessionValue(context, "token")!!)
            }
            return request.build()
        }

        fun getImagePermissions(context: Context): Boolean{
            var isPermissionsCorrect = true
            if (Build.VERSION.SDK_INT >= 23) {
                val permissionCheck =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    isPermissionsCorrect = false
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                }
            }
            return isPermissionsCorrect
        }

        fun getPostResponse(response: String?): ResponseInterface{
            val responseObj = getGson().fromJson(response, ResponseInterface::class.java)
            if(responseObj.status == "sessionFailed"){
                val context: Context = MainActivity.getMainContext()
                SessionModel(context).signOutSession()
                context.startActivity(Intent(context, MainActivity::class.java))
            }
            return responseObj
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