package com.example.jonathangalvan.mirelex

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.jonathangalvan.mirelex.Interfaces.ConverationMessagesInterface
import com.example.jonathangalvan.mirelex.Interfaces.ConversationInterface
import com.example.jonathangalvan.mirelex.Interfaces.ConversationMessageInterface
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.Requests.CreateConversationMessageRequest
import com.example.jonathangalvan.mirelex.Requests.GetConversationMessagesRequest
import kotlinx.android.synthetic.main.activity_chat.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Get bunlde info*/
        val bundleFromChat = intent.extras
        val conversationObj = UtilsModel.getGson().fromJson(bundleFromChat.getString("conversationObj"), ConversationInterface::class.java)

        /*Set activity actionbar title*/
        supportActionBar?.title = conversationObj.userTo

        /*Fill messages*/
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        val conversationMessagesObj = UtilsModel.getGson().toJson(GetConversationMessagesRequest(
            userIdFrom = conversationObj.userIdFrom,
            userIdTo = conversationObj.userIdTo,
            conversationId = conversationObj.conversationId
        ))
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getConversationMessages), conversationMessagesObj)).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@ChatActivity, responseStr)
                if(responseObj.status == "success"){
                    val messagesObj = UtilsModel.getGson().fromJson(responseStr, ConverationMessagesInterface::class.java)
                    for (message in messagesObj.data) {
                        if(conversationObj.userIdFrom == null){
                            var toName = ""
                            if(SessionModel(this@ChatActivity).getUser().person?.userId == message.userIdFrom){
                                toName = message.userTo.toString()
                                conversationObj.userIdFrom = message.userIdFrom
                                conversationObj.userIdTo = message.userIdTo
                            }else{
                                toName = message.userFrom.toString()
                                conversationObj.userIdFrom = message.userIdTo
                                conversationObj.userIdTo = message.userIdFrom
                            }
                            runOnUiThread {
                                run{
                                    supportActionBar?.title = toName
                                }
                            }
                        }
                        runOnUiThread {
                            run{
                                addMessage(message,null, message.userIdFrom == SessionModel(this@ChatActivity).getUser().person?.userId)
                            }
                        }
                    }
                }
            }
        })

        /*On send event*/
        chatSendMessageButton.setOnClickListener(View.OnClickListener {
            if(chatSendMessageText.text.toString().isNotEmpty() && chatSendMessageText.text.toString() != ""){
                /*Insert message in db*/
                val createMessageObj = UtilsModel.getGson().toJson(CreateConversationMessageRequest(
                    userIdFrom = conversationObj.userIdFrom,
                    userIdTo = conversationObj.userIdTo,
                    message = chatSendMessageText.text.toString(),
                    conversationId = conversationObj.conversationId
                ))
                UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.createConversationMessage), createMessageObj)).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    override fun onResponse(call: Call, response: Response) {}
                })

                /*Add automatically message in container*/
                addMessage(null, chatSendMessageText.text.toString(), true)
            }
        })

        chatSendMessageScrollView.addOnLayoutChangeListener(object: View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                chatSendMessageScrollView.fullScroll(View.FOCUS_DOWN)
            }
        })
    }

    fun addMessage(message: ConversationMessageInterface?, messageStr: String?, isUserMessage: Boolean){
        val nt = layoutInflater.inflate(R.layout.view_chat_text, chatSendMessages, false)
        (nt.findViewById<TextView>(R.id.viewChatText)).text = message?.message ?: messageStr
        (nt.findViewById<TextView>(R.id.viewChatDate)).text = message?.created ?: ""
        if(isUserMessage){
            ((nt.findViewById<TextView>(R.id.viewChatText)).parent.parent as LinearLayout).gravity = Gravity.RIGHT
            ((nt.findViewById<TextView>(R.id.viewChatText)).parent as CardView).setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorBlue))
        }
        chatSendMessageText.setText("")
        chatSendMessages.addView(nt)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

