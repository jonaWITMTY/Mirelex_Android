package com.example.jonathangalvan.mirelex.Adapters

import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentManager
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.R
import com.squareup.picasso.Picasso

class ZoomSliderPagerAdapter(var images: ArrayList<String>, var fm: FragmentManager?) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout = LayoutInflater.from(container.context).inflate(R.layout.view_image_zoomslider, container, false)
        val img = imageLayout.findViewById<ImageView>(R.id.imageZoomSliderImg)

        if(images.isNotEmpty()){
//            Picasso.with(container.context).load(images[position]).into(img)
            Glide.with(container.context).load(images[position]).apply( RequestOptions().override(800, 0)).into(img)
        }else{
            Picasso.with(container.context).load(R.drawable.mirelex_logo_cian).into(img)
        }


        container.addView(imageLayout)
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0 === p1
    }

    override fun getCount(): Int {
        return if(images.size != 0) {
            return images.size
        }else {
            return 1
        }
    }

}