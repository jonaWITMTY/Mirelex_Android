package com.example.jonathangalvan.mirelex.Adapters

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jonathangalvan.mirelex.Fragments.Utils.ImagePreview
import com.example.jonathangalvan.mirelex.R
import com.squareup.picasso.Picasso

class SliderPagerAdapter(var images: ArrayList<String>, var fm: androidx.fragment.app.FragmentManager?) : androidx.viewpager.widget.PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout = LayoutInflater.from(container.context).inflate(R.layout.view_image_slider, container, false)
        val img = imageLayout.findViewById<ImageView>(R.id.imageSliderImg)

        if(images.isNotEmpty()){
//            Picasso.with(container.context).load(images[position]).into(img)
            Glide.with(container.context).load(images[position]).apply( RequestOptions().override(800, 0)).into(img)
            img.setOnClickListener( View.OnClickListener {
                ImagePreview().newInstance(images, position).show(fm,"alertDialog")
            })
        }else{
            Picasso.with(container.context).load(R.drawable.mirelex_logo_cian).into(img)
        }


        container.addView(imageLayout)
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
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