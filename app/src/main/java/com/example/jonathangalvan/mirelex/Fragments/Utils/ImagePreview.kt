package com.example.jonathangalvan.mirelex.Fragments.Utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.example.jonathangalvan.mirelex.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_preview.*

class ImagePreview : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    fun newInstance(image: String?): ImagePreview {
        val ip = ImagePreview()
        val args = Bundle()
        args.putString("image", image)
        ip.setArguments(args)
        return ip
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertView = activity?.layoutInflater!!.inflate(R.layout.fragment_image_preview, null)
        val alert = AlertDialog.Builder(activity)
        Picasso.with(activity!!).load(arguments?.getString("image")).error(R.drawable.mirelex_logo_cian).into(alertView.findViewById<ImageView>(R.id.imagePreviewMain))
        alertView.findViewById<ImageView>(R.id.imagePreviewClose).setOnClickListener(View.OnClickListener {
            onDismiss(dialog)
        })
        alert.setView(alertView)
        return alert.create()
    }

}
