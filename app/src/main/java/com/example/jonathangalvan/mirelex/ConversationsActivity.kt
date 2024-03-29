package com.example.jonathangalvan.mirelex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.Adapters.ConversationsAdapter
import com.example.jonathangalvan.mirelex.Interfaces.ConversationInterface
import com.example.jonathangalvan.mirelex.Interfaces.ConversationsInterface
import com.example.jonathangalvan.mirelex.Listeners.RecyclerItemClickListener
import com.example.jonathangalvan.mirelex.Models.SessionModel
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import kotlinx.android.synthetic.main.activity_conversations.*
import kotlinx.android.synthetic.main.view_centered_message.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class ConversationsActivity : AppCompatActivity() {

    var conversationAdapter: ConversationsAdapter = ConversationsAdapter(this, ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.conversations)

        /*Fill conversation list*/
        conversationsList.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        conversationsList.adapter = conversationAdapter
        val loader = layoutInflater.inflate(R.layout.view_progressbar, findViewById(android.R.id.content), true)
        UtilsModel.getOkClient().newCall(UtilsModel.postRequest(this, resources.getString(R.string.getConversations))).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                UtilsModel.getAlertView().newInstance(UtilsModel.getErrorRequestCall(), 1, 0).show(supportFragmentManager,"alertDialog")
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {run{findViewById<ViewGroup>(android.R.id.content).removeView(findViewById(R.id.view_progressbar))}}
                val responseStr = response.body()?.string()
                val responseObj = UtilsModel.getPostResponse(this@ConversationsActivity, responseStr)
                when(responseObj.status){
                    "success" -> {
                        var conversationObj =
                            UtilsModel.getGson().fromJson(responseStr, ConversationsInterface::class.java)
                        runOnUiThread {
                            run {
                                conversationAdapter.loadNewData(conversationObj.data)
                            }
                        }
                    }
                    "noDataAvailable" -> {
                        runOnUiThread {
                            run {
                                val ceneteredLayout = layoutInflater.inflate(
                                    R.layout.view_centered_message,
                                    findViewById(android.R.id.content),
                                    true
                                )
                                ceneteredLayout.centeredMessage.text = responseObj.desc
                            }
                        }
                    }
                    else -> {
                        UtilsModel.getAlertView().newInstance(responseStr, 1, 0).show(supportFragmentManager,"alertDialog")
                    }
                }
            }
        })

        /*On conversation click event*/
        conversationsList.addOnItemTouchListener(RecyclerItemClickListener(this, conversationsList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val bundleToChat = Bundle()
                val goToChat = Intent(this@ConversationsActivity, ChatActivity::class.java)
                val sessionUser = SessionModel(this@ConversationsActivity).getUser()
                val conversation = conversationAdapter.getItem(position)
                val conversationObj = ConversationInterface(
                    userIdTo = conversation.userIdTo,
                    userIdFrom = sessionUser.person?.userId,
                    userTo = if(sessionUser.person?.userId == conversation.userIdTo) conversation.userFrom else conversation.userTo,
                    conversationId = conversation.conversationId
                )
                bundleToChat.putString("conversationObj", UtilsModel.getGson().toJson(conversationObj))
                goToChat.putExtras(bundleToChat)
                startActivity(goToChat)
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
