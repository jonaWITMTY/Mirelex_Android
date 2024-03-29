package com.example.jonathangalvan.mirelex.Models

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomAlert
import com.example.jonathangalvan.mirelex.Interfaces.ResponseInterface
import com.example.jonathangalvan.mirelex.MainActivity
import com.example.jonathangalvan.mirelex.R
import com.example.jonathangalvan.mirelex.RegisterActivity
import com.example.jonathangalvan.mirelex.Requests.GlobalRequest
import com.example.jonathangalvan.mirelex.SplashActivity
import com.google.gson.Gson
import okhttp3.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import org.json.JSONException
import org.json.JSONArray
import org.json.JSONObject

class UtilsModel {
    companion object {
        private val gson = Gson()
        private val client = OkHttpClient()
        private var alertView : CustomAlert = CustomAlert()
        private val errorRequestCall: String = "{'status': 'systemError'}"
        private val errorMissingTerms: String = "{'status': 'termsAndCondition'}"
        private val errorUserAccessDenied: String = "{'status': 'deniedAccess'}"

        fun postRequest(context: Context, endPoint: String, data: String = getGson().toJson(GlobalRequest())): Request{
            var newData = data
            if(newData == "") {
                newData = getGson().toJson(GlobalRequest())
            }
            val body: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), newData)
            val request = Request.Builder()
            request.url("${context.resources.getString(R.string.apiUrl)}/v1/$endPoint")
            request.addHeader("Public-Key", context.getResources().getString(R.string.publicKey))
            request.addHeader("Accept-Language", Locale.getDefault().language)
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
            request.addHeader("Accept-Language", Locale.getDefault().language)
            request.post(formBody)
            if(SessionModel.getSessionValue(context, "token") != null){
                request.addHeader("token", SessionModel.getSessionValue(context, "token")!!)
            }
            return request.build()
        }

        fun postImagesRequest(context: Context, files: ArrayList<File>, endPoint: String, id: String) : Request {
            val formBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("productId", id)
                .addFormDataPart("PrivateKey", context.resources.getString(R.string.privateKey))

            for((index, file) in files.withIndex()){
                formBody.addFormDataPart(
                    "file_$index", file.name,
                    RequestBody.create(MediaType.parse("text/plain"), file)
                )
            }

            val request = Request.Builder()
            request.url("${context.resources.getString(R.string.apiUrl)}/v1/$endPoint")
            request.addHeader("Public-Key", context.resources.getString(R.string.publicKey))
            request.post(formBody.build())
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

        fun getPostResponse(context: Context?, response: String?): ResponseInterface{
            var responseObj = ResponseInterface(status = "systemError", title = "", desc = "", data = ArrayList())
            if(response != null && response != "" && isJSONValid(response)){
                responseObj = getGson().fromJson(response, ResponseInterface::class.java)
                if(responseObj.status == "sessionFailed"){
                    if (context != null) {
                        SessionModel(context).signOutSession()
                    }
                    val goToMain = Intent(context, MainActivity::class.java)
                    goToMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context?.startActivity(goToMain)
                }
            }
            return responseObj
        }

        fun dptopx(context: Context, dp: Int): Int {
            val scale = context.resources.displayMetrics.density.toInt()
            val pixels = (dp * scale)
            return pixels
        }

        fun isJSONValid(test: String): Boolean {
            try {
                JSONObject(test)
            } catch (ex: JSONException) {
                // edited, to include @Arthur's comment
                // e.g. in case JSONArray is valid as well...
                try {
                    JSONArray(test)
                } catch (ex1: JSONException) {
                    return false
                }

            }

            return true
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