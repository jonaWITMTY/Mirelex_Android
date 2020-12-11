package com.example.jonathangalvan.mirelex.Fragments.Utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jonathangalvan.mirelex.Adapters.ZoomSliderPagerAdapter
import com.example.jonathangalvan.mirelex.R
import me.relex.circleindicator.CircleIndicator

class ImagePreview : androidx.fragment.app.DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(androidx.fragment.app.DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    fun newInstance(image: Any?, position: Int = -1): ImagePreview {
        val ip = ImagePreview()
        val args = Bundle()
        if(image is String){
            args.putString("image", image)
            var images: ArrayList<String> = ArrayList()
            images.add(image)
            args.putStringArrayList("image", images)
        }else{
            args.putStringArrayList("image", image as ArrayList<String>?)
        }
        args.putInt("position", position)
        ip.arguments = args
        return ip
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertView = activity?.layoutInflater!!.inflate(R.layout.fragment_image_preview, null)
        val alert = AlertDialog.Builder(activity)
//        Picasso.with(activity!!).load(arguments?.getString("image")).error(R.drawable.mirelex_logo_cian).into(alertView.findViewById<ImageView>(R.id.imagePreviewMain))
//        Glide.with(activity!!).load(arguments?.getString("image")).apply( RequestOptions().override(800, 0)).into(alertView.findViewById<ImageView>(R.id.imagePreviewMain))


        val sliderAdapter = ZoomSliderPagerAdapter(arguments!!.getStringArrayList("image"), activity?.supportFragmentManager)
        alertView.findViewById<androidx.viewpager.widget.ViewPager>(R.id.imagePreviewSlider).adapter = sliderAdapter
        if(arguments!!.getInt("position") != -1){
            alertView.findViewById<androidx.viewpager.widget.ViewPager>(R.id.imagePreviewSlider).currentItem = arguments!!.getInt("position")
        }
        alertView.findViewById<CircleIndicator>(R.id.imagePreviewIndicator).setViewPager(alertView.findViewById<androidx.viewpager.widget.ViewPager>(R.id.imagePreviewSlider))


        alertView.findViewById<ImageView>(R.id.imagePreviewClose).setOnClickListener(View.OnClickListener {
            onDismiss(dialog)
        })
        alert.setView(alertView)
        return alert.create()
    }

}
