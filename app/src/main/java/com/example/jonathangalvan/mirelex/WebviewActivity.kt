package com.example.jonathangalvan.mirelex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_webview.*

class WebviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.termsAndConditions)
        webView.loadUrl("${resources.getString(R.string.apiUrl)}/v1/terms.php")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
