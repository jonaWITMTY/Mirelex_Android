package com.example.jonathangalvan.mirelex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import kotlinx.android.synthetic.main.view_progressbar.*

class TransactionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.transactions)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}